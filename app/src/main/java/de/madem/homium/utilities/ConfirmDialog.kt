package de.madem.homium.utilities

import android.app.AlertDialog
import android.content.DialogInterface
import de.madem.homium.application.HomiumApplication

class ConfirmDialog(messageResource: Int, onConfirm: (DialogInterface) -> Unit) {

    private val context = HomiumApplication.appContext
    private val dialogBuilder = AlertDialog.Builder(context)

    init {
        dialogBuilder
            .setMessage(messageResource)
            .setPositiveButton(android.R.string.yes) { dialog, which ->
                onConfirm.invoke(dialog)
            }
            .setNegativeButton(android.R.string.no) { dialog, which ->
                dialog.dismiss()
            }
    }

    companion object {
        fun show(messageResource: Int, onConfirm: (DialogInterface) -> Unit) {
            ConfirmDialog(messageResource, onConfirm).show()
        }
    }

    private fun show() {
        dialogBuilder.show()
    }

}