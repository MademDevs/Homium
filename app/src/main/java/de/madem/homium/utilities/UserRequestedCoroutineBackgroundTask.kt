package de.madem.homium.utilities

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.WeakReference

class UserRequestedCoroutineBackgroundTask<T>(private val contextRef : WeakReference<Context>, private val message: String, private val yes: String = HomiumApplication.appContext!!.resources.getString(R.string.answer_yes), private val no: String = HomiumApplication.appContext!!.resources.getString(R.string.answer_no)) : CoroutineBackgroundTask<T>() {

    //secondary constructor
    constructor(contextRef: WeakReference<Context>, msgRes : Int) : this(contextRef,HomiumApplication.appContext!!.resources.getString(msgRes))

    //functions
    override fun start() {
        contextRef.get().notNull {
            AlertDialog.Builder(it)
                .setMessage(message)
                .setPositiveButton(yes) { dialog, _ ->
                    super.start()
                }
                .setNegativeButton(no) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }


    }

    override fun startInCoroutineScope(scope: CoroutineScope) {

        contextRef.get().notNull {
            AlertDialog.Builder(it)
                .setMessage(message)
                .setPositiveButton(yes) { dialog, _ ->
                    super.startInCoroutineScope(scope)
                }
                .setNegativeButton(no) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

    }
}