package de.madem.homium.utilities.actionmode

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode

interface ActionModeInterface<ItemHolder : ActionModeItemHolder> {

    var menu: Menu
    var selectedItems: MutableList<ItemHolder>
    var onStartActionMode: List<() -> Unit>
    var onStopActionMode: List<() -> Unit>

    fun isActionModeActive(): Boolean
    fun finishActionMode()
    fun clickItem(itemHolder: ItemHolder)
    fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean
    fun startActionMode()

}