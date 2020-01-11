package de.madem.homium.utilities

import android.content.Context
import android.widget.Toast
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import java.lang.ref.WeakReference



/*
 * The class InventoryQuantityOperationInformation encapsulates an information for id and quantity
 * returned after calculating with quantities (= Infomation what is the result of a quantity calculation with
 * certain Inventoryitems like "From all Inventoryitems with their ids in List i have to delete this
 * amount with unit and count, but i do not delete it from each element but from the hole itemquantity")
 *
 *
 * */
inline class InventoryQuantityOperationInformation(val info: Triple<List<Int>,Int,String>)

/*
 * The class CookingAssistant is made for cooking elements in Homium and deals with certain operations in Database.
 *
 * */

typealias AnalysisResult = Pair<List<ShoppingItem>,List<InventoryQuantityOperationInformation>>



class CookingAssistant(private val contextReference: WeakReference<Context>) {

    //fields
    private val recipeDao = AppDatabase.getInstance().recipeDao()
    private val inventoryDao = AppDatabase.getInstance().inventoryDao()
    private val shoppingDao = AppDatabase.getInstance().itemDao()

    //public functions
    fun cookRecipe(recipe: Recipe){

        CoroutineBackgroundTask<AnalysisResult>().executeInBackground {
            //analyze
            analyzeRecipe(recipe)

        }.onDone {analysisResult ->
            if(analysisResult.first.isEmpty()){
                //TODO: Implement logic for subtracting from inventory
                showToast("STILL TODO: DELETE FROM INVENTORY")
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
                    missingShoppingItems.add(ShoppingItem(ingredient.name,countDifference,ingredient.unit))
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
                //TODO: Implement logic for analyzing data with convertable units
            }




        }

        //TODO: create code for returning a list of shopping items to be set on shopping list or data to be subtracted from Inventory

        //creating and returning result
        return AnalysisResult(missingShoppingItems, subtractQuantities)

    }


    private fun addMissingShoppingItemsToShoppingList(missingItems : List<ShoppingItem>){

        val msg = createMissingShoppingMessage(missingItems)

        UserRequestedCoroutineBackgroundTask<Boolean>(contextReference, message = msg)
            .executeInBackground {
                if (missingItems.isEmpty()){
                    return@executeInBackground false
                }
                else{
                    shoppingDao.insertShopping(*(missingItems.toTypedArray()))
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