package de.madem.homium.utilities.actionmode

import android.content.Context
import android.graphics.Color
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import de.madem.homium.utilities.showToastShort

class ActionModeHandler<ItemHolder : ActionModeItemHolder>(
    val context: Context, val titleResource: Int, val menuResource: Int
) : ActionMode.Callback {

    private val appCompatActivity = context as AppCompatActivity
    private var actionMode: ActionMode? = null

    //utility fields
    private val menuInflater = MenuInflater(context)
    lateinit var menu: Menu

    var selectedItems: MutableList<ItemHolder> = mutableListOf()
    var onStartActionMode = listOf<() -> Unit>()
    var onStopActionMode = listOf<() -> Unit>()

    var onItemSelected = { item: MenuItem -> false }

    fun startActionMode() {
        actionMode = appCompatActivity.startSupportActionMode(this)!!.apply {
            setTitle(titleResource)
        }
    }

    fun finishActionMode() {
        actionMode?.finish()
    }

    fun isActionModeActive() = actionMode != null

    //functions


    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return onItemSelected()
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
        menuInflater.inflate(menuResource, menu)
        this.menu = menu
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        onStartActionMode.forEach { it() }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        selectedItems.forEach { it.itemView.deselect() }
        selectedItems.clear()

        actionMode = null
        onStopActionMode.forEach { it() }
    }


    fun clickItem(itemHolder: ItemHolder) {
        if (selectedItems.contains(itemHolder)) {
            selectedItems.remove(itemHolder)

            itemHolder.itemView.deselect()
        } else {
            selectedItems.add(itemHolder)

            itemHolder.itemView.select()
        }

        //finish action mode if none selected
        if (selectedItems.size == 0) {
            finishActionMode()
        }
    }

    private fun View.select() = setBackgroundColor(Color.LTGRAY)
    private fun View.deselect() = setBackgroundColor(Color.WHITE)

}