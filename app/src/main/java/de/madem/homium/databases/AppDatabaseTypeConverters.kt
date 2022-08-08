package de.madem.homium.databases

import androidx.room.TypeConverter
import de.madem.homium.models.Units

class AppDatabaseTypeConverters {
    @TypeConverter
    fun unitsToString(units: Units) : String = units.toString()

    @TypeConverter
    fun stringToUnits(str: String) : Units {
        return try {
            enumValueOf(str)
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            Units.default
        }
    }
}