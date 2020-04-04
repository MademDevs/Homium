package de.madem.homium.utilities

import android.widget.Toast
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.models.Units
import de.madem.homium.speech.recognizers.ShoppingRecognizer
import de.madem.homium.ui.dialogs.RecipeImportDialogListener
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.showToastLong
import kotlinx.coroutines.launch

class RecipeImporter : RecipeImportDialogListener {
    companion object{
        private const val EMPTY = ""
        private val recognitionPattern = Regex("[\\n\\t ]*([0-9a-zA-ZäöüÄÖÜ\\- ]+)[:]?[\\n]*(Zutat(en)?)?[:]?[\\n]*((- [1-9]* (${Units.asSpeechRecognitionPattern()}) [a-zA-ZäöüÄÖÜ\\- ]+[\\n]?)*)[\\n]*(Beschreibung(en)?)?[:]?[\\n]*(([1-9]*\\) [0-9a-zA-ZäöüÄÖÜ\\- ]+[\\n]*)*)")
    }

    init {
        println(recognitionPattern)
    }

    //this is the point where import process starts
    override fun importRecipe(message: String) {
        val parsedRecipe = parseRecipe(message)
        if(parsedRecipe == null){
            Toast.makeText(HomiumApplication.appContext!!, R.string.error_msg_recipe_import_failed, Toast.LENGTH_SHORT).show()
        }
        else{
            addRecipe(parsedRecipe)
        }
    }

    //private functions
    private fun parseRecipe(message: String) : Triple<Recipe,List<RecipeIngredient>,List<RecipeDescription>>?{
        val args = recognitionPattern.find(message)?.groupValues
            ?.asSequence()
            ?.map{it.trim()}
            ?.filter{it.isNotEmpty() && it.isNotBlank() && it != message }
            ?.filter{!(it.matches(Regex("(en)?")))}
            ?.filter{!(it.matches(Regex("Zutat(en)?|Beschreibung(en)?")))}
            ?.filter { !(it.matches(Regex(Units.asSpeechRecognitionPattern()))) }
            ?.filterIndexed { index, _ -> index == 0 || index % 2 != 0}
            ?.toList()?.takeIf { it.size == 3 } ?: return null

        if(args.first().isEmpty() || args.first().isBlank() ||
            args.first().matches(Regex("([\n]*- [0-9]* (${Units.asSpeechRecognitionPattern()}) [a-zA-Z0-9äöüÄÖÜ\\-]*)*"))){
            return null
        }

        val recipe = Recipe(args[0],"")
        val ingredients = args[1].split(Regex("[\\n]+"))
            .map { it.replace("- ", EMPTY).trim().split(Regex(" ")) }
            .filter { it.size == 3 }
            .map {
                RecipeIngredient(it[2],it[0].toIntOrNull() ?: 1, it[1],recipeId = recipe.uid)
            }
        val descriptions = args[2].split(Regex("[\\n]+"))
            .map{it.replace(Regex("[1-9]*\\)"), EMPTY).trim()}
            .filter{it.isNotEmpty() && it.isNotBlank()}
            .map{RecipeDescription(it,recipe.uid)}

        return Triple(recipe,ingredients,descriptions)
    }

    private fun addRecipe(data : Triple<Recipe,List<RecipeIngredient>,List<RecipeDescription>>){
        CoroutineBackgroundTask<Boolean>().executeInBackground {
            val cntxt = HomiumApplication.appContext

            return@executeInBackground if(cntxt == null){
                false
            }
            else{
                val recipeDao = AppDatabase.getInstance(cntxt).recipeDao()
                //adding recipe
                val id = recipeDao.insertRecipe(data.first)

                //setting ingredients
                val ingrJob = launch {
                    data.second.forEach {
                        it.recipeId = id.toInt()
                        recipeDao.insertIngredient(it)
                    }
                }

                val descrJob = launch {
                    data.third.forEach {
                        it.recipeID = id.toInt()
                        recipeDao.insertDescription(it)
                    }
                }

                //wait to finish
                ingrJob.join()
                descrJob.join()


                true
            }
        }.onDone {success ->
            HomiumApplication.appContext.notNull {
                val message = if(success){
                    ViewRefresher.recipeViewRefresher.invoke()
                    it.resources.getString(R.string.notification_recipe_import_success)
                }
                else{
                    it.resources.getString(R.string.errormsg_recipe_adding_failed)
                }

                Toast.makeText(it, message,Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
}