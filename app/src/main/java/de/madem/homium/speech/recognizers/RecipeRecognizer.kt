package de.madem.homium.speech.recognizers

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import de.madem.homium.R
import de.madem.homium.ui.activities.recipe.RecipeEditActivity
import de.madem.homium.utilities.*
import java.lang.ref.WeakReference

class RecipeRecognizer(private val contextReference : WeakReference<Context>) : PatternRecognizer{

    //companion
    companion object{
        //Patterns
        val ADD_NEW_RECIPE = Regex("([eE]rstell[e]*[n]*|[eE]rzeug[e]*[n]*) \\S* \\S* Rezept( (namens|mit dem Titel) ([a-zA-ZäüöÄÜÖß ]+))?")

    }

    //matching task
    override fun matchingTask(command: String): CoroutineBackgroundTask<Boolean>? {
        return when{
            command.matches(ADD_NEW_RECIPE) -> matchAddNewRecipe(command)
            else -> null
        }

    }

    //private functions
    private fun matchAddNewRecipe(command : String) : CoroutineBackgroundTask<Boolean>?{
        val context = contextReference.get()
        if(context != null){
            var name = ""

            return CoroutineBackgroundTask<Boolean>().executeInBackground{
                val params = ADD_NEW_RECIPE.find(command)?.groupValues
                    ?.map{it.trim()}
                    ?.asSequence()
                    ?.filter{it.isNotEmpty()}
                    ?.filterIndexed{index, _ -> index != 0}
                    ?.filter{!(it.matches(Regex("([eE]rstell[e]*[n]*|[eE]rzeug[e]*[n]*)")))}
                    ?.filter{!(it.matches(Regex("(namens|mit dem Titel)")))}
                    ?.map { it.replace(Regex("(namens|mit dem Titel)"),"").trim() }
                    ?.toList()
                    ?.takeIf { it.isEmpty() || it.size == 2 } ?: return@executeInBackground false

                if(params.isNotEmpty()){
                    name = params[0]
                }

                return@executeInBackground true

            }.onDone {success ->
                if(success){

                    context.switchToActivity(RecipeEditActivity::class){intent ->
                        if(name.isNotEmpty() && name.isNotBlank()){
                           intent.putExtra(context.resources.getString(R.string.data_transfer_intent_edit_recipe_name),
                               name)
                        }
                    }
                }
                else{
                    saySorry()
                }
            }
        }
        else{
            return null
        }

    }

    //help functions
    private fun saySorry(){
        contextReference.get().notNull {
            it.showToastShort(R.string.assistent_msg_recipe_action_failed)
        }
    }

}