package de.madem.homium.ui.fragments.shopping

import android.content.Context
import android.graphics.Color
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import de.madem.homium.R
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.ShoppingItem

class ShoppingActionModeHandler(val context : Context) : ActionMode.Callback {

    private val appCompatActivity = context as AppCompatActivity
    private var actionMode: ActionMode? = null

    //private fields
    private var selectedItems: MutableList<ShoppingItem> = mutableListOf()
    private var selectedViewHolders: MutableList<ShoppingItemListAdapter.ShoppingItemViewHolder> = mutableListOf()

    //utility fields
    private val menuInflater = MenuInflater(context)
    private lateinit var menu : Menu

    var clickEditButtonHandler: (ShoppingItem) -> Unit = {}
    var clickDeleteButtonHandler: (List<ShoppingItem>, List<ShoppingItemListAdapter.ShoppingItemViewHolder>) -> Unit = { _, _ ->
    }
    var clickCheckButtonHandler: (List<ShoppingItem>, List<ShoppingItemListAdapter.ShoppingItemViewHolder>) -> Unit = { _, _ ->

    }

    fun startActionMode() {
        actionMode = appCompatActivity.startSupportActionMode(this)!!.apply {
            setTitle(R.string.screentitle_main_actionmode_shopping)
        }
    }

    fun finishActionMode() {
        actionMode?.finish()
        actionMode = null
    }

    fun isActionModeActive() = actionMode != null

    //functions
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shopping_item_am_btn_edit -> {
                clickEditButtonHandler.invoke(selectedItems.first())
                return true
            }
            R.id.shopping_item_am_btn_delete -> {
                clickDeleteButtonHandler.invoke(selectedItems, selectedViewHolders)
                return true
            }
            R.id.shopping_item_am_btn_check -> {
                clickCheckButtonHandler.invoke(selectedItems, selectedViewHolders)
                return true
            }
        }
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shopping_fragment_actionmode,menu)
        this.menu = menu
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        selectedItems.clear()

        selectedViewHolders.forEach { it.itemView.deselect() }
        selectedViewHolders.clear()
    }

    fun clickItem(item: ShoppingItem, viewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
            selectedViewHolders.remove(viewHolder)

            viewHolder.itemView.deselect()
        } else {
            selectedItems.add(item)
            selectedViewHolders.add(viewHolder)

            viewHolder.itemView.select()
        }

        //only visible if maximum one item is selected
        menu.findItem(R.id.shopping_item_am_btn_edit).isVisible = selectedViewHolders.size == 1

        //finish action mode if none selected
        if (countSelected() == 0) {
            finishActionMode()
        }

    }

    private fun countSelected(): Int{
        return if(selectedViewHolders.size == selectedItems.size) selectedItems.size else -1
    }

    private fun View.select() = setBackgroundColor(Color.LTGRAY)
    private fun View.deselect() = setBackgroundColor(Color.WHITE)
}