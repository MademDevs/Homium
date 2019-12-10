package de.madem.homium.ui.fragments.shopping

import android.content.Context
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import de.madem.homium.R
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.actionmode.ActionModeHandler
import de.madem.homium.utilities.actionmode.ActionModeInterface
import de.madem.homium.utilities.actionmode.ActionModeItemHolder
import de.madem.homium.utilities.showToastShort

class ShoppingActionModeHandler(val context: Context) : ActionModeInterface<ShoppingActionModeHandler.ItemHolder> {

    var clickEditButtonHandler: (ItemHolder) -> Unit = {}
    var clickDeleteButtonHandler: (List<ItemHolder>) -> Unit = { _ -> }
    var clickCheckButtonHandler: (List<ItemHolder>) -> Unit = { _ -> }

    private var actionModeHandler: ActionModeHandler<ItemHolder> = ActionModeHandler(
        context,
        titleResource = R.string.screentitle_main_actionmode_shopping,
        menuResource = R.menu.shopping_fragment_actionmode
    )

    init {

    }




    class ItemHolder(
        val shoppingItem: ShoppingItem,
        val adapterViewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder
    ) : ActionModeItemHolder() {
        override val itemView = adapterViewHolder.itemView
    }

    fun clickItem(
        shoppingItem: ShoppingItem,
        viewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder
    ) {
        clickItem(ItemHolder(shoppingItem, viewHolder))
    }

    override fun onActionItemClicked(item: MenuItem): Boolean {
        context.showToastShort("new")
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