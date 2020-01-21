package de.madem.homium.utilities

import android.graphics.Color
import de.madem.homium.application.HomiumApplication
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units

enum class InventoryItemAmountClassifier(val order: Int, val color: Int) {

    LESS(1, Color.RED),
    MEDIUM(2, Color.YELLOW),
    MANY(3, Color.GREEN),
    UNDEFINED(4, Color.GRAY);

    companion object {

        fun byInventoryItem(inventoryItem: InventoryItem): InventoryItemAmountClassifier {
            val unit = Units.getUnitForText(HomiumApplication.appContext!!, inventoryItem.unit)
                ?: return UNDEFINED

            val min = unit.bounds.first
            val max = unit.bounds.second

            return when {
                inventoryItem.count < min -> LESS
                inventoryItem.count < max -> MEDIUM
                else -> MANY
            }

        }
    }
}