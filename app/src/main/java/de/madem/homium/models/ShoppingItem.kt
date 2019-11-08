package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "shoppingItem")
data class ShoppingItem(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "unit") val unit: String = "",
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)