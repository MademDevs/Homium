package de.madem.homium.databases

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.madem.homium.models.Units

class AppDatabaseMigrations(val context: Context) {
    fun all() : Array<Migration> = arrayOf(
        MigrationV1ToV2(context)
    )
}

private class MigrationV1ToV2(private val context: Context) : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //Migrating ShoppingItems and Product Unit values from Res-Strings
        val unitsTitlesToUnits = Units.values().associateBy { context.getString(it.resourceId) }
        database.beginTransaction()
        try {
            migrateUnitsValuesFromResStringToEnumStrings(
                unitsTitlesToUnits, database, "ShoppingItem"
            )
            migrateUnitsValuesFromResStringToEnumStrings(
                unitsTitlesToUnits, database, "Product", "defaultUnit"
            )
            database.setTransactionSuccessful()
        }
        finally {
            database.endTransaction()
        }
    }

    private fun migrateUnitsValuesFromResStringToEnumStrings(
        mapping: Map<String, Units>,
        database: SupportSQLiteDatabase,
        table: String,
        unitPropName: String = "unit"
    ){
        val shoppingIdsAndUnitsCursor = database
            .query("SELECT uid, $unitPropName FROM $table;")
        while (shoppingIdsAndUnitsCursor.moveToNext()) {
            val id = shoppingIdsAndUnitsCursor.getInt(0)
            val unit = shoppingIdsAndUnitsCursor.getString(1)
            val resolvedUnit = mapping[unit] ?: Units.default
            val newUnit = resolvedUnit.toString()
            database.execSQL("UPDATE $table SET $unitPropName = '$newUnit' WHERE uid = $id;")
        }
    }
}
