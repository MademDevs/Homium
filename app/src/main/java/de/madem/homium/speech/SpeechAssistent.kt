package de.madem.homium.speech

import android.content.Context
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.FragmentActivity
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.speech.recognizers.ShoppingRecognizer
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.UserRequestedCoroutineBackgroundTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SpeechAssistent(val context: Context) {

    //fields
    val shoppingRecognizer = ShoppingRecognizer(context)

    //public function
    fun executeCommand(command : String){
        CoroutineBackgroundTask<CoroutineBackgroundTask<Boolean>?>().executeInBackground {
            shoppingRecognizer.matchingTask(command)
        }.onDone {task ->
            task?.start() ?: saySorry()

        }.start()
    }

    // saying Sorry if somthing failed
    private fun saySorry() = Toast.makeText(context,getStringRessource(R.string.assistent_msg_sorry),Toast.LENGTH_SHORT).show()

    private fun getStringRessource(id : Int) : String{
        return context.resources.getString(id)
    }





}
