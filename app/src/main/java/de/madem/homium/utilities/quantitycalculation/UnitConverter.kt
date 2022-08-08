package de.madem.homium.utilities.quantitycalculation

import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units

class UnitConverter {

    //fields
    private val unitStringMap = Units.stringValueMap()


    //functions
    fun convertUnit(oldQuantity: Pair<Int,String>,newUnit: Units) : Pair<Int,String>{
        val convertibleUnits = Units.convertibleUnits()
        val oldUnit = oldQuantity.second

        return if(convertibleUnits.contains(oldUnit)){
            when(newUnit){
                Units.KILOGRAM -> convertToKilogram(oldQuantity)
                Units.LITRE -> convertToLitre(oldQuantity)
                Units.GRAM -> convertToGram(oldQuantity)
                Units.MILLILITRE -> convertToMilliLitre(oldQuantity)
                else -> oldQuantity
            }
        }
        else{
             oldQuantity
        }

    }

    // Be careful with using this method in context of databases. It returns an exact copy of the inventory item (even id) with changed count and unit
    fun convertUnitOf(item : InventoryItem, newUnit: Units) : InventoryItem{
        val convertResult = convertUnit(Pair(item.count,item.unit),newUnit)

        return InventoryItem(item.name,convertResult.first,convertResult.second,item.location,item.uid)
    }

    //private functions
    private fun convertToKilogram(oldQuantity: Pair<Int,String>) : Pair<Int,String>{
        val kilo = unitStringMap[Units.KILOGRAM] ?: Units.KILOGRAM.getString()

        return when(oldQuantity.second){
            unitStringMap[Units.GRAM] -> Pair(oldQuantity.first/1000,kilo)
            unitStringMap[Units.MILLILITRE] -> Pair(oldQuantity.first/1000,kilo)
            unitStringMap[Units.LITRE] -> Pair(oldQuantity.first,kilo)
            else -> oldQuantity
        }
    }

    private fun convertToLitre(oldQuantity: Pair<Int,String>) : Pair<Int,String>{
        val litre = unitStringMap[Units.LITRE] ?: Units.LITRE.getString()

        return when(oldQuantity.second){
            unitStringMap[Units.GRAM] -> Pair(oldQuantity.first/1000,litre)
            unitStringMap[Units.MILLILITRE] -> Pair(oldQuantity.first/1000,litre)
            unitStringMap[Units.KILOGRAM] -> Pair(oldQuantity.first,litre)
            else -> oldQuantity
        }
    }

    private fun convertToGram(oldQuantity: Pair<Int,String>) : Pair<Int,String>{
        val gram = unitStringMap[Units.GRAM] ?: Units.GRAM.getString()

        return when(oldQuantity.second){
            unitStringMap[Units.MILLILITRE] -> Pair(oldQuantity.first,gram)
            unitStringMap[Units.LITRE] -> Pair(oldQuantity.first*1000,gram)
            unitStringMap[Units.KILOGRAM] -> Pair(oldQuantity.first*1000,gram)
            else -> oldQuantity
        }
    }

    private fun convertToMilliLitre(oldQuantity: Pair<Int,String>) : Pair<Int,String>{
        val ml = unitStringMap[Units.MILLILITRE] ?: Units.MILLILITRE.getString()

        return when(oldQuantity.second){
            unitStringMap[Units.GRAM] -> Pair(oldQuantity.first,ml)
            unitStringMap[Units.LITRE] -> Pair(oldQuantity.first*1000,ml)
            unitStringMap[Units.KILOGRAM] -> Pair(oldQuantity.first*1000,ml)
            else -> oldQuantity
        }
    }
}