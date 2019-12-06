package de.madem.homium.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients",
        foreignKeys = arrayOf(ForeignKey(
            entity = Recipe::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("uuid"),
            onDelete = ForeignKey.CASCADE))
        )
data class Ingredients(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "unit") val unit: String,
    @PrimaryKey(autoGenerate = true) val uuid: Int = 0
)