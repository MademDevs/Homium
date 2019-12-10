package de.madem.homium.models

import android.content.Context
import de.madem.homium.R

enum class Units(val ressourceId : Int, val shortCut : String) {

    //values
    ITEM(R.string.data_units_item,""),
    GRAM(R.string.data_units_gram,"g"),
    KILOGRAM(R.string.data_units_kilogram,"kg"),
    MILLILITRE(R.string.data_units_millilitre,"ml"),
    LITRE(R.string.data_units_litre,"l"),
    PACK(R.string.data_units_pack,"");

    //companion
    companion object{

        fun stringValueArray(context: Context) : Array<String>{
            return values().map { it.getString(context) }.toTypedArray()
        }

        fun stringShortcuts(): List<String>{
            return values().map { it.shortCut }
        }

        fun shortCutToLongValue(shortcut : String, context: Context) : String{
            values().forEach {
                val longVersion = it.getString(context)

                if((it.shortCut == shortcut.toLowerCase()) || (longVersion.toLowerCase() == shortcut.toLowerCase())) {
                    return  longVersion
                }
            }

            return ""
        }

    }

    //functions
    fun getString(context: Context) : String {
        return context.resources.getString(ressourceId)
    }



}