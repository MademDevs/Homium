package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "recipe")
data class Recipe(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "picture") val image: String,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)