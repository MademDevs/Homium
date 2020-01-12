package de.madem.homium.speech

import android.content.Context
import android.widget.Toast
import de.madem.homium.R
import de.madem.homium.speech.recognizers.InventoryRecognizer
import de.madem.homium.speech.recognizers.PatternRecognizer
import de.madem.homium.speech.recognizers.RecipeRecognizer
import de.madem.homium.speech.recognizers.ShoppingRecognizer
import de.madem.homium.utilities.CoroutineBackgroundTask
import java.lang.ref.WeakReference

class SpeechAssistent(val context: Context) {

    //fields

    private val recognizers = listOf<PatternRecognizer>(
        ShoppingRecognizer(WeakReference<Context>(context)),
        InventoryRecognizer(WeakReference<Context>(context)),
        RecipeRecognizer(WeakReference<Context>(context)))

    //public function
    fun executeCommand(command : String){

        val formattedCommand = replaceNumberWords(command.toLowerCase())

        CoroutineBackgroundTask<CoroutineBackgroundTask<Boolean>?>().executeInBackground {
            var resultTask : CoroutineBackgroundTask<Boolean>? = null

            for(rec in recognizers){
                val result = if(rec is RecipeRecognizer){
                     rec.matchingTask(command)
                }
                else{
                    rec.matchingTask(formattedCommand)
                }

                if(result != null){
                    resultTask = result
                    break
                }

            }

            return@executeInBackground resultTask

        }.onDone {task ->
            task?.start() ?: saySorry()

        }.start()
    }

    // saying Sorry if somthing failed
    private fun saySorry() = Toast.makeText(context,getStringRessource(R.string.assistent_msg_sorry),Toast.LENGTH_SHORT).show()

    private fun getStringRessource(id : Int) : String{
        return context.resources.getString(id)
    }

    private fun replaceNumberWords(str : String) : String{
        return str.replace(Regex(" ein(e)*(n)* ")," 1 ")
            .replace(" zwei "," 2 ")
            .replace(" drei "," 3 ")
            .replace(" vier "," 4 ")
            .replace(" fünf "," 5 ")
            .replace(" sechs "," 6 ")
            .replace(" sieben "," 7 ")
            .replace(" acht "," 8 ")
            .replace(" neun "," 9 ")
            .replace(" zehn "," 10 ")
            .replace(" elf "," 11 ")
            .replace(" zwölf "," 12 ")
    }



}
