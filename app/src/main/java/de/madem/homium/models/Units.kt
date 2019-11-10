package de.madem.homium.models

import android.content.Context
import de.madem.homium.R

enum class Units(val ressourceId : Int) {

    //values
    ITEM(R.string.data_units_item),
    GRAM(R.string.data_units_gram),
    KILOGRAM(R.string.data_units_kilogram),
    MILLILITRE(R.string.data_units_millilitre),
    LITRE(R.string.data_units_litre),
    PACK(R.string.data_units_pack);

    //companion
    companion object{

        fun stringValueArray(context: Context) : Array<String>{
            return values().map { it.getString(context) }.toTypedArray()
        }

    }

    //functions
    fun getString(context: Context) : String {
        return context.resources.getString(ressourceId)
    }

}