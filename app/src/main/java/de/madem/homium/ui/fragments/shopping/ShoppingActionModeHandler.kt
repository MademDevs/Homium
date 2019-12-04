package de.madem.homium.ui.fragments.shopping

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import de.madem.homium.R
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.actionmode.ActionModeHandler
import de.madem.homium.utilities.actionmode.ActionModeInterface
import de.madem.homium.utilities.actionmode.ActionModeItemHolder

class ShoppingActionModeHandler(
    context: Context,
    private val delegate: ActionModeHandler<ItemHolder> = ActionModeHandler(
        context,
        titleResource = R.string.screentitle_main_actionmode_shopping,
        menuResource = R.menu.shopping_fragment_actionmode
    )
) : ActionModeInterface<ShoppingActionModeHandler.ItemHolder> by delegate {

    var clickEditButtonHandler: (ItemHolder) -> Unit = {}
    var clickDeleteButtonHandler: (List<ItemHolder>) -> Unit = { _ -> }
    var clickCheckButtonHandler: (List<ItemHolder>) -> Unit = { _ -> }

    class ItemHolder(
        val shoppingItem: ShoppingItem,
        val adapterViewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder
    ) : ActionModeItemHolder() {

        override val itemView: View
            get() = adapterViewHolder.itemView
    }

    fun clickItem(
        shoppingItem: ShoppingItem,
        viewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder
    ) {
        clickItem(ItemHolder(shoppingItem, viewHolder))
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
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

    override fun clickItem(itemHolder: ItemHolder) {
        delegate.clickItem(itemHolder)

        //only visible if maximum one item is selected
        menu.findItem(R.id.shopping_item_am_btn_edit).isVisible = selectedItems.size == 1
    }

}