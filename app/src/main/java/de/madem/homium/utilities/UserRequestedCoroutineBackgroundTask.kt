package de.madem.homium.utilities

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.madem.homium.R
import kotlinx.coroutines.CoroutineScope

class UserRequestedCoroutineBackgroundTask<T>(val context : Context, val message: String, val yes: String = context.resources.getString(R.string.answer_yes), val no: String = context.resources.getString(R.string.answer_no)) : CoroutineBackgroundTask<T>() {

    //secondary constructor
    constructor(context: Context, msgRes : Int) : this(context,context.resources.getString(msgRes))

    //functions
    override fun start() {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(yes) { dialog, _ ->
                super.start()
            }
            .setNegativeButton(no) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun startInCoroutineScope(scope: CoroutineScope) {

        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(yes) { dialog, _ ->
                super.startInCoroutineScope(scope)
            }
            .setNegativeButton(no) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}