package de.madem.homium.utilities.quantitycalculation

import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units
import de.madem.homium.utilities.InventoryQuantityOperationInformation
import kotlin.math.abs

class InventoryQuantityCalculationOperator(private val unitConverter: UnitConverter = UnitConverter()) {
    //fields
    private val inventoryDao = AppDatabase.getInstance().inventoryDao()
    private val convertibleUnits = Units.convertibleUnits()

    //functions

    //function, that subtracts quantity from a quantity of inventory elements
    fun subractFromInventory(operationInfo: InventoryQuantityOperationInformation){
        var quantity = operationInfo.info.second
        var unit = operationInfo.info.third
        val idList = operationInfo.info.first

        var idIdx = 0;
        while(quantity > 0 && idIdx <= idList.lastIndex){
            //getting current item to subtract from
            val currentItem = inventoryDao.fetchInventoryItemById(idList[idIdx])
            val currentOriginUnit = currentItem.unit

            //do subtraction
            val subtractResult = subtractFromItem(quantity,unit,currentItem)

            //evaluate subtractResult
            if(subtractResult.first == 0) {
                //case that quantity fitted perfect with one element
                inventoryDao.deleteInventoryItemById(currentItem.uid)
                break
            }else if (subtractResult.first < 0){
                //case that more has been subtracted than possible and there is a rest left
                inventoryDao.deleteInventoryItemById(currentItem.uid)
                quantity = abs(subtractResult.first)
                unit = subtractResult.second
            }
            else{
                //case that there is still something left from currentItem
                if(allowedToScaleBack(subtractResult.first,currentOriginUnit)){
                    val convertQuantity = unitConverter.convertUnit(subtractResult,Units.unitOf(currentOriginUnit)!!)
                    inventoryDao.updateQuantityOf(currentItem.uid,convertQuantity.first,convertQuantity.second)
                }
                else{
                    inventoryDao.updateQuantityOf(currentItem.uid,subtractResult.first,subtractResult.second)
                }

                break;
            }

            //increasing loop counter for idlist
            idIdx++

        }
    }

    private fun allowedToScaleBack(count : Int, originUnitStr : String): Boolean {
        val unit = Units.unitOf(originUnitStr) ?: return false

        return (count % 1000 == 0) && (unit.isBigUnit())
    }

    private fun subtractFromItem(quantity : Int,unit : String,currentItem: InventoryItem) : Pair<Int, String>{
        return if(unit == currentItem.unit){
            Pair<Int,String>(currentItem.count-quantity,unit)
        }
        else{
            val convertQuantityInput = unitConverter.convertUnit(Pair(quantity,unit),Units.unitOf(currentItem.unit)!!.getDownscaledUnit())
            val convertInventoryItem = unitConverter.convertUnitOf(currentItem,Units.unitOf(currentItem.unit)!!.getDownscaledUnit())
            Pair(convertInventoryItem.count-convertQuantityInput.first,convertInventoryItem.unit)
        }
    }

}