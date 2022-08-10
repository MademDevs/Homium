package de.madem.homium.utilities

import android.content.Context
import android.widget.Toast
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.backgroundtasks.UserRequestedCoroutineBackgroundTask
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.showToastLong
import de.madem.homium.utilities.extensions.showToastShort
import de.madem.homium.utilities.quantitycalculation.InventoryQuantityCalculationOperator
import de.madem.homium.utilities.quantitycalculation.UnitConverter
import java.lang.ref.WeakReference


/**
 * The class InventoryQuantityOperationInformation encapsulates an information for id and quantity
 * returned after calculating with quantities (= Infomation what is the result of a quantity calculation with
 * certain Inventoryitems like "From all Inventoryitems with their ids in List i have to delete this
 * amount with unit and count, but i do not delete it from each element but from the hole itemquantity")
 * */
@JvmInline
value class InventoryQuantityOperationInformation(val info: Triple<List<Int>,Int,String>)

/**
 * The class CookingAssistant is made for cooking elements in Homium and deals with certain operations in Database.
 * */

typealias AnalysisResult = Pair<List<ShoppingItem>,List<InventoryQuantityOperationInformation>>

class CookingAssistant @AssistedInject constructor(
    @Assisted private val contextReference: WeakReference<Context>,
    private val db: AppDatabase
) {

    //fields
    private val recipeDao = db.recipeDao()
    private val inventoryDao = db.inventoryDao()
    private val shoppingDao = db.shoppingDao()

    private val unitConverter : UnitConverter = UnitConverter()
    private val inventoryQuantityOperator = InventoryQuantityCalculationOperator(unitConverter, db.inventoryDao())

    //public functions
    fun cookRecipe(recipe: Recipe){

        CoroutineBackgroundTask<AnalysisResult>()
            .executeInBackground {
            //analyze
            analyzeRecipe(recipe)

        }.onDone {analysisResult ->
            if(analysisResult.first.isEmpty()){
                subtractFromInventory(analysisResult.second)
            }
            else{
                addMissingShoppingItemsToShoppingList(analysisResult.first)
            }
        }.start()


    }


    //private functions
    /*THIS FUNCTION SHOULD ONLY BE CALLED FROM A BACKGROUND THREAD BECAUSE OF ROOM INTERACTION*/
    private fun analyzeRecipe(recipe : Recipe) : AnalysisResult{

        //creating  lists for data
        val missingShoppingItems = mutableListOf<ShoppingItem>()
        val subtractQuantities = mutableListOf<InventoryQuantityOperationInformation>()


        //getting all ingredients of a recipe
        val ingredientsOfRecipe = recipeDao.getIngredientByRecipeId(recipeId = recipe.uid)

        //check ingriedients in inventory
        for(ingredient in ingredientsOfRecipe){

            //checking unit in cases of converting units

            //case of "simple"/non-convertable unit
            if(ingredient.unit == Units.ITEM.getString() || ingredient.unit == Units.PACK.getString()){
                // How much elements with name and unit of this ingredient are in inventory?
                val quantitySum = inventoryDao.sumOfQuantityByNameUnit(name = ingredient.name,unit = ingredient.unit)

                //decide what to do depending on quantitySum
                //case that there is less in inventory than needed in ingredient
                if(quantitySum < ingredient.count){
                    val countDifference : Int = ingredient.count - quantitySum
                    missingShoppingItems.add(ShoppingItem(
                        ingredient.name,
                        countDifference,
                        Units.unitOf(ingredient.unit) ?: Units.default)
                    )
                }
                //case that there is more in inventory than needed in ingredient
                else{
                    val inventoryIdsToSubtractFrom : List<Int> = inventoryDao.getUIdsByNameUnit(name = ingredient.name,unit = ingredient.unit)
                    val subtractInfo = Triple(inventoryIdsToSubtractFrom,ingredient.count,ingredient.unit)
                    subtractQuantities.add(InventoryQuantityOperationInformation(subtractInfo))
                }
            }
            //case of "difficult"/convertable unit
            else{
                //Getting all inventoryitems with an convertible unit and name of ingredient
                val convertibleItemsWithName = inventoryDao.getInventoryItemWithNameWithoutForbiddenUnits(
                    name = ingredient.name,
                    forbiddenUnits = Units.nonconvertibleUnits())

                //creating unit value of ingriedient unit string (if not possible, ingriedent is ignored)
                val ingredientOriginUnit : Units = Units.unitOf(ingredient.unit) ?: continue

                var ingredientUnit = ingredientOriginUnit
                var ingredientCount = ingredient.count

                if(ingredientOriginUnit.isBigUnit()){
                    //scale down big units to small unit to avoid mistakes because of integer division
                    val convResult = unitConverter.convertUnit(Pair(ingredientCount,ingredientUnit.getString()),
                        ingredientUnit.getDownscaledUnit())

                    ingredientCount = convResult.first
                    ingredientUnit = Units.unitOf(convResult.second) ?: continue
                }


                //getting sum of quantity of convertible units
                val quantitySumInInventory = convertibleItemsWithName
                    .map { unitConverter.convertUnitOf(it,ingredientUnit) }
                    .map { it.count }
                    .sum()

                //decide what to do depending on quantitySum
                //case that there is less in inventory than needed in ingredient
                if(quantitySumInInventory < ingredientCount){
                    val countDifference = ingredientCount - quantitySumInInventory
                    if(allowedToScaleBack(countDifference,ingredientOriginUnit)){
                        val backConvert = unitConverter.convertUnit(Pair(countDifference,ingredientUnit.getString()),ingredientOriginUnit)
                        missingShoppingItems.add(
                            ShoppingItem(
                                ingredient.name,
                                backConvert.first,
                                Units.unitOf(backConvert.second) ?: Units.default
                            )
                        )
                    }
                    else{
                        missingShoppingItems.add(
                            ShoppingItem(
                                ingredient.name,
                                countDifference,
                                Units.unitOf(ingredientUnit.getString()) ?: Units.default
                            )
                        )
                    }

                }
                //case that there is more in inventory than needed in ingredient
                else{
                    val subtractValues : Triple<List<Int>,Int,String> =
                        if(allowedToScaleBack(ingredientCount,ingredientUnit)) {
                            val backConvert = unitConverter.convertUnit(Pair(ingredientCount,ingredientUnit.getString()),ingredientOriginUnit)
                            Triple(convertibleItemsWithName.map{it.uid},backConvert.first,backConvert.second)
                    }
                    else{
                        Triple(convertibleItemsWithName.map{it.uid},ingredientCount,ingredientUnit.getString())
                    }


                    subtractQuantities.add(InventoryQuantityOperationInformation(subtractValues))
                }

            }

        }

        //creating and returning result
        return AnalysisResult(missingShoppingItems, subtractQuantities)

    }


    private fun addMissingShoppingItemsToShoppingList(missingItems : List<ShoppingItem>){

        val msg = createMissingShoppingMessage(missingItems)

        UserRequestedCoroutineBackgroundTask<Boolean>(
            contextReference,
            message = msg
        )
            .executeInBackground {
                if (missingItems.isEmpty()){
                    return@executeInBackground false
                }
                else{
                    shoppingDao.insertShoppingItem(*(missingItems.toTypedArray()))
                    return@executeInBackground true
                }
            }.onDone { success ->
                if(success){
                    showToast(R.string.cooking_notification_success_add_missing_shoppingitems)
                }
                else{
                    showToast(R.string.cooking_error_failed_add_missing_shoppingitems)
                }
            }.start()
    }

    private fun subtractFromInventory(operations : List<InventoryQuantityOperationInformation>){
        UserRequestedCoroutineBackgroundTask<Unit>(contextReference,R.string.cooking_question_subtract_items_from_inventory)
            .executeInBackground {

                //executing each operation
                operations.forEach { operationInfo ->
                    inventoryQuantityOperator.subractFromInventory(operationInfo)
                }

            }.onDone {
                showToast(R.string.cooking_notification_success_subtract_from_inventory,Toast.LENGTH_LONG)
            }.start()

    }

    private fun allowedToScaleBack(count : Int, originUnit : Units): Boolean {
        return (count % 1000 == 0) && (originUnit.isBigUnit())
    }


    private fun showToast(msgId: Int,length: Int = Toast.LENGTH_SHORT){
        contextReference.get().notNull {
            with(it){
                if(length == Toast.LENGTH_SHORT){
                    showToastShort(msgId)
                }
                else{
                    showToastLong(msgId)
                }

            }
        }
    }

    private fun showToast(msg : String,length: Int = Toast.LENGTH_SHORT) = contextReference.get().notNull {
        Toast.makeText(it,msg,length).show()
    }

    private fun createMissingShoppingMessage(missingItems: List<ShoppingItem>) : String {
        val cntxt = contextReference.get()

        val baseString = cntxt?.getString(R.string.cooking_question_add_missing_shoppingitems)
            ?: "Das Rezept kann leider nicht gekocht werden. Es fehlen folgende Zutaten: #Sollen diese Zutaten auf die Einkaufsliste gesetzt werden?"

        val replaceString = missingItems.joinToString("\n") { "${it.count} ${it.unit} ${it.name}" }

        return baseString.replace("#","\n\n$replaceString\n\n")

    }
}