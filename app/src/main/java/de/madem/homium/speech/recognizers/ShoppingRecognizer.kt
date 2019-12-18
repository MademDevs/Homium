package de.madem.homium.speech.recognizers

import android.content.Context
import android.widget.Toast
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.speech.CommandParser
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.UserRequestedCoroutineBackgroundTask
import de.madem.homium.utilities.notNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class ShoppingRecognizer(val context: Context) : PatternRecognizer {


    private val itemDao = AppDatabase.getInstance().itemDao()
    private val commandParser = CommandParser(context)

    companion object{
        private val unitsAsRecognitionPattern = Units.values().map {
            val str = it.getString()
            val shortcutExists = it.shortCut.isNotEmpty()

            //creating regex string via string operation
            //goal is to achieve like e.g. ([mM]{1}illiliter)(ml)
            if(it == Units.PACK){
                "${str.replaceFirst(str.first().toString(),"[${str.first().toLowerCase()}${str.first().toUpperCase()}]{1}")}(en)?${if(shortcutExists) "|"+it.shortCut else ""}"
            }
            else if(it == Units.KILOGRAM){
                "${str.replaceFirst(str.first().toString(),"[${str.first().toLowerCase()}${str.first().toUpperCase()}]{1}").removeSuffix("gramm")}(gramm)?${if(shortcutExists) "|"+it.shortCut else ""}"
            }
            else{
                "${str.replaceFirst(str.first().toString(),"[${str.first().toLowerCase()}${str.first().toUpperCase()}]{1}")}${if(shortcutExists) "|"+it.shortCut else ""}"
            }
        }.joinToString("|").also { println(it) }

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
        println(command.matches(Regex("[sS]{1}[ei]tze ([(a-z)(0-9)]+) (${unitsAsRecognitionPattern}) ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")))
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
                Toast.makeText(context,
                    R.string.errormsg_insert_shoppingitem_failed,
                    Toast.LENGTH_SHORT).show()
            }
            else{
                ViewRefresher.shoppingRefresher.invoke()

            }
        }
    }

    private fun matchClearShoppingList() : CoroutineBackgroundTask<Boolean> {
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
        val words = DELETE_SHOPPING_WITH_NAME.find(command)?.groupValues
            ?.filter{it.isNotBlank() && it.isNotEmpty()}?.map{it.trim()}?.toMutableList()
            ?.apply{
                remove(command)
                remove("e")
                removeAll{it.matches(Regex("(aus|von){1}"))}
            }?.takeIf { it.size == 2 || it.size == 1 } ?: command.split(Regex(" "))

        val allShouldBeDeleted = words.contains("alle")
        var name = if(words.size > 2){
            if(allShouldBeDeleted) words[2] else words[1]
        }
        else{
            words[words.lastIndex]
        }

        name = name.split(" ").map { it.capitalize() }.joinToString (" ")

        val msg = if(allShouldBeDeleted){
            getStringRessource(R.string.assistent_question_delete_all_shopping_with_name_and_plural).replace("#","\"${name.capitalize()}\"")
        }
        else {
            getStringRessource(R.string.assistent_question_delete_all_shopping_with_name).replace("#","\"${name.capitalize()}\"")
        }

        return UserRequestedCoroutineBackgroundTask<Boolean>(context,msg).executeInBackground {
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
                val sucessMsg = getStringRessource(R.string.assistent_msg_shoppingitem_with_param_deleted).replace("#","\"${name.capitalize()}\"")
                Toast.makeText(context,sucessMsg, Toast.LENGTH_SHORT).show()
                ViewRefresher.shoppingRefresher.invoke()
            }
            else{
                val failMsg = getStringRessource(R.string.assistent_msg_delete_failed)
                Toast.makeText(context,failMsg, Toast.LENGTH_SHORT).show()
            }


        }
    }

    private fun matchDeleteShoppingWithAllParams(command: String) : CoroutineBackgroundTask<Boolean>{
        val params =  DELETE_SHOPPING_WITH_ALL_PARAMS.find(command)?.groupValues?.filter{it.isNotBlank() && it.isNotEmpty()}?.map{it.trim()}?.toMutableList()?.apply{
            remove(command)
            removeAll{it.matches(Regex("e(n)?"))}
            removeAll{it.matches(Regex("lösch(e)*(n)*"))}
            removeAll{it.matches(Regex("(aus|von){1}"))}
        }?.takeIf { it.size == 3  } ?: command.split(" ").slice(1..3)
        val itemStr : String = params.map { it.capitalize() }.joinToString(" ")
        println(itemStr.toUpperCase())


        return UserRequestedCoroutineBackgroundTask<Boolean>(context, getStringRessource(R.string.assistent_question_delete_all_shopping_with_name).replace("#","\"$itemStr\""))
            .executeInBackground{

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
                    Toast.makeText(context,getStringRessource(R.string.assistent_msg_shoppingitem_with_param_deleted).replace("#","\"$itemStr\""),
                        Toast.LENGTH_SHORT).show();
                    ViewRefresher.shoppingRefresher.invoke()
                }
                else{
                    saySorry()
                }
            }
    }

    private fun matchDeleteShoppingWithNameQuantity(command: String) : CoroutineBackgroundTask<Boolean>{
        val params = DELETE_SHOPPING_WITH_NAME_QUANTITY.find(command)?.groupValues?.filter{it.isNotBlank() && it.isNotEmpty()}?.map{it.trim()}?.toMutableList()?.apply{
            remove(command)
            removeAll{it.matches(Regex("e(n)?"))}
            removeAll{it.matches(Regex("lösch(e)*(n)*"))}
            removeAll{it.matches(Regex("(aus|von){1}"))}
        }?.takeIf { it.size == 2 } ?: command.split(" ").slice(1..2).toMutableList()
        val pseudoOut : String = params.map { it.split(" ").map { it.capitalize() }.joinToString(" ") }.joinToString(" ")

        return UserRequestedCoroutineBackgroundTask<Boolean>(context,context.getString(R.string.assistent_question_delete_all_shopping_with_name).replace("#","\"${pseudoOut}\""))
            .executeInBackground {
                val searchCount = params.first().trim().toIntOrNull()
                val searchName = params.last().trim()
                    .split(" ").map { it.capitalize() }.joinToString(" ")

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
                    Toast.makeText(context,getStringRessource(R.string.assistent_msg_shoppingitem_with_param_deleted).replace("#","\"$pseudoOut\""),
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