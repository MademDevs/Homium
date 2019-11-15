package de.madem.homium.ui.fragments.shopping

import android.content.Context
import android.graphics.Color
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.cardview.widget.CardView
import de.madem.homium.R
import de.madem.homium.models.ShoppingItem

class ShoppingActionModeHandler(val context : Context) : ActionMode.Callback {

    //private fields
    private var selectedItems: MutableSet<ShoppingItem> = mutableSetOf()
    private var selectedViews: MutableSet<View> = mutableSetOf()
    private var multiSelect: Boolean = false

    //utility fields
    private val menuInflater = MenuInflater(context)
    private lateinit var menu : Menu

    //functions
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
        multiSelect = true
        menuInflater.inflate(R.menu.shopping_fragment_actionmode,menu)
        this.menu = menu

        //menu.add("Delete")
        //menu.add("Edit")
        //menu.add("Check")
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

            if(selectedViews.size == 1){
                menu.findItem(R.id.main_actionmode_shopping_edit).isVisible = true
            }
            else{
                menu.findItem(R.id.main_actionmode_shopping_edit).isVisible = false
            }
        }
    }

    fun countSelected(): Int{
        return if(selectedViews.size == selectedItems.size) selectedItems.size else -1
    }

    private fun View.select() = setBackgroundColor(Color.LTGRAY)
    private fun View.deselect() = setBackgroundColor(Color.WHITE)
}