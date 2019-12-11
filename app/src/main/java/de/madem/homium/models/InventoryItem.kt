package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "inventoryItem")
data class InventoryItem(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "unit") val unit: String,
    @ColumnInfo(name = "location") val location: String,
    @PrimaryKey(autoGenerate = true) val uid: Int = -1
)