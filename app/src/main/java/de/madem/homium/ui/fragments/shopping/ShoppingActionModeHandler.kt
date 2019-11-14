package de.madem.homium.ui.fragments.shopping

import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.cardview.widget.CardView
import de.madem.homium.models.ShoppingItem

class ShoppingActionModeHandler : ActionMode.Callback {

    private var selectedItems: MutableSet<ShoppingItem> = mutableSetOf()
    private var selectedViews: MutableSet<View> = mutableSetOf()
    private var multiSelect: Boolean = false

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
        multiSelect = true

        menu.add("Delete")
        menu.add("Edit")
        menu.add("Check")
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        multiSelect = false
        selectedItems.clear()

        selectedViews.forEach { it.deselect() }
        selectedViews.clear()
    }

    fun selectItemIfMultisectActive(item: ShoppingItem, view: View) {
        println("Multisect $multiSelect")
        if (multiSelect) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
                selectedViews.remove(view)
                view.deselect()
            } else {
                selectedItems.add(item)
                selectedViews.add(view)
                view.select()
            }
        }
    }

    private fun View.select() = setBackgroundColor(Color.LTGRAY)
    private fun View.deselect() = setBackgroundColor(Color.WHITE)
}