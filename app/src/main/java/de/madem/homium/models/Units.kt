package de.madem.homium.models

import android.content.Context
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication

enum class Units(val resourceId : Int, val shortCut : String, val bounds : Pair<Int,Int>) {

    //values
    ITEM(R.string.data_units_item,"",Pair(10,30)),
    GRAM(R.string.data_units_gram,"g",Pair(100,1000)),
    KILOGRAM(R.string.data_units_kilogram,"kg",Pair(2,5)),
    MILLILITRE(R.string.data_units_millilitre,"ml",Pair(100,1000)),
    LITRE(R.string.data_units_litre,"l",Pair(2,4)),
    PACK(R.string.data_units_pack,"",Pair(4,10));

    //companion
    companion object{

        fun stringValueArray(context: Context = HomiumApplication.appContext!!) : Array<String>{
            return values().map { it.getString(context) }.toTypedArray()
        }

        fun convertibleUnits() : List<String>{
            return values().filter{ it.isConvertible() }.map { it.getString() }.toList()
        }

        fun stringValueMap() : Map<Units,String>{
            return values().toList().associateWith { it.getString() }
        }

        fun unitOf(str: String) : Units?{
            values().forEach {
                if(it.getString() == str){
                    return it
                }
            }

            return null
        }

        fun asSpeechRecognitionPattern() : String{
            return values().map {
                val str = it.getString()
                val shortcutExists = it.shortCut.isNotEmpty()

                //creating regex string via string operation
                //goal is to achieve like e.g. ([mM]{1}illiliter)(ml)
                if(it == PACK){
                    "${str.replaceFirst(str.first().toString(),"[${str.first().toLowerCase()}${str.first().toUpperCase()}]{1}")}(en)?${if(shortcutExists) "|"+it.shortCut else ""}"
                }
                else if(it == KILOGRAM){
                    "${str.replaceFirst(str.first().toString(),"[${str.first().toLowerCase()}${str.first().toUpperCase()}]{1}").removeSuffix("gramm")}(gramm)?${if(shortcutExists) "|"+it.shortCut else ""}"
                }
                else{
                    "${str.replaceFirst(str.first().toString(),"[${str.first().toLowerCase()}${str.first().toUpperCase()}]{1}")}${if(shortcutExists) "|"+it.shortCut else ""}"
                }
            }.joinToString("|")
        }

        fun stringShortcuts(): List<String>{
            return values().map { it.shortCut }
        }

        fun shortCutToLongValue(shortcut : String, context: Context = HomiumApplication.appContext!!) : String{
            values().forEach {
                val longVersion = it.getString(context)

                if((it.shortCut == shortcut.toLowerCase()) || (longVersion.toLowerCase() == shortcut.toLowerCase())) {
                    return  longVersion
                }
            }

            return ""
        }

        fun getUnitForText(context: Context, text: String): Units? {
            return values().firstOrNull { context.getString(it.resourceId) == text }
        }

    }

    //functions
    fun getString(context: Context = HomiumApplication.appContext!!) : String {
        return context.resources.getString(resourceId)
    }

    fun isConvertible() : Boolean{
        return if(this == PACK || this == ITEM){
            false
        }
        else{
            return true
        }
    }



}