package de.madem.homium.utilities.actionmode

import android.content.Context
import android.graphics.Color
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import de.madem.homium.R
import de.madem.homium.utilities.extensions.applyNotNull

abstract class ActionModeHandler<ItemHolder : ActionModeItemHolder>(val context: Context){

    //private members
    private val activity = context as AppCompatActivity
    private var actionMode: ActionMode? = null
    private val menuInflater = MenuInflater(context)
    private var selectedItemMap: MutableMap<Int, ItemHolder> = mutableMapOf()
    protected var menu: Menu? = null

    //abstract members
    protected abstract val actionModeSettings: ActionModeSettings

    //utility fields
    val selectedItems: Collection<ItemHolder>
        get() = selectedItemMap.values

    var onStartActionMode = listOf<() -> Unit>()
    var onStopActionMode = listOf<() -> Unit>()

    //private functions
    private fun View.select() = setBackgroundColor(ContextCompat.getColor(activity, R.color.listCardItemBackgroundSelected))
    private fun View.deselect() = setBackgroundColor(ContextCompat.getColor(activity, R.color.listCardItemBackground))

    //abstract functions
    protected abstract fun onMenuItemClicked(item: MenuItem): Boolean

    //utility functions
    fun startActionMode() {
        val actionModeCallback = ActionModeCallback(this)

        actionMode = activity.startSupportActionMode(actionModeCallback).applyNotNull {
            setTitle(actionModeSettings.titleResource)
        }
    }

    fun finishActionMode() {
        actionMode?.finish()
    }

    fun isActionModeActive() = actionMode != null

    open fun clickItem(itemHolder: ItemHolder) {
        println(itemHolder.hashCode())

        val hashCode = itemHolder.hashCode()

        if (selectedItemMap.containsKey(itemHolder.hashCode())) {
            selectedItemMap.remove(hashCode)

            itemHolder.itemView.deselect()
        } else {
            selectedItemMap[hashCode] = itemHolder

            itemHolder.itemView.select()
        }

        //finish action mode if none selected
        if (selectedItems.size == 0) {
            finishActionMode()
        }
    }

    //action mode settings
    protected data class ActionModeSettings(
        val titleResource: Int,
        val menuResource: Int
    )

    //action mode listener
    private class ActionModeCallback<ItemHolder : ActionModeItemHolder>(
        val amh: ActionModeHandler<ItemHolder>
    ) : ActionMode.Callback {

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = with(amh) {
            return onMenuItemClicked(item)
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean = with(amh) {
            menuInflater.inflate(actionModeSettings.menuResource, menu)
            this.menu = menu
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = with(amh) {
            onStartActionMode.forEach { it() }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) = with(amh) {
            selectedItemMap.forEach { it.value.itemView.deselect() }
            selectedItemMap.clear()

            actionMode = null
            onStopActionMode.forEach { it() }
        }

    }

}