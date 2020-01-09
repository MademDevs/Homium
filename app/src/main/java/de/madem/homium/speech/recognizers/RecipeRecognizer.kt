package de.madem.homium.speech.recognizers

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.ui.activities.recipe.RecipeEditActivity
import de.madem.homium.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class RecipeRecognizer(private val contextReference : WeakReference<Context>) : PatternRecognizer{

    //fields
    val recipeDao = AppDatabase.getInstance().recipeDao()

    //companion
    companion object{
        //Patterns
        val ADD_NEW_RECIPE = Regex("([eE]rstell[e]*[n]*|[eE]rzeug[e]*[n]*) \\S* \\S* Rezept( (namens|mit dem Titel) ([a-zA-ZäüöÄÜÖß ]+))?")
        val EDIT_RECIPE = Regex("([bB]earbeite[n]*[t]*|[eE]ditiere[n]*)[ das]*( Rezept)?( namens| mit dem Titel)? ([a-zA-ZäüöÄÜÖß ]+)")

    }

    //matching task
    override fun matchingTask(command: String): CoroutineBackgroundTask<Boolean>? {
        return when{
            command.matches(ADD_NEW_RECIPE) -> matchAddNewRecipe(command)
            command.matches(EDIT_RECIPE) -> matchEditRecipe(command)
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

    private fun matchEditRecipe(command : String) : CoroutineBackgroundTask<Boolean>{
        return CoroutineBackgroundTask<Boolean>().executeInBackground {
            val params : List<String> = EDIT_RECIPE.find(command)?.groupValues
                ?.asSequence()
                ?.map{it.trim()}
                ?.filterIndexed{idx,_ -> idx != 0}
                ?.filter{it.isNotEmpty() && it.isNotBlank()}
                ?.filter{!(it.matches(Regex("([bB]earbeite[n]*|[eE]ditiere[n]*)")))}
                ?.filter{!(it.matches(Regex("(Rezept)?(namens|mit dem Titel)?")))}
                ?.toList()
                ?.takeIf { it.size == 1 }
                ?: return@executeInBackground false

            val recipeName = params.first()

            val recipeId = recipeDao.idOf(name=recipeName)

            if(recipeId > 0){
                withContext(Dispatchers.Main){
                    contextReference.get().notNull {
                        it.switchToActivity(RecipeEditActivity::class){intent ->
                            intent.putExtra(it.resources.getString(R.string.data_transfer_intent_edit_recipe_id),recipeId)
                        }
                    }
                }
                true
            }
            else{
                return@executeInBackground false
            }

        }.onDone {success ->
            if(!success){
               contextReference.get().notNull {
                   it.showToastShort(R.string.assistent_msg_recipe_not_found)
               }
            }

        }
    }

    //help functions
    private fun saySorry(){
        contextReference.get().notNull {
            it.showToastShort(R.string.assistent_msg_recipe_action_failed)
        }
    }

}