package de.madem.homium.speech

import android.content.Context
import androidx.core.text.isDigitsOnly
import de.madem.homium.R
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import java.util.*

class CommandParser(val context: Context) {

    fun parseShoppingItem(splittedWords : List<String>) : ShoppingItem?{
        //name
        val name = splittedWords[2].capitalize()
        println("NAME = $name")
        //unit
        var unit : String = splittedWords[1].capitalize()
        println("UNIT = $unit")

        if(unit == "Kilo"){
            unit = context.resources.getString(R.string.data_units_kilogram)
        }

        if(unit.contains(Units.PACK.getString(context))){
            unit = unit.replace("en","")
        }
        else if (!(Units.stringShortcuts().contains(unit.toLowerCase()) || Units.stringValueArray(context).contains(unit))){
            return null
        }

        //quantity
        val quantity = replaceNumberWords(splittedWords[0]).toIntOrNull() ?: return null
        println("QUANTITY = $quantity")
        return ShoppingItem(name,quantity,Units.shortCutToLongValue(unit,context))

    }


    private fun replaceNumberWords(str : String) : String {

        return if(str.isDigitsOnly()){
            str
        }
        else{
            str.replace(Regex("ein(e)*"),"1")
                .replace("zwei","2")
                .replace("drei","3")
                .replace("vier","4")
                .replace("fünf","5")
                .replace("sechs","6")
                .replace("sieben","7")
                .replace("acht","8")
                .replace("neun","9")
                .replace("zehn","10")
                .replace("elf","11")
                .replace("zwölf","12")

        }

    }
}