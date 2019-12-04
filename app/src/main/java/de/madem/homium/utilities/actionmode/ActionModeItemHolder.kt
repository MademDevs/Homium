package de.madem.homium.utilities.actionmode

import android.view.View

abstract class ActionModeItemHolder {

    abstract val itemView: View

    override fun equals(other: Any?): Boolean {
        if (other is View) {
            return other == itemView
        }
        return false
    }

    override fun hashCode(): Int {
        return itemView.hashCode()
    }

}