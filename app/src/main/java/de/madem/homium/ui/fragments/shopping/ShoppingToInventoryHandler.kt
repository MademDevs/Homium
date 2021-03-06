package de.madem.homium.ui.fragments.shopping

import android.app.AlertDialog
import android.content.Context
import de.madem.homium.R
import de.madem.homium.application.HomiumSettings
import de.madem.homium.constants.SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_TO_INVENTORY
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.extensions.getSetting
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ShoppingToInventoryHandler(private val context: Context) {

    private fun getCheckedRadio(): Int {
        var checkedRadioId: Int = HomiumSettings.shoppingToInventory/*context.getSetting(
            SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_TO_INVENTORY,
            Int::class
        ) ?: 0*/

        if (checkedRadioId == 0) {
            checkedRadioId = R.id.radio_check_question
        }

        return checkedRadioId
    }


    fun handleShoppingItems(shoppingCart: List<ShoppingItem>, callback: () -> Unit) {
        when (getCheckedRadio()) {
            R.id.radio_check_always -> {
                putShoppingItemsIntoInventory(shoppingCart)
                callback.invoke()
            }
            R.id.radio_check_question -> openQuestionAlertDialog(shoppingCart, callback)
            else -> {
                callback.invoke()
            }
        }
    }

    private fun openQuestionAlertDialog(shoppingCart: List<ShoppingItem>, callback: () -> Unit) {
        AlertDialog.Builder(context)
            .setMessage(R.string.setting_shopping_check_behaviour_question_alert_message)
            .setPositiveButton(R.string.answer_yes) { dialog, _ ->
                putShoppingItemsIntoInventory(shoppingCart)
                dialog.dismiss()
                callback.invoke()
            }
            .setNegativeButton(R.string.answer_no) { dialog, _ ->
                dialog.dismiss()
                callback.invoke()
            }.show()
    }

    private fun putShoppingItemsIntoInventory(shoppingCart: List<ShoppingItem>) {
        GlobalScope.launch(IO) {
            val inventoryDao = AppDatabase.getInstance().inventoryDao()
            val currentInventoryItems = inventoryDao.fetchAllInventoryItems()

            shoppingCart.forEach { shoppingItem ->

                val inventoryItem = currentInventoryItems.firstOrNull { inventoryItem ->
                    inventoryItem.name.equals(shoppingItem.name, true) &&
                            inventoryItem.unit == shoppingItem.unit
                }

                if (inventoryItem == null) {
                    //create new inventory item
                    with(shoppingItem) {
                        inventoryDao.insertInventoryItems(
                            InventoryItem(name, count, unit, "")
                        )
                    }

                } else { //update inventory item count
                    with(inventoryItem) {
                        inventoryDao.updateInventoryItem(
                            uid, name, count + shoppingItem.count, unit, location
                        )
                    }
                }
            }
        }
    }

}