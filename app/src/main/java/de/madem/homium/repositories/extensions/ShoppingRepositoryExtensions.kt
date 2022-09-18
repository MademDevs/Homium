package de.madem.homium.repositories.extensions

import de.madem.homium.models.ShoppingItem
import de.madem.homium.repositories.ShoppingRepository
import de.madem.homium.utilities.AppResult

/**
 * This extension function updates the [ShoppingItem.checked]-State by using other Repository-
 * Functions
 * @return An [AppResult] to reflect the Operation-Status
 */
suspend fun ShoppingRepository.updateIsChecked(
    item: ShoppingItem,
    isChecked: Boolean
): AppResult<Unit> {
    if (item.checked == isChecked) {
        return AppResult.Success(Unit)
    }

    return updateShoppingItemById(
        id = item.uid, name = item.name, count = item.count, unit = item.unit, isChecked = isChecked
    )
}