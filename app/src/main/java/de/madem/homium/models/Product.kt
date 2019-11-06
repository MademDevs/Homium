package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "product")
data class Product(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "name") val name: String
)