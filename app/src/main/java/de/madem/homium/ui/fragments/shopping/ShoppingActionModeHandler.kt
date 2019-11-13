package de.madem.homium.ui.fragments.shopping

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode

class ShoppingActionModeHandler : ActionMode.Callback {



    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
        menu.add("Delete")
        menu.add("Edit")
        menu.add("Check")
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {

    }
}