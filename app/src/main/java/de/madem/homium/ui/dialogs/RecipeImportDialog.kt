package de.madem.homium.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import de.madem.homium.R
import de.madem.homium.utilities.extensions.notNull

class RecipeImportDialog : AppCompatDialogFragment() {
    //GUI variables
    private lateinit var editTextMessage : EditText

    //listeners
    private var recipeImportDialogListener : RecipeImportDialogListener = object : RecipeImportDialogListener{
        override fun importRecipe(message: String) {
            //nothing to do here
        }
    }

    //functions
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder : AlertDialog.Builder = AlertDialog.Builder(activity!!)

        val view = LayoutInflater.from(activity!!).inflate(R.layout.dialog_import_recipe,null)
        editTextMessage = view.findViewById<EditText>(R.id.import_recipe_dialog_editTxt)

        builder.setView(view)
            .setTitle(R.string.recipe_import)
            .setNegativeButton(R.string.abort, DialogInterface.OnClickListener { _, _ ->  })
            .setPositiveButton(R.string.import_cmd, DialogInterface.OnClickListener { dialogInterface, i ->
                val messageText = editTextMessage.text.toString()
                recipeImportDialogListener.importRecipe(messageText)
            })
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context as? RecipeImportDialogListener).notNull {
            recipeImportDialogListener = it
        }
    }
}