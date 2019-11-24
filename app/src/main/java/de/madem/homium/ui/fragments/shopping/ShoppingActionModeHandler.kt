package de.madem.homium.ui.fragments.shopping

import android.content.Context
import android.graphics.Color
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.models.ShoppingItem

class ShoppingActionModeHandler(val context : Context) : ActionMode.Callback {

    //private fields
    private var selectedItems: MutableSet<ShoppingItem> = mutableSetOf()
    private var selectedViews: MutableSet<View> = mutableSetOf()

    var multiSelectActive: Boolean = false

    //utility fields
    private val menuInflater = MenuInflater(context)
    private lateinit var menu : Menu

    var clickEditButtonHandler: (ShoppingItem) -> Unit = {}
    var clickDeleteButtonHandler: (Set<ShoppingItem>, Set<View>) -> Unit = { _, _ ->

    }

    //functions
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shopping_item_am_btn_edit -> {
                clickEditButtonHandler.invoke(selectedItems.first())
                return true
            }
            R.id.shopping_item_am_btn_delete -> {
                clickDeleteButtonHandler.invoke(selectedItems, selectedViews)
                return true
            }
        }
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
        multiSelectActive = true
        menuInflater.inflate(R.menu.shopping_fragment_actionmode,menu)
        this.menu = menu
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        multiSelectActive = false
        selectedItems.clear()

        selectedViews.forEach { it.deselect() }
        selectedViews.clear()
    }

    fun selectItemIfMultiSelectActive(item: ShoppingItem, view: View) {
        if (multiSelectActive) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
                selectedViews.remove(view)
                view.deselect()
            } else {
                selectedItems.add(item)
                selectedViews.add(view)
                view.select()
            }

            //only visible if maximum one item is selected
            menu.findItem(R.id.shopping_item_am_btn_edit).isVisible = selectedViews.size == 1
        }
    }

    fun countSelected(): Int{
        return if(selectedViews.size == selectedItems.size) selectedItems.size else -1
    }

    private fun View.select() = setBackgroundColor(Color.LTGRAY)
    private fun View.deselect() = setBackgroundColor(Color.WHITE)
}