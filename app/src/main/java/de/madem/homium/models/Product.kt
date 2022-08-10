package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "product")
data class Product(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "pluralName") val plural: String,
    @ColumnInfo(name = "defaultUnit") val unit: Units = Units.default,
    @ColumnInfo(name = "defaultAmount") val amount: String,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)