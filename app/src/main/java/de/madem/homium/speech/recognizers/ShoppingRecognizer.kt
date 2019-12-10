package de.madem.homium.speech.recognizers

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.speech.CommandParser
import de.madem.homium.ui.fragments.shopping.ShoppingFragment
import de.madem.homium.ui.fragments.shopping.ShoppingViewModel
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.UserRequestedCoroutineBackgroundTask
import de.madem.homium.utilities.notNull
import de.madem.homium.utilities.toAppCompatActivityOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class ShoppingRecognizer(val context: Context) : PatternRecognizer {


    private val itemDao = AppDatabase.getInstance().itemDao()
    private val commandParser = CommandParser(context)

    companion object{
        val ADD_SHOPPING_ITEM = Regex("s[ei]tze ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) ([a-zA-ZäöüÄÖÜ]+) auf die einkaufsliste")
        val ADD_SHOPPING_ITEM_WITHOUT_UNIT = Regex("s[ei]tze ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) auf die einkaufsliste")
        val ADD_SHOPPING_ITEM_WITHOUT_UNIT_WITHOUT_QUANTITY = Regex("s[ei]tze ([a-zA-ZäöüÄÖÜ]+) auf die einkaufsliste")
        val CLEAR_SHOPPING_LIST = Regex("(lösch(e)*|(be)?reinig(e)*(n)*){1} [^ ]* einkaufsliste")
        val DELETE_SHOPPING_WITH_NAME = Regex("lösch(e)*( alle)? ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der einkaufsliste( heraus)?")
        val DELETE_SHOPPING_WITH_ALL_PARAMS = Regex("(lösch(e)*|welche){1} ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der einkaufsliste( heraus)?")
    }

    //functions
    override fun matchingTask(command: String): CoroutineBackgroundTask<Boolean>? {

        return when{
            command.matches(ADD_SHOPPING_ITEM) -> matchAddShopping(command)
            command.matches(ADD_SHOPPING_ITEM_WITHOUT_UNIT) -> matchAddShoppingWithoutUnit(command)
            command.matches(ADD_SHOPPING_ITEM_WITHOUT_UNIT_WITHOUT_QUANTITY) -> matchAddShoppingWithoutUnitWithoutQuantity(command)
            command.matches(CLEAR_SHOPPING_LIST) -> matchClearShoppingList(command)
            command.matches(DELETE_SHOPPING_WITH_NAME) -> matchDeleteShoppingWithName(command)
            command.matches(DELETE_SHOPPING_WITH_ALL_PARAMS) -> matchDeleteShoppingWithAllParams(command)
            else -> null
        }
    }


    //functions for recognition
    private fun matchAddShopping(command : String) : CoroutineBackgroundTask<Boolean>{
        return CoroutineBackgroundTask<Boolean>().executeInBackground{
            val result = commandParser.parseShoppingItem(command.split(" ").toList().slice(1..3))

            if(result != null){
                itemDao.insertShopping(result)
                withContext(Dispatchers.Main){
                    Toast.makeText(context,getStringRessource(R.string.assistent_msg_shoppingitem_added),
                        Toast.LENGTH_SHORT).show()
                }
            }

            result != null
        }.onDone { sucess ->
            if(!sucess) {
                Toast.makeText(context,
                    R.string.errormsg_insert_shoppingitem_failed,
                    Toast.LENGTH_SHORT).show()
            }
            else{
                ViewRefresher.shoppingRefresher.invoke()

            }


        }
    }

    private fun matchAddShoppingWithoutUnit(command : String) : CoroutineBackgroundTask<Boolean> {
        return CoroutineBackgroundTask<Boolean>().executeInBackground {
            //getting words and adding "stück" as unit
            val words = command.split(Regex(" ")).toMutableList().apply {
                add(2, Units.ITEM.getString(context))

            }

            //parsing shopping item
            val itemResult = async<ShoppingItem?> {
                commandParser.parseShoppingItem(words.slice(1..3))
            }

            //getting products
            val productsFromDataBase = itemDao.getProductsByName(words[3])
            val result = itemResult.await()

            //inserting result if not null
            if (result != null){
                //editing unit if there is a preset for unit in products
                if(!(productsFromDataBase.isNullOrEmpty())){
                    val productNameIndex = productsFromDataBase.map { it.name }.indexOf(result.name)
                    if(productNameIndex >= 0) {
                        val newUnit = productsFromDataBase[productNameIndex].unit
                        itemDao.insertShopping(ShoppingItem(result.name,result.count,newUnit))
                    }
                    else{
                        itemDao.insertShopping(result)
                    }
                }
                else{
                    itemDao.insertShopping(result)
                }

                withContext(Dispatchers.Main){
                    Toast.makeText(context,getStringRessource(R.string.assistent_msg_shoppingitem_added),
                        Toast.LENGTH_SHORT).show()
                }

                true
            }
            else{
                false
            }
        }.onDone { sucess ->
            if(!sucess) {
                Toast.makeText(context,
                    R.string.errormsg_insert_shoppingitem_failed,
                    Toast.LENGTH_SHORT).show()
            }
            else{
                ViewRefresher.shoppingRefresher.invoke()

            }
        }
    }

    private fun matchAddShoppingWithoutUnitWithoutQuantity(command : String) : CoroutineBackgroundTask<Boolean>{
        return CoroutineBackgroundTask<Boolean>().executeInBackground {
            val words = command.split(Regex(" ")).toMutableList().apply {
                add(1, Units.ITEM.getString(context))
                add(1,1.toString())
            }

            //parsing shopping item
            val itemResult = async<ShoppingItem?> {
                commandParser.parseShoppingItem(words.slice(1..3))
            }

            //getting products
            val productsFromDataBase = itemDao.getProductsByName(words[3])
            val result = itemResult.await()

            //inserting result if not null
            if (result != null){
                //editing unit if there is a preset for unit in products
                if(!(productsFromDataBase.isNullOrEmpty())){
                    val productNameIndex = productsFromDataBase.map { it.name }.indexOf(result.name)
                    if(productNameIndex >= 0) {
                        val newUnit = productsFromDataBase[productNameIndex].unit
                        val newQuantity = productsFromDataBase[productNameIndex].amount.toIntOrNull() ?: 1
                        itemDao.insertShopping(ShoppingItem(result.name,newQuantity,newUnit))
                    }
                    else{
                        itemDao.insertShopping(result)
                    }
                }
                else{
                    itemDao.insertShopping(result)
                }

                withContext(Dispatchers.Main){
                    Toast.makeText(context,getStringRessource(R.string.assistent_msg_shoppingitem_added),
                        Toast.LENGTH_SHORT).show()
                }

                true
            }
            else{
                false
            }
        }.onDone {sucess ->
            if(!sucess) {
                Toast.makeText(context,
                    R.string.errormsg_insert_shoppingitem_failed,
                    Toast.LENGTH_SHORT).show()
            }
            else{
                ViewRefresher.shoppingRefresher.invoke()

            }
        }
    }

    private fun matchClearShoppingList(command: String) : CoroutineBackgroundTask<Boolean> {
        return UserRequestedCoroutineBackgroundTask<Boolean>(context,getStringRessource(R.string.assistent_question_delete_all_shopping)).executeInBackground{
            itemDao.deleteAllShopping()
            true
        }.onDone {sucess ->
            if(sucess) {
                Toast.makeText(context, R.string.notification_remove_all_shoppingitems, Toast.LENGTH_SHORT).show()
                ViewRefresher.shoppingRefresher.invoke()
            }
        }
    }

    private fun matchDeleteShoppingWithName(command : String) : CoroutineBackgroundTask<Boolean>{
        val words = command.split(Regex(" "))
        val name = if(words.contains("alle")) words[2] else words[1]

        val msg = getStringRessource(R.string.assistent_question_delete_all_shopping_with_name).replace("#","\"${name.capitalize()}\"")

        return UserRequestedCoroutineBackgroundTask<Boolean>(context,msg).executeInBackground {
            itemDao.deleteShoppingByName(name)
            true
        }.onDone {sucess ->
            if(sucess){
                val sucessMsg = getStringRessource(R.string.assistent_msg_shoppingitem_with_param_deleted).replace("#","\"${name.capitalize()}\"")
                Toast.makeText(context,sucessMsg, Toast.LENGTH_SHORT).show()
                ViewRefresher.shoppingRefresher.invoke()
            }


        }
    }

    private fun matchDeleteShoppingWithAllParams(command: String) : CoroutineBackgroundTask<Boolean>{
        val params = command.split(" ").slice(1..3)
        val itemStr : String = params.map { it.capitalize() }.joinToString(" ")
        println(itemStr.toUpperCase())

        return UserRequestedCoroutineBackgroundTask<Boolean>(context, getStringRessource(R.string.assistent_question_delete_all_shopping_with_name).replace("#","\"$itemStr\"")).executeInBackground{
            val item : ShoppingItem?= commandParser.parseShoppingItem(params)

            if(item != null){
                itemDao.deleteShoppingByNameCountUnit(item.name,item.count,item.unit)
                true
            }
            else{
                false
            }


        }.onDone {sucess ->
            if(sucess){
                Toast.makeText(context,getStringRessource(R.string.assistent_msg_shoppingitem_with_param_deleted).replace("#","\"$itemStr\""),
                    Toast.LENGTH_SHORT).show();
                ViewRefresher.shoppingRefresher.invoke()
            }
            else{
                saySorry()
            }
        }
    }

    //helpFunctions
    private fun getStringRessource(id : Int) : String{
        return context.resources.getString(id)
    }

    // saying Sorry if somthing failed
    private fun saySorry() = Toast.makeText(context,getStringRessource(R.string.assistent_msg_sorry),Toast.LENGTH_SHORT).show()
}