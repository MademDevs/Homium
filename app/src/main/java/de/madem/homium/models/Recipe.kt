package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "recipe")
data class Recipe(
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "picture") var image: String,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)