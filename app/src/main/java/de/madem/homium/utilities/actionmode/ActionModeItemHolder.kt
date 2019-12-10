package de.madem.homium.utilities.actionmode

import android.view.View

abstract class ActionModeItemHolder {

    abstract val itemView: View

    abstract fun equalsImpl(other: ActionModeItemHolder): Boolean
    abstract fun hashCodeProvider(): Int

    override fun equals(other: Any?): Boolean {
        if (other is ActionModeItemHolder) {
            equalsImpl(other)
        }
        return false
    }

    override fun hashCode(): Int {
        return hashCodeProvider()
    }

}