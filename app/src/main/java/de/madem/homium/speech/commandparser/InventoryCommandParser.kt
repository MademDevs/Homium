package de.madem.homium.speech.commandparser

import android.content.Context
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Product
import de.madem.homium.models.Units
import de.madem.homium.utilities.capitalizeEachWord
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.ref.WeakReference

class InventoryCommandParser(private val contextRef :WeakReference<Context>) {

    companion object{
        private const val fridgeConst = "Kühlschrank"
    }

    fun parseInventory(args: List<String>): InventoryItem?{
        //name
        val name = args[2].trim().capitalizeEachWord()

        if(name.isEmpty() || name.isBlank()){
            return null
        }

        //count
        val count = args[0].toIntOrNull() ?: return null

        //unit
        var unit : String = args[1].trim().capitalize()

        if(unit == "Kilo"){
            unit = contextRef.get()?.resources?.getString(R.string.data_units_kilogram) ?: "Kilogramm"
        }


        if(unit.contains(Units.PACK.getString())){
            unit = unit.replace("en","")
        }
        else if (!(Units.stringShortcuts().contains(unit.toLowerCase()) || Units.stringValueArray().contains(unit))){
            return null
        }

        //location
        val location = args[3].trim().capitalize().takeIf { it.isNotEmpty() && it.isNotBlank() } ?: getInventoryDefaultLocation()

        return InventoryItem(name,count, Units.shortCutToLongValue(unit), location)
    }

    suspend fun parseInventoryWithoutUnit(args : List<String>) : InventoryItem? = coroutineScope{
        //getting pseudo inventory item
        val args3 = args.slice(0..1)
        val pseudoItem : InventoryItem = parseInventoryWithoutLocationUnit(args.slice(0..1))
            ?: return@coroutineScope null

        //getting location
        return@coroutineScope if(args[2].isEmpty() || args[2].isBlank()) pseudoItem
        else InventoryItem(pseudoItem.name,pseudoItem.count,pseudoItem.unit,args[2].trim().capitalize())

    }

    suspend fun parserInventoryWithoutUnitCount(args : List<String>) : InventoryItem? = coroutineScope {
        //getting pseudo inventory item
        val pseudoItem : InventoryItem = parseInventoryWithoutLocationUnitCount(args[0])
            ?: return@coroutineScope null

        return@coroutineScope if(args[1].isEmpty() || args[1].isBlank()) pseudoItem
        else InventoryItem(pseudoItem.name,pseudoItem.count,pseudoItem.unit,args[1].trim().capitalize())
    }

    fun parseInventoryWithoutLocation(args : List<String>) : InventoryItem?{

        //name
        val name = args[2].trim().capitalizeEachWord().takeIf { it.isNotEmpty() && it.isNotBlank() }
            ?: return null

        //count
        val count = args[0].toIntOrNull() ?: return null

        //unit
        var unit : String = args[1].trim().capitalize()

        if(unit == "Kilo"){
            unit = contextRef.get()?.resources?.getString(R.string.data_units_kilogram) ?: "Kilogramm"
        }


        if(unit.contains(Units.PACK.getString())){
            unit = unit.replace("en","")
        }
        else if (!(Units.stringShortcuts().contains(unit.toLowerCase()) || Units.stringValueArray().contains(unit))){
            return null
        }

        val defaultLocation = getInventoryDefaultLocation()


        return InventoryItem(name,count,Units.shortCutToLongValue(unit), defaultLocation )
    }

    suspend fun parseInventoryWithoutLocationUnit(args : List<String>) : InventoryItem? = coroutineScope{

        if(args.size < 2){
            return@coroutineScope null
        }

        //name
        val name = args[1].trim().capitalizeEachWord()

        val matchingProductsDeffered = async<List<Product>> {
            AppDatabase.getInstance().itemDao().getProductsByNameOrPlural(name)
        }


        //quantity
        val amount = args[0].trim().toIntOrNull()


        if(name.isNotEmpty() && name.isNotBlank() && amount != null){

            val productResult = matchingProductsDeffered.await()


            if(productResult.isEmpty()){
                return@coroutineScope InventoryItem(name,amount,Units.ITEM.getString(),getInventoryDefaultLocation())
            }
            else{
                val productNameIndex = productResult.map { it.name }.indexOf(name)
                val productPluralNameIndex = productResult.map { it.plural }.indexOf(name)
                if(productNameIndex >= 0 || productPluralNameIndex >= 0) {
                    val newUnit = if(productPluralNameIndex < 0) productResult[productNameIndex].unit else productResult[productPluralNameIndex].unit
                    return@coroutineScope InventoryItem(name,amount,newUnit,getInventoryDefaultLocation())
                }
                else{
                    return@coroutineScope InventoryItem(name,amount,Units.ITEM.getString(),getInventoryDefaultLocation())
                }
            }
        }
        else{
            return@coroutineScope null
        }

    }

    suspend fun parseInventoryWithoutLocationUnitCount(nameIn : String) : InventoryItem? = coroutineScope {
        val productsDeffered = async<List<Product>> {
            AppDatabase.getInstance().itemDao().getProductsByNameOrPlural(nameIn.capitalize())
        }

        if(nameIn.isNotBlank() && nameIn.isNotEmpty()){

            val name = nameIn.capitalizeEachWord()

            val productResult = productsDeffered.await()

            val productNameIndex = productResult.map { it.name }.indexOf(name)
            val productPluralIndex = productResult.map { it.plural }.indexOf(name)
            return@coroutineScope when {
                productNameIndex >= 0 -> {
                    val newUnit = productResult[productNameIndex].unit
                    val newQuantity = productResult[productNameIndex].amount.toIntOrNull() ?: 1
                    InventoryItem(name,newQuantity,newUnit,getInventoryDefaultLocation())
                }
                productPluralIndex >= 0 -> {
                    val newUnit = productResult[productPluralIndex].unit
                    val newQuantity = productResult[productPluralIndex].amount.toIntOrNull()?.takeIf { it > 1 } ?: 2
                    InventoryItem(name,newQuantity,newUnit,getInventoryDefaultLocation())

                }
                else -> {
                    InventoryItem(name,1,Units.ITEM.getString(),getInventoryDefaultLocation())
                }
            }


        }
        else{
            return@coroutineScope null
        }
    }

    //help functions
    private fun getInventoryDefaultLocation() : String{
        return contextRef.get()?.resources?.getString(R.string.assistent_const_inventory_default_location)
            ?: HomiumApplication.appContext?.resources?.getString(R.string.assistent_const_inventory_default_location)
            ?: "Kühlschrank"
    }

}