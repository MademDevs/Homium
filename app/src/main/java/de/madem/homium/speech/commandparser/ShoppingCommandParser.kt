package de.madem.homium.speech.commandparser

import android.content.Context
import de.madem.homium.R
import de.madem.homium.databases.ItemDao
import de.madem.homium.models.Product
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.ref.WeakReference

class ShoppingCommandParser(private val contextRef: WeakReference<Context>, private val shoppingDao: ItemDao) {

    fun parseShoppingItem(splittedWords : List<String>) : ShoppingItem?{

        if(splittedWords.size < 3){
            return null
        }

        //name
        val name = splittedWords[2].trim().split(" ").map { it.capitalize() }.joinToString(" ")
        println("NAME = $name")
        //unit
        var unit : String = splittedWords[1].trim().capitalize()
        println("UNIT = $unit")

        if(unit == "Kilo"){

            unit = contextRef.get()?.resources?.getString(R.string.data_units_kilogram) ?: "Kilogramm"
        }

        if(unit.contains(Units.PACK.getString())){
            unit = unit.replace("en","")
        }
        else if (!(Units.stringShortcuts().contains(unit.toLowerCase()) || Units.stringValueArray().contains(unit))){
            return null
        }

        //quantity
        val quantity = splittedWords[0].trim().toIntOrNull() ?: return null //replaceNumberWords(splittedWords[0]).toIntOrNull() ?: return null
        println("QUANTITY = $quantity")
        return ShoppingItem(name,quantity,Units.shortCutToLongValue(unit))

    }

    suspend fun parseShoppingItemWithoutUnit(splittedWords : List<String>) : ShoppingItem? = coroutineScope{

        if(splittedWords.size < 2){
            return@coroutineScope null
        }

        //name
        val name = splittedWords[1].trim().split(Regex(" ")).map { it.capitalize() }.joinToString(" ")

        val matchingProductsDeffered = async<List<Product>> {
            shoppingDao.getProductsByNameOrPlural(name)
        }


        //quantity
        val amount = splittedWords[0].trim().toIntOrNull()


        if(name.isNotEmpty() && name.isNotBlank() && amount != null){

            val productResult = matchingProductsDeffered.await()

            if(productResult.isEmpty()){
                return@coroutineScope ShoppingItem(name,amount,Units.ITEM.getString())
            }
            else{
                val productNameIndex = productResult.map { it.name }.indexOf(name)
                val productPluralNameIndex = productResult.map { it.plural }.indexOf(name)
                if(productNameIndex >= 0 || productPluralNameIndex >= 0) {
                    val newUnit = if(productPluralNameIndex < 0) productResult[productNameIndex].unit else productResult[productPluralNameIndex].unit
                    return@coroutineScope ShoppingItem(name,amount,newUnit)
                }
                else{
                    return@coroutineScope ShoppingItem(name,amount,Units.ITEM.getString())
                }
            }
        }
        else{
            return@coroutineScope null
        }
    }

    suspend fun parseShoppingItemWithoutUnitWithoutAmount(nameIn : String) : ShoppingItem? = coroutineScope{

        val productsDeffered = async<List<Product>> {
            shoppingDao.getProductsByNameOrPlural(nameIn.capitalize())
        }

        if(nameIn.isNotBlank() && nameIn.isNotEmpty()){

            val name = nameIn.trim().split(Regex(" ")).map { it.capitalize() }.joinToString(" ")

            val productResult = productsDeffered.await()

            val productNameIndex = productResult.map { it.name }.indexOf(name)
            val productPluralIndex = productResult.map { it.plural }.indexOf(name)
            return@coroutineScope when {
                productNameIndex >= 0 -> {
                    val newUnit = productResult[productNameIndex].unit
                    val newQuantity = productResult[productNameIndex].amount.toIntOrNull() ?: 1
                    ShoppingItem(name,newQuantity,newUnit)
                }
                productPluralIndex >= 0 -> {
                    val newUnit = productResult[productPluralIndex].unit
                    val newQuantity = productResult[productPluralIndex].amount.toIntOrNull()?.takeIf { it > 1 } ?: 2
                    ShoppingItem(name,newQuantity,newUnit)

                }
                else -> {
                    ShoppingItem(name,1,Units.ITEM.getString())
                }
            }


        }
        else{
            return@coroutineScope null
        }
    }

}