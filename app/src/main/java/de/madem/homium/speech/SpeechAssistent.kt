package de.madem.homium.speech

import android.content.Context
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.FragmentActivity
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.speech.recognizers.InventoryRecognizer
import de.madem.homium.speech.recognizers.PatternRecognizer
import de.madem.homium.speech.recognizers.ShoppingRecognizer
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.UserRequestedCoroutineBackgroundTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class SpeechAssistent(val context: Context) {

    //fields
    /*
    private val shoppingRecognizer = ShoppingRecognizer(WeakReference<Context>(context))
    private val inventoryRecognizer = InventoryRecognizer(WeakReference<Context>(context))

     */

    private val recognizers = listOf<PatternRecognizer>(
        ShoppingRecognizer(WeakReference<Context>(context)),
        InventoryRecognizer(WeakReference<Context>(context)))

    //public function
    fun executeCommand(command : String){

        val formattedCommand = replaceNumberWords(command.toLowerCase())

        CoroutineBackgroundTask<CoroutineBackgroundTask<Boolean>?>().executeInBackground {
            //shoppingRecognizer.matchingTask(formattedCommand)

            /*val results = mutableListOf<CoroutineBackgroundTask<Boolean>?>()
            val mutex = Mutex()

            async{
                for(rec in recognizers){
                    launch {
                        val result = rec.matchingTask(formattedCommand)
                        mutex.withLock { results.add(result) }
                    }.join()

                }
            }.await()

            return@executeInBackground results.filterNotNull().takeIf { it.isNotEmpty() }?.get(0)


             */
            var resultTask : CoroutineBackgroundTask<Boolean>? = null

            for(rec in recognizers){
                val result = rec.matchingTask(formattedCommand)
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
