package de.madem.homium.ui.fragments.inventory

import android.content.Context
import android.view.MenuItem
import de.madem.homium.R
import de.madem.homium.managers.adapters.InventoryItemListAdapter
import de.madem.homium.models.InventoryItem
import de.madem.homium.utilities.actionmode.ActionModeHandler
import de.madem.homium.utilities.actionmode.ActionModeItemHolder

class InventoryActionModeHandler(context: Context) : ActionModeHandler<InventoryActionModeHandler.ItemHolder>(context) {

    //protected properties
    override val actionModeSettings: ActionModeSettings
        get() = ActionModeSettings(
            titleResource = R.string.screentitle_main_actionmode_inventory,
            menuResource = R.menu.inventory_fragment_actionmode
        )


    //public api properties
    var clickDeleteButtonHandler: (Collection<ItemHolder>) -> Unit = { _ -> }


    //protected functions
    override fun onMenuItemClicked(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.inventory_item_am_btn_delete -> {
                clickDeleteButtonHandler.invoke(selectedItems)
                return true
            }
        }
        return false
    }

    //api methods
    fun clickItem(
        inventoryItem: InventoryItem,
        adapterViewHolder: InventoryItemListAdapter.InventoryItemViewHolder
    ) {
        val itemHolder = ItemHolder.of(inventoryItem, adapterViewHolder)
        clickItem(itemHolder)
    }


    //item holder class. Init only via ItemHolder.of()
    class ItemHolder private constructor(
        val inventoryItem: InventoryItem,
        adapterViewHolder: InventoryItemListAdapter.InventoryItemViewHolder
    ) : ActionModeItemHolder() {

        //super properties
        override val itemView = adapterViewHolder.itemView

        //super implementations
        override fun equalsImpl(other: ActionModeItemHolder): Boolean {
            return if (other is ItemHolder) {
                inventoryItem.uid == other.inventoryItem.uid
            } else false
        }
        override fun hashCodeProvider(): Int = inventoryItem.uid

        //static method for initialization
        companion object {
            fun of(
                inventoryItem: InventoryItem,
                adapterViewHolder: InventoryItemListAdapter.InventoryItemViewHolder
            ) = ItemHolder(inventoryItem, adapterViewHolder)
        }
    }
}