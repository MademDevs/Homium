package de.madem.homium.utilities

import android.app.AlertDialog
import android.content.Context

class ConfirmDialog(context: Context, messageResource: Int) {

    private val dialogBuilder = AlertDialog.Builder(context)

    var onConfirm: () -> Unit = {
        //no nothing
    }

    init {
        dialogBuilder
            .setMessage(messageResource)
            .setPositiveButton(android.R.string.yes) { dialog, which ->
                onConfirm.invoke()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.no) { dialog, which ->
                dialog.dismiss()
            }
    }

    companion object {
        fun show(context: Context, messageResource: Int, configuration: ConfirmDialog.() -> Unit) {
            ConfirmDialog(context, messageResource).apply { configuration.invoke(this) }.show()
        }
    }

    private fun show() {
        dialogBuilder.show()
    }

}