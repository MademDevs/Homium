package de.madem.homium.speech.commandparser

import android.content.Context
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units
import de.madem.homium.utilities.capitalizeEachWord
import java.lang.ref.WeakReference

class InventoryCommandParser(val contextRef :WeakReference<Context>) {

    companion object{
        private const val fridgeConst = "Kühlschrank"
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

        val defaultLocation = contextRef.get()?.resources?.getString(R.string.assistent_const_inventory_default_location)
            ?: HomiumApplication.appContext?.resources?.getString(R.string.assistent_const_inventory_default_location)
            ?: "Kühlschrank"


        return InventoryItem(name,count,Units.shortCutToLongValue(unit), defaultLocation )


    }
}