package de.madem.homium.speech.recognizers

import android.content.Context
import android.widget.Toast
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.speech.commandparser.ShoppingCommandParser
import de.madem.homium.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class ShoppingRecognizer(private val contextRef: WeakReference<Context>) : PatternRecognizer {


    private val itemDao = AppDatabase.getInstance().itemDao()
    private val commandParser =
        ShoppingCommandParser(contextRef)

    companion object{
        private val unitsAsRecognitionPattern = Units.asSpeechRecognitionPattern()

        val ADD_SHOPPING_ITEM = Regex("[sS]{1}[ei]tze (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}){1} ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")
        val ADD_SHOPPING_ITEM_WITHOUT_UNIT = Regex("[sS]{1}[ei]tze (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")
        val ADD_SHOPPING_ITEM_WITHOUT_UNIT_WITHOUT_QUANTITY = Regex("[sS]{1}[ei]tze ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")
        val CLEAR_SHOPPING_LIST = Regex("(lösch(e)*|(be)?reinig(e)*(n)*){1} [^ ]* [eE]{1}inkaufsliste")
        val DELETE_SHOPPING_WITH_NAME = Regex("lösch(e)*( alle)? ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
        val DELETE_SHOPPING_WITH_ALL_PARAMS = Regex("(lösch(e)*|welche){1} (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}){1} ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
        val DELETE_SHOPPING_WITH_NAME_QUANTITY = Regex("(lösch(e)*|welche){1} (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
    }

    //functions
    override fun matchingTask(command: String): CoroutineBackgroundTask<Boolean>? {
        return when{
            command.matches(ADD_SHOPPING_ITEM) -> matchAddShopping(command)
            command.matches(ADD_SHOPPING_ITEM_WITHOUT_UNIT) -> matchAddShoppingWithoutUnit(command)
            command.matches(ADD_SHOPPING_ITEM_WITHOUT_UNIT_WITHOUT_QUANTITY) -> matchAddShoppingWithoutUnitWithoutQuantity(command)
            command.matches(CLEAR_SHOPPING_LIST) -> matchClearShoppingList()
            command.matches(DELETE_SHOPPING_WITH_ALL_PARAMS) -> matchDeleteShoppingWithAllParams(command)
            command.matches(DELETE_SHOPPING_WITH_NAME_QUANTITY) -> matchDeleteShoppingWithNameQuantity(command)
            command.matches(DELETE_SHOPPING_WITH_NAME) -> matchDeleteShoppingWithName(command)
            else -> null
        }
    }


    //functions for recognition
    private fun matchAddShopping(command : String) : CoroutineBackgroundTask<Boolean>{
        return CoroutineBackgroundTask<Boolean>().executeInBackground{

            val params : List<String> = ADD_SHOPPING_ITEM
                .find(command)?.groupValues?.filter { it.isNotBlank() && it.isNotEmpty() }?.toMutableList()
                ?.apply {
                    remove(command)
                    removeAll {this[1] != it && this[1].contains(it)}
                }
                .takeIf { it != null && it.size == 3 } ?:
                command.split(" ").toList().slice(1..3)

            val result = commandParser.parseShoppingItem(params)

            if(result != null){
                itemDao.insertShopping(result)
                withContext(Dispatchers.Main){
                    contextRef.get().notNull {
                        Toast.makeText(it,getStringRessource(R.string.assistent_msg_shoppingitem_added),
                            Toast.LENGTH_SHORT).show()
                    }

                }
            }

            result != null
        }.onDone { sucess ->
            if(!sucess) {
                contextRef.get().notNull {
                    Toast.makeText(it,
                        R.string.errormsg_insert_shoppingitem_failed,
                        Toast.LENGTH_SHORT).show()
                }


            }
            else{
                ViewRefresher.shoppingRefresher.invoke()

            }


        }
    }

    private fun matchAddShoppingWithoutUnit(command : String) : CoroutineBackgroundTask<Boolean> {
        return CoroutineBackgroundTask<Boolean>().executeInBackground {
            //getting words

            val words = ADD_SHOPPING_ITEM_WITHOUT_UNIT.find(command)?.groupValues
                ?.filter { it.isNotBlank() && it.isNotEmpty() }?.toMutableList()
                ?.apply { remove(command) }.takeIf { it != null && it.size == 2 } ?:
                command.split(Regex(" ")).toMutableList().slice(1..2)

            //parsing shopping item
            val parsedItem : ShoppingItem? = commandParser.parseShoppingItemWithoutUnit(words)

            //insert if not null
            var sucess = false

            parsedItem.notNull {
                itemDao.insertShopping(it)
                sucess = true
            }

            return@executeInBackground sucess

        }.onDone { sucess ->
            if(!sucess) {
                contextRef.get().notNull {
                    Toast.makeText(it,
                        R.string.errormsg_insert_shoppingitem_failed,
                        Toast.LENGTH_SHORT).show()
                }


            }
            else{
                ViewRefresher.shoppingRefresher.invoke()

            }
        }
    }

    private fun matchAddShoppingWithoutUnitWithoutQuantity(command : String) : CoroutineBackgroundTask<Boolean>{
        return CoroutineBackgroundTask<Boolean>().executeInBackground {

            val name : String = ADD_SHOPPING_ITEM_WITHOUT_UNIT_WITHOUT_QUANTITY.find(command)?.groupValues
                ?.filter { it.isNotBlank() && it.isNotEmpty() }?.toMutableList()
                ?.apply { remove(command) }.takeIf { it != null && it.size == 1}
                ?.get(0)  ?: command.split(" ")[1]

            //parsing shopping item
            val parsedItem = commandParser.parseShoppingItemWithoutUnitWithoutAmount(name)

            var sucess = false

            parsedItem.notNull {
                itemDao.insertShopping(it)
                sucess = true
            }

            return@executeInBackground sucess

        }.onDone {sucess ->
            if(!sucess) {
                contextRef.get().notNull {
                    Toast.makeText(it,
                        R.string.errormsg_insert_shoppingitem_failed,
                        Toast.LENGTH_SHORT).show()
                }
            }
            else{
                ViewRefresher.shoppingRefresher.invoke()

            }
        }
    }

    private fun matchClearShoppingList() : CoroutineBackgroundTask<Boolean> {
        return UserRequestedCoroutineBackgroundTask<Boolean>(contextRef,getStringRessource(R.string.assistent_question_delete_all_shopping)).executeInBackground{
            if(itemDao.shoppingListSize() != 0){
                itemDao.deleteAllShopping()
                true
            }
            else{
                false
            }



        }.onDone {sucess ->

            contextRef.get().notNull {cntxt ->
                if(sucess) {
                    Toast.makeText(cntxt, R.string.notification_remove_all_shoppingitems, Toast.LENGTH_SHORT).show()
                }
                else{
                    cntxt.showToastShort(R.string.errormsg_delete_shopping_failed)
                }

            }

            if(sucess){
                ViewRefresher.shoppingRefresher.invoke()
            }

        }


    }

    private fun matchDeleteShoppingWithName(command : String) : CoroutineBackgroundTask<Boolean>{
        val words = DELETE_SHOPPING_WITH_NAME.find(command)?.groupValues
            ?.filter{it.isNotBlank() && it.isNotEmpty()}?.map{it.trim()}?.toMutableList()
            ?.apply{
                remove(command)
                removeAll{it == "e"}
                removeAll { it == "heraus" }
                removeAll{it.matches(Regex("(aus|von){1}"))}
            }?.takeIf { it.size == 2 || it.size == 1 } ?: command.split(Regex(" "))

        val allShouldBeDeleted = words.contains("alle")
            var name = if(words.size > 2){
                if(allShouldBeDeleted) words[2] else words[1]
            }
            else{
            words[words.lastIndex]
        }

        name = name.trim().capitalizeEachWord()

        val msg = if(allShouldBeDeleted){
            getStringRessource(R.string.assistent_question_delete_all_shopping_with_name_and_plural).replace("#","\"${name.capitalize()}\"")
        }
        else {
            getStringRessource(R.string.assistent_question_delete_all_shopping_with_name).replace("#","\"${name.capitalize()}\"")
        }

        return UserRequestedCoroutineBackgroundTask<Boolean>(contextRef,msg).executeInBackground {

            if(!(itemDao.getAllShoppingNames().contains(name))){
                return@executeInBackground false
            }

            if(allShouldBeDeleted){
                val products = itemDao.getProductsByNameOrPlural(name)

                if(products.isNotEmpty()){
                    itemDao.deleteShoppingByName(products[0].plural)
                    itemDao.deleteShoppingByName(products[0].name)
                }
            }

            itemDao.deleteShoppingByName(name)

            true
        }.onDone {sucess ->
            if(sucess){
                val sucessMsg = getStringRessource(R.string.assistent_msg_inventoryitem_with_param_deleted).replace("#","\"${name.capitalize()}\"")
                contextRef.get().notNull {
                    Toast.makeText(it,sucessMsg, Toast.LENGTH_SHORT).show()
                }
                ViewRefresher.shoppingRefresher.invoke()
            }
            else{
                val failMsg = getStringRessource(R.string.assistent_msg_delete_failed)
                contextRef.get().notNull {
                    Toast.makeText(it,failMsg, Toast.LENGTH_SHORT).show()
                }
            }


        }
    }

    private fun matchDeleteShoppingWithAllParams(command: String) : CoroutineBackgroundTask<Boolean>{
        val params =  DELETE_SHOPPING_WITH_ALL_PARAMS.find(command)?.groupValues?.filter{it.isNotBlank() && it.isNotEmpty()}?.map{it.trim()}?.toMutableList()?.apply{
            remove(command)
            removeAll { it == "heraus" }
            removeAll{it.matches(Regex("e(n)?"))}
            removeAll{it.matches(Regex("lösch(e)*(n)*"))}
            removeAll{it.matches(Regex("(aus|von){1}"))}
        }?.takeIf { it.size == 3  } ?: command.split(" ").slice(1..3)
        val itemStr : String = params.map { it.capitalize() }.joinToString(" ")
        println(itemStr.toUpperCase())


        return UserRequestedCoroutineBackgroundTask<Boolean>(contextRef, getStringRessource(R.string.assistent_question_delete_all_shopping_with_name).replace("#","\"$itemStr\""))
            .executeInBackground{

                if(!(itemDao.getAllShoppingNames().contains(params[2].capitalize()))){
                    return@executeInBackground false
                }

                val item : ShoppingItem?= commandParser.parseShoppingItem(params)

                if(item != null){
                    itemDao.deleteShoppingByNameCountUnit(item.name,item.count,item.unit)
                    true
                }
                else{
                    false
                }


            }
            .onDone {sucess ->
                if(sucess){
                    contextRef.get().notNull {
                        Toast.makeText(it,getStringRessource(R.string.assistent_msg_shoppingitem_with_param_deleted).replace("#","\"$itemStr\""),
                            Toast.LENGTH_SHORT).show();
                    }
                    ViewRefresher.shoppingRefresher.invoke()
                }
                else{
                    saySorry()
                }
            }
    }

    private fun matchDeleteShoppingWithNameQuantity(command: String) : CoroutineBackgroundTask<Boolean>?{
        val params = DELETE_SHOPPING_WITH_NAME_QUANTITY.find(command)?.groupValues?.filter{it.isNotBlank() && it.isNotEmpty()}?.map{it.trim()}?.toMutableList()?.apply{
            remove(command)
            removeAll { it == "heraus" }
            removeAll{it.matches(Regex("e(n)?"))}
            removeAll{it.matches(Regex("lösch(e)*(n)*"))}
            removeAll{it.matches(Regex("(aus|von){1}"))}
        }?.takeIf { it.size == 2 } ?: command.split(" ").slice(1..2).toMutableList()
        val pseudoOut : String = params.map { it.capitalizeEachWord() }.joinToString(" ")

        return UserRequestedCoroutineBackgroundTask<Boolean>(contextRef,contextRef.get()?.getString(R.string.assistent_question_delete_all_shopping_with_name)
            ?.replace("#","\"${pseudoOut}\"") ?: "")
            .executeInBackground {



                val searchCount = params.first().trim().toIntOrNull()
                val searchName = params.last().trim().capitalizeEachWord()

                if(!(itemDao.getAllShoppingNames().contains(searchName))){
                    return@executeInBackground false
                }

                if(searchCount != null){
                    itemDao.deleteShoppingItemByNameCount(searchName,searchCount)
                    true
                }
                else{
                    false
                }



             }
            .onDone {sucess ->
                if(sucess){
                    contextRef.get().notNull {
                        Toast.makeText(it,getStringRessource(R.string.assistent_msg_shoppingitem_with_param_deleted).replace("#","\"$pseudoOut\""),
                            Toast.LENGTH_SHORT).show();
                    }
                    ViewRefresher.shoppingRefresher.invoke()
                }
                else{
                    saySorry()
                }
            }
    }

    //helpFunctions
    private fun getStringRessource(id : Int) : String{
        return contextRef.get()?.resources?.getString(id)
            ?: HomiumApplication.appContext!!.resources.getString(id)
    }

    // saying Sorry if somthing failed
    private fun saySorry() = Toast.makeText(contextRef.get() ?: HomiumApplication.appContext!!,getStringRessource(R.string.assistent_msg_sorry),Toast.LENGTH_SHORT).show()
}