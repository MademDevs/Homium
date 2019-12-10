package de.madem.homium.ui.fragments.shopping

import android.content.Context
import android.view.MenuItem
import de.madem.homium.R
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.actionmode.ActionModeHandler
import de.madem.homium.utilities.actionmode.ActionModeItemHolder

class ShoppingActionModeHandler(
    context: Context
) : ActionModeHandler<ShoppingActionModeHandler.ItemHolder>(context) {

    //protected properties
    override val actionModeSettings: ActionModeSettings
        get() = ActionModeSettings(
            titleResource = R.string.screentitle_main_actionmode_shopping,
            menuResource = R.menu.shopping_fragment_actionmode
        )


    //public api properties
    var clickEditButtonHandler: (ItemHolder) -> Unit = {}
    var clickDeleteButtonHandler: (Collection<ItemHolder>) -> Unit = { _ -> }
    var clickCheckButtonHandler: (Collection<ItemHolder>) -> Unit = { _ -> }


    //protected functions
    override fun onMenuItemClicked(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.shopping_item_am_btn_edit -> {
                clickEditButtonHandler.invoke(selectedItems.first())
                return true
            }
            R.id.shopping_item_am_btn_delete -> {
                clickDeleteButtonHandler.invoke(selectedItems)
                return true
            }
            R.id.shopping_item_am_btn_check -> {
                clickCheckButtonHandler.invoke(selectedItems)
                return true
            }
        }
        return false
    }


    //api methods
    fun clickItem(
        shoppingItem: ShoppingItem,
        adapterViewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder
    ) {
        val itemHolder = ItemHolder.of(shoppingItem, adapterViewHolder)

        clickItem(itemHolder)

        //only visible if maximum one item is selected
        menu?.findItem(R.id.shopping_item_am_btn_edit)?.isVisible = selectedItems.size == 1
    }


    //item holder class. Init only via ItemHolder.of()
    class ItemHolder private constructor(
        val shoppingItem: ShoppingItem,
        val adapterViewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder
    ) : ActionModeItemHolder() {

        //super properties
        override val itemView = adapterViewHolder.itemView

        //super implementations
        override fun equalsImpl(other: ActionModeItemHolder): Boolean {
            return if (other is ItemHolder) {
                shoppingItem.uid == other.shoppingItem.uid
            } else false
        }
        override fun hashCodeProvider(): Int = shoppingItem.uid

        //static method for initialization
        companion object {
            fun of(
                shoppingItem: ShoppingItem,
                adapterViewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder
            ) = ItemHolder(shoppingItem, adapterViewHolder)
        }
    }
}