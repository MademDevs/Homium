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
        //Migrating ShoppingItems Unit values from Res-Strings
        val unitsTitlesToUnits = Units.values().associateBy { context.getString(it.resourceId) }

        database.beginTransaction()
        try {
            val idsAndUnitsCursor = database.query("SELECT uid, unit FROM ShoppingItem;")
            while (idsAndUnitsCursor.moveToNext()) {
                val id = idsAndUnitsCursor.getInt(0)
                val unit = idsAndUnitsCursor.getString(1)
                val resolvedUnit = unitsTitlesToUnits[unit] ?: Units.default
                val newUnit = resolvedUnit.toString()
                database.execSQL("UPDATE ShoppingItem SET unit = '$newUnit' WHERE uid = $id;")
            }
            database.setTransactionSuccessful()
        }
        finally {
            database.endTransaction()
        }
    }
}
