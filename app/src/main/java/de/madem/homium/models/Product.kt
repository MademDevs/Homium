package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "product")
data class Product(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "defaultUnit") val unit: String,
    @ColumnInfo(name = "defaultAmount") val amount: String,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)