package de.madem.homium.speech.recognizers

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_COOK_RECIPE
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.ui.activities.recipe.RecipeEditActivityNew
import de.madem.homium.ui.activities.recipe.RecipePresentationActivity
import de.madem.homium.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

class RecipeRecognizer(private val contextReference : WeakReference<Context>) : PatternRecognizer{

    //fields
    val recipeDao = AppDatabase.getInstance().recipeDao()

    //companion
    companion object{
        //Patterns
        private val ADD_NEW_RECIPE = Regex("([eE]rstell[e]*[n]*|[eE]rzeug[e]*[n]*) \\S* \\S* Rezept( (namens|mit dem Titel) ([a-zA-ZäüöÄÜÖß ]+))?")
        private val EDIT_RECIPE = Regex("([bB]earbeite[n]*[t]*|[eE]ditiere[n]*)[ das]*( Rezept)?( namens| mit dem Titel)? ([a-zA-ZäüöÄÜÖß ]+)")
        private val SHOW_RECIPE = Regex("([zZ]eig[e]*[n]*|([öÖ]ffne[t]*[n]*))[ das]*( Rezept)?( namens| mit dem Titel)? ([a-zA-ZäüöÄÜÖß ]+)")
        private val RECOMMEND_RECIPE = Regex("([sS]chlage[r]*( ein)?( zufälliges)? Rezept vor)|([wW]as gibt es heute zu[m]? ([aA]bendbrot|[aA]bendessen|[mM]ittag|[mM]ittagessen|[fF]rühstück|[eE]ssen)([ ]*\\?)?)")
        private val COOK_RECIPE = Regex("([kK]och[e]*[n]*|[bB]ack[e]*[n]*)(( das)? [rR]ezept)?( namens| mit dem [tT]itel)? ([a-zA-ZäüöÄÜÖß ]+)")
    }

    //matching task
    override fun matchingTask(command: String): CoroutineBackgroundTask<Boolean>? {
        return when{
            command.matches(ADD_NEW_RECIPE) -> matchAddNewRecipe(command)
            command.matches(EDIT_RECIPE) -> matchEditRecipe(command)
            command.matches(SHOW_RECIPE) -> matchShowRecipe(command)
            command.matches(RECOMMEND_RECIPE) -> matchRecommendRecipe()
            command.matches(COOK_RECIPE) -> matchCookRecipe(command)
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

                    context.switchToActivity(RecipeEditActivityNew::class){ intent ->
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

            val recipeId = recipeDao.getIdByName(name=recipeName)

            changedActivityIfValid(RecipeEditActivityNew::class,recipeId)

        }.onDone {success ->
            if(!success){
                sayRecipeNotFound()
            }

        }
    }

    private fun matchShowRecipe(command: String) : CoroutineBackgroundTask<Boolean>{
        return CoroutineBackgroundTask<Boolean>().executeInBackground {
            val params  : List<String> = SHOW_RECIPE.find(command)?.groupValues
                ?.asSequence()
                ?.map{it.trim()}
                ?.filterIndexed{idx,_ -> idx != 0}
                ?.filter{it.isNotEmpty() && it.isNotBlank()}
                ?.filter{!(it.matches(Regex("([zZ]eig[e]*[n]*|[öÖ]ffne[t]*[n]*)")))}
                ?.filter{!(it.matches(Regex("(Rezept)?(namens|mit dem Titel)?")))}
                ?.toList()
                ?.takeIf { it.size == 1 }
                ?: return@executeInBackground false


            val recipeName = params.first()

            val recipeId = recipeDao.getIdByName(name=recipeName)

            changedActivityIfValid(RecipePresentationActivity::class,recipeId)

        }.onDone {success ->
            if(!success){
                sayRecipeNotFound()
            }

        }
    }

    private fun matchRecommendRecipe() : CoroutineBackgroundTask<Boolean>{

        var randomRecipe : Recipe? = null

        return CoroutineBackgroundTask<Boolean>().executeInBackground {


            val recipeIds : List<Int> = recipeDao.getRecipeIds()
            val randomId : Int = recipeIds.random()
            randomRecipe = if(randomId != 0) recipeDao.getRecipeById(randomId) else null
                ?: return@executeInBackground false

            true


        }.onDone {success ->
            if(success && randomRecipe != null){
                contextReference.get().notNull {context ->
                    with(context){
                        val message = resources.getString(R.string.assistent_question_show_recommended_recipe)
                            .replace("#","\n\"${randomRecipe?.name}\"\n")

                        AlertDialog.Builder(this)
                            .setMessage(message)
                            .setPositiveButton(resources.getString(R.string.answer_yes)) { dialog, _ ->
                                switchToActivity(RecipePresentationActivity::class){ intent ->
                                    intent.putExtra(resources.getString(R.string.data_transfer_intent_edit_recipe_id),
                                        randomRecipe?.uid ?: 0)
                                }
                            }
                            .setNegativeButton(resources.getString(R.string.answer_no)) { dialog, _ ->
                                dialog.dismiss()
                            }.show()
                    }
                }
            }
            else{
                saySorry()
            }
        }
    }

    private fun matchCookRecipe(command: String) : CoroutineBackgroundTask<Boolean>{
        var recipeId = 0

        return CoroutineBackgroundTask<Boolean>().executeInBackground {
            val params : List<String> = COOK_RECIPE.find(command)?.groupValues
                ?.asSequence()
                ?.map{it.trim()}
                ?.filterIndexed{idx,_ -> idx != 0}
                ?.filter{!(it.matches(Regex("([kK]och[e]*[n]*|[bB]ack[e]*[n]*)")))}
                ?.filter{!(it.matches(Regex("(das)?")) || it.matches(Regex("((das)? [rR]ezept)?")))}
                ?.filter{!(it.matches(Regex("(namens|mit dem [tT]itel)?")))}
                ?.toList()
                ?.takeIf { it.size == 1 }
                ?: return@executeInBackground false

            //validation
            recipeId = recipeDao.getIdByName(name=params.first())

            if(recipeId <= 0) {
                return@executeInBackground false
            }
            else{
                //TODO: IMPLEMENT LOGIC FOR CALLING COOKING ACTIONS --> DONE LATER IN OTHER BRANCH
                true
            }


        }.onDone {success ->
            if (success){
                contextReference.get().notNull {context ->
                    with(context){
                        //switch Screen
                        switchToActivity(RecipePresentationActivity::class){ intent ->
                            intent.putExtra(resources.getString(R.string.data_transfer_intent_edit_recipe_id),recipeId)
                            intent.putExtra(resources.getString(R.string.data_transfer_intent_recipe_cook_request),
                                REQUEST_CODE_COOK_RECIPE)
                        }
                    }
                }
            }
            else{
                sayRecipeNotFound()
            }

        }
    }

    //help functions
    private fun saySorry(){
        contextReference.get().notNull {
            it.showToastShort(R.string.assistent_msg_recipe_action_failed)
        }
    }

    private fun sayRecipeNotFound() = contextReference.get().notNull {
        it.showToastShort(R.string.assistent_msg_recipe_not_found)
    }

    private suspend fun <T : Any> changedActivityIfValid(clazz: KClass<T>, id : Int) : Boolean = coroutineScope{
        if(id > 0){
            withContext(Dispatchers.Main){
                contextReference.get().notNull {
                    it.switchToActivity(clazz){intent ->
                        intent.putExtra(it.resources.getString(R.string.data_transfer_intent_edit_recipe_id),id)
                    }
                }
            }
            true
        }
        else{
            false
        }
    }

}