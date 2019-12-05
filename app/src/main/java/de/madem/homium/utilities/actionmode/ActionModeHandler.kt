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

class ActionModeHandler<ItemHolder : ActionModeItemHolder>(val context: Context, val titleResource: Int, val menuResource: Int)
    : ActionMode.Callback, ActionModeInterface<ItemHolder> {

    private val appCompatActivity = context as AppCompatActivity
    private var actionMode: ActionMode? = null

    //utility fields
    private val menuInflater = MenuInflater(context)
    override lateinit var menu: Menu

    override var selectedItems: MutableList<ItemHolder> = mutableListOf()
    override var onStartActionMode = listOf<() -> Unit>()
    override var onStopActionMode = listOf<() -> Unit>()

    override fun startActionMode() {
        actionMode = appCompatActivity.startSupportActionMode(this)!!.apply {
            setTitle(titleResource)
        }
    }

    override fun finishActionMode() {
        actionMode?.finish()
    }

    override fun isActionModeActive() = actionMode != null

    //functions
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        context.showToastShort("old")
        return false
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


    override fun clickItem(itemHolder: ItemHolder) {
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