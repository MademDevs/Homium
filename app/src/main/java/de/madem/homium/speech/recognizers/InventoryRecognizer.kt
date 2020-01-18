package de.madem.homium.speech.recognizers

import android.content.Context
import android.widget.Toast
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units
import de.madem.homium.speech.commandparser.InventoryCommandParser
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.backgroundtasks.UserRequestedCoroutineBackgroundTask
import de.madem.homium.utilities.extensions.capitalizeEachWord
import de.madem.homium.utilities.extensions.getSetting
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.showToastShort
import java.lang.ref.WeakReference

class InventoryRecognizer(private val contextRef : WeakReference<Context>) : PatternRecognizer{

    private val parser = InventoryCommandParser(contextRef)
    private val inventoryDao = AppDatabase.getInstance().inventoryDao()

    companion object{
        private val unitsAsRecognitionPattern = Units.asSpeechRecognitionPattern().also { println(it) }
        private val ADD_INVENTORY_ITEM_WITHOUT_LOCATION = Regex("([sS][ei]tze|[nN]ehme) (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}) ([a-zA-ZäöüÄÖÜß( )*]+)( auf| in)( die| das)? [iI]nventar(liste)?( auf)?")
        private val ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT = Regex("([sS][ei]tze|[nN]ehme) (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜß( )*]+)( auf| in)( die| das)? [iI]nventar(liste)?( auf)?")
        private val ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT_COUNT = Regex("([sS][ei]tze|[nN]ehme) ([a-zA-ZäöüÄÖÜß( )*]+)( auf| in)( die| das)? [iI]nventar(liste)?( auf)?")
        private val ADD_INVENTORY_ITEM = Regex("([sS][ei]tze|[lL]ege) (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}) ([a-zA-ZäöüÄÖÜß( )*]+)( auf| in)( die| das| den)? ([a-zA-ZäöüÄÖÜß( )*]+)")
        private val ADD_INVENTORY_ITEM_WITHOUT_UNIT = Regex("([sS][ei]tze|[lL]ege) (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜß( )*]+)( auf| in)( die| das| den)? ([a-zA-ZäöüÄÖÜß( )*]+)")
        private val ADD_INVENTORY_ITEM_WITHOUT_UNIT_COUNT = Regex("([sS][ei]tze|[lL]ege) ([a-zA-ZäöüÄÖÜß( )*]+)( auf| in)( die| das| den)? ([a-zA-ZäöüÄÖÜß( )*]+)")
        private val CLEAR_INVENTORY_LIST = Regex("(lösch(e)*|(be)?reinig(e)*(n)*) [^ ]* [iI]nventar(liste)?")
        private val DELETE_INVENTORY_ITEM_WITH_NAME = Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*)( alle)? ([a-zA-ZäöüÄÖÜß( )*]+) (aus|von)[ derm]* [iI]nventar(liste)?( heraus)?")
        private val DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY_UNIT = Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*) (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}) ([a-zA-ZäöüÄÖÜß( )*]+) (aus|von)[ derm]* [iI]nventar(liste)?( heraus)?")
        private val DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY = Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*) (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜß( )*]+) (aus|von)[ derm]* [iI]nventar(liste)?( heraus)?")
        private val DELETE_INVENTORY_ITEM_WITH_NAME_LOCATION = Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*) ([a-zA-ZäöüÄÖÜß( )*]+) (aus|von)[ derm]* ([a-zA-ZäöüÄÖÜß( )*]+)[heraus]?")
        private val DELETE_INVENTORY_ITEM_WITH_ALL_PARAMS = Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*) (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}) ([a-zA-ZäöüÄÖÜß( )*]+) (aus|von)[ derm]* ([a-zA-ZäöüÄÖÜß( )*]+)[heraus]?")
        private val DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY_LOCATION = Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*)( alle)? (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜß( )*]+) (aus|von)[ derm]* ([a-zA-ZäöüÄÖÜß( )*]+)[heraus]?")
        private val CLEAR_LOCATION = Regex("(lösch(e)*|(be)?reinig(e)*(n)*|leere|lehre)( alles)?( aus| von)? [derniasm]*[ ]*([a-zA-ZäöüÄÖÜß( )*]+)")
    }

    override fun matchingTask(command: String): CoroutineBackgroundTask<Boolean>? {
        return when{
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION) -> matchAddInventoryWithoutLocation(command)
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT) -> matchAddInventoryWithoutLocationUnit(command)
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT_COUNT) -> matchAddInventoryWithoutLocationUnitCount(command)
            command.matches(ADD_INVENTORY_ITEM) -> matchAddInventory(command)
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_UNIT) -> matchAddInventoryWithoutUnit(command)
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_UNIT_COUNT) -> matchAddInventoryWithoutUnitCount(command)
            command.matches(CLEAR_INVENTORY_LIST) -> matchClearInventory()
            command.matches(DELETE_INVENTORY_ITEM_WITH_NAME) -> matchDeleteItemWithName(command)
            command.matches(DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY_UNIT) -> matchDeleteItemWithNameQuantityUnit(command)
            command.matches(DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY) -> matchDeleteItemWithNameQuantity(command)
            command.matches(DELETE_INVENTORY_ITEM_WITH_NAME_LOCATION) -> matchDeleteItemWithNameLocation(command)
            command.matches(DELETE_INVENTORY_ITEM_WITH_ALL_PARAMS) -> matchDeleteItemWithAllParams(command)
            command.matches(DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY_LOCATION) -> matchDeleteItemWithNameQuantityLocation(command)
            command.matches(CLEAR_LOCATION) -> matchClearLocation(command)
            else -> null
        }


    }

    private fun matchAddInventoryWithoutLocation(command : String) : CoroutineBackgroundTask<Boolean> {
        return CoroutineBackgroundTask<Boolean>()
            .executeInBackground {

            val params = ADD_INVENTORY_ITEM_WITHOUT_LOCATION.find(command)?.groupValues
                ?.asSequence()
                ?.map{it.trim().toLowerCase()}
                ?.filter{it.isNotBlank() && it.isNotEmpty()}
                ?.filter{!(it.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION))}
                ?.filter{!(it.matches(Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1}")))}
                ?.filter{
                    it != "der"
                            && it != "die"
                            && it != "das"
                            && it != "auf"
                            && it != "in"
                            && it != "liste"
                }
                ?.filter {
                    it != "en"
                }
                ?.toMutableList()
                ?.apply {
                    if(get(1).toLowerCase() == Units.KILOGRAM.getString().toLowerCase()){
                        remove("gramm")
                    }
                }
                ?.takeIf { it.size == 3 } ?: return@executeInBackground false

            val parsedItem = parser.parseInventoryWithoutLocation(params) ?: return@executeInBackground false

            inventoryDao.insertInventoryItems(parsedItem)


            return@executeInBackground true



        }.onDone {success ->
            if(success){
                ViewRefresher.inventoryRefresher.invoke()
            }

            makeUserFeedbackAboutAdd(success)
        }
    }

    private fun matchAddInventoryWithoutLocationUnit(command: String) : CoroutineBackgroundTask<Boolean> {
        return CoroutineBackgroundTask<Boolean>()
            .executeInBackground {
            //getting params
            val params = ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT.find(command)?.groupValues
                ?.asSequence()
                ?.map{it.trim().toLowerCase()}
                ?.filter{it.isNotBlank() && it.isNotEmpty()}
                ?.filter{!(it.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT))}
                ?.filter{!(it.matches(Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1}")))}
                ?.filter{
                    it != "der"
                            && it != "die"
                            && it != "das"
                            && it != "auf"
                            && it != "in"
                            && it != "liste"
                }
                ?.toList().also { println(it) }
                ?.takeIf { it.size == 2 } ?: return@executeInBackground false

            //parsing item
            val resultItem = parser.parseInventoryWithoutLocationUnit(params) ?: return@executeInBackground false

            //inserting item
            inventoryDao.insertInventoryItems(resultItem)

            //return
            true

        }.onDone {success ->

            if(success){
                //view refresh
                ViewRefresher.inventoryRefresher.invoke()
            }

            makeUserFeedbackAboutAdd(success)
        }
    }

    private fun matchAddInventoryWithoutLocationUnitCount(command : String) : CoroutineBackgroundTask<Boolean> {
        return CoroutineBackgroundTask<Boolean>()
            .executeInBackground {
            //getting params
            val params = ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT_COUNT.find(command)?.groupValues
                ?.asSequence()
                ?.map{it.trim().toLowerCase()}
                ?.filter{it.isNotBlank() && it.isNotEmpty()}
                ?.filter{!(it.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT_COUNT))}
                ?.filter{!(it.matches(Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1}")))}
                ?.filter{
                    it != "der"
                            && it != "die"
                            && it != "das"
                            && it != "auf"
                            && it != "in"
                            && it != "liste"
                }
                ?.toList()?.takeIf { it.size == 1 } ?: return@executeInBackground false

            //getting inventoryitem
            val resultItem : InventoryItem = parser.parseInventoryWithoutLocationUnitCount(params[0])
                ?: return@executeInBackground false

            //insert into db
            inventoryDao.insertInventoryItems(resultItem)



            //return
            true


        }.onDone {sucess ->

            if(sucess){
                //view refresh
                ViewRefresher.inventoryRefresher.invoke()
            }

            makeUserFeedbackAboutAdd(sucess)


        }
    }

    private fun matchAddInventory(command: String) : CoroutineBackgroundTask<Boolean> {
        return CoroutineBackgroundTask<Boolean>()
            .executeInBackground {
            val params = ADD_INVENTORY_ITEM.find(command)?.groupValues
                ?.asSequence()
                ?.map{it.trim()}
                ?.filter{ it.isNotEmpty() && it.isNotBlank() }
                ?.filterIndexed{index, _ -> index != 0 }
                ?.filter{ !(it.matches(Regex("([sS]{1}[ei]tze|[lL]{1}ege){1}"))) }
                ?.filter{ !(it.matches(Regex("(auf|in|die|das|den)?"))) }
                ?.toList()?.takeIf { it.size == 4 } ?: return@executeInBackground false

            val resultItem = parser.parseInventory(args=params) ?: return@executeInBackground false

            inventoryDao.insertInventoryItems(resultItem)

            true
        }.onDone {success ->
            if(success){
                //view refresh
                ViewRefresher.inventoryRefresher.invoke()
            }

            makeUserFeedbackAboutAdd(success)
        }
    }

    private fun matchAddInventoryWithoutUnit(command: String) : CoroutineBackgroundTask<Boolean> {
        return CoroutineBackgroundTask<Boolean>()
            .executeInBackground {
            val params = ADD_INVENTORY_ITEM_WITHOUT_UNIT.find(command)?.groupValues
                ?.asSequence()
                ?.map{it.trim()}
                ?.filter{ it.isNotEmpty() && it.isNotBlank() }
                ?.filterIndexed{index, _ -> index != 0 }
                ?.filter{ !(it.matches(Regex("([sS]{1}[ei]tze|[lL]{1}ege){1}"))) }
                ?.filter{ !(it.matches(Regex("(auf|in|die|das|den)?"))) }
                ?.toList()?.takeIf { it.size == 3 } ?: return@executeInBackground false

            val resultItem = parser.parseInventoryWithoutUnit(params) ?: return@executeInBackground false

            inventoryDao.insertInventoryItems(resultItem)


            true
        }.onDone {success ->
            if(success){
                //view refresh
                ViewRefresher.inventoryRefresher.invoke()
            }

            makeUserFeedbackAboutAdd(success)
        }
    }

    private fun matchAddInventoryWithoutUnitCount(command: String) : CoroutineBackgroundTask<Boolean> {
        return CoroutineBackgroundTask<Boolean>()
            .executeInBackground {
            val params = ADD_INVENTORY_ITEM_WITHOUT_UNIT_COUNT.find(command)?.groupValues
                ?.asSequence()
                ?.map{it.trim()}
                ?.filter{ it.isNotEmpty() && it.isNotBlank() }
                ?.filterIndexed{index, _ -> index != 0 }
                ?.filter{ !(it.matches(Regex("([sS]{1}[ei]tze|[lL]{1}ege){1}"))) }
                ?.filter{ !(it.matches(Regex("(auf|in|die|das|den)?"))) }
                ?.toList()?.takeIf { it.size == 2 } ?: return@executeInBackground false


            val resultItem = parser.parserInventoryWithoutUnitCount(params) ?: return@executeInBackground false

            inventoryDao.insertInventoryItems(resultItem)

            true

        }.onDone {success ->
            if(success){
                //view refresh
                ViewRefresher.inventoryRefresher.invoke()
            }

            makeUserFeedbackAboutAdd(success)
        }
    }



    private fun matchClearInventory() : CoroutineBackgroundTask<Boolean> {

        //creating functions
        val backgroundFunc : () -> Boolean = {
            if(inventoryDao.inventorySize() != 0){
                inventoryDao.clearInventory()

                true
            }
            else{
                false
            }
        }

        val onDoneFunc : (Boolean) -> Unit = {success ->
            if(success){
                ViewRefresher.inventoryRefresher.invoke()
            }

            contextRef.get().notNull {cntxt ->
                if(success){
                    cntxt.showToastShort(R.string.notification_remove_all_inventoryitems)
                }
                else{
                    cntxt.showToastShort(R.string.errormsg_delete_inventory_failed)
                }
            }

        }


        //returning task
        return if(shouldAskDeleteQuestion()){
            UserRequestedCoroutineBackgroundTask<Boolean>(
                contextRef,
                R.string.assistent_question_delete_all_inventory
            ).executeInBackground {
                backgroundFunc.invoke()
            }.onDone {success ->
                onDoneFunc.invoke(success)
            }
        }
        else{
            CoroutineBackgroundTask<Boolean>()
                .executeInBackground {
                backgroundFunc.invoke()
            }.onDone {success ->
                onDoneFunc.invoke(success)
            }
        }
    }

    private fun matchDeleteItemWithName(command : String) : CoroutineBackgroundTask<Boolean>?{
        val params = DELETE_INVENTORY_ITEM_WITH_NAME.find(command)?.groupValues
            ?.asSequence()
            ?.map{it.trim().toLowerCase()}
            ?.filter{it.isNotBlank() && it.isNotEmpty()}
            ?.filter{!(it.matches(DELETE_INVENTORY_ITEM_WITH_NAME))}
            ?.filter{!(it.matches(Regex("(e)*")))}
            ?.filter{!(it.matches(Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*)")))}
            ?.filter{!(it.matches(Regex("(aus|von){1}")))}
            ?.filter{
                it != "der"
                        && it != "die"
                        && it != "das"
                        && it != "dem"
                        && it != "auf"
                        && it != "in"
                        && it != "liste"
                        && it != "alle"
                        && it != "heraus"
            }
            ?.toList()?.takeIf { it.size == 1 } ?: return null

        val name = params[0].capitalizeEachWord()

        val allShouldBeDeleted = command.contains("alle")


        val msg = if(allShouldBeDeleted){

            createDeleteMessage(name,R.string.assistent_question_delete_all_shopping_with_name_and_plural,true)
        }
        else {
            createDeleteMessage(name,withQutationMarks = true)
        }

        //creating functions
        val backgroundFunc : () -> Boolean = backgroundFunc@{
            if(!(inventoryDao.getAllInventoryItemNames().contains(name))){
                return@backgroundFunc false
            }

            if(allShouldBeDeleted){
                val products = AppDatabase.getInstance().itemDao().getProductsByNameOrPlural(name)

                if(products.isNotEmpty()){
                    inventoryDao.deleteInventoryItemByName(products[0].plural)
                    inventoryDao.deleteInventoryItemByName(products[0].name)
                }
            }

            inventoryDao.deleteInventoryItemByName(name)

            return@backgroundFunc true
        }

        val onDoneFunc : (Boolean) -> Unit = {success ->
            if(success){
                val sucessMsg = getStringResource(R.string.assistent_msg_shoppingitem_with_param_deleted)
                    .replace("#","\"${name.capitalizeEachWord()}\"")
                contextRef.get().notNull {
                    Toast.makeText(it,sucessMsg, Toast.LENGTH_SHORT).show()
                }
                ViewRefresher.inventoryRefresher.invoke()
            }
            else{
                val failMsg = getStringResource(R.string.assistent_msg_delete_failed)
                contextRef.get().notNull {
                    Toast.makeText(it,failMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        //returning right task
        return if(shouldAskDeleteQuestion()){
            UserRequestedCoroutineBackgroundTask<Boolean>(
                contextRef,
                msg
            )
                .executeInBackground {
                    backgroundFunc.invoke()
                }.onDone {success ->
                    onDoneFunc(success)
                }
        }
        else{
            CoroutineBackgroundTask<Boolean>()
                .executeInBackground {
                    backgroundFunc.invoke()
                }.onDone {success ->
                    onDoneFunc(success)
                }
        }
    }

    private fun matchDeleteItemWithNameQuantityUnit(command : String) : CoroutineBackgroundTask<Boolean>?{
        val params = DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY_UNIT.find(command)?.groupValues
            ?.asSequence()
            ?.map{it.trim().toLowerCase()}
            ?.filter{it.isNotBlank() && it.isNotEmpty()}
            ?.filter{!(it.matches(DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY_UNIT))}
            ?.filter{!(it.matches(Regex("e(n)?")))}
            ?.filter{!(it.matches(Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*)")))}
            ?.filter{!(it.matches(Regex("(aus|von){1}")))}
            ?.filter{
                it != "der"
                        && it != "die"
                        && it != "das"
                        && it != "dem"
                        && it != "auf"
                        && it != "in"
                        && it != "liste"
                        && it != "alle"
                        && it != "heraus"
            }
            ?.toMutableList()
            ?.apply {
                if(get(1).toLowerCase() == Units.KILOGRAM.getString().toLowerCase()){
                    remove("gramm")
                }
            }?.takeIf { it.size == 3 } ?: return null

        val itemStr = params.joinToString(" ") { it.capitalizeEachWord() }
        val msg = createDeleteMessage(replacement = itemStr,withQutationMarks = true)

        //creating functions
        val backgroundFunc : () -> Boolean = backgroundFunc@{
            if(!(inventoryDao.getAllInventoryItemNames().contains(params[2].capitalizeEachWord()))){
                return@backgroundFunc false
            }

            val item : InventoryItem?= parser.parseInventoryWithoutLocation(params)

            return@backgroundFunc if(item != null){
                inventoryDao.deleteInventoryByNameCountUnit(item.name,item.count,item.unit)
                true
            }
            else{
                false
            }
        }

        val onDoneFunc : (Boolean) -> Unit = {success ->
            if(success){
                contextRef.get().notNull {
                    Toast.makeText(it,getStringResource(R.string.assistent_msg_inventoryitem_with_param_deleted).replace("#","\"$itemStr\""),
                        Toast.LENGTH_SHORT).show();
                }
                ViewRefresher.inventoryRefresher.invoke()
            }
            else{
                contextRef.get().notNull {
                    it.showToastShort(R.string.errormsg_delete_inventory_failed)
                }
            }
        }

        return if(shouldAskDeleteQuestion()){
            UserRequestedCoroutineBackgroundTask<Boolean>(
                contextRef,
                msg
            ).executeInBackground {
                backgroundFunc()
            }.onDone { success ->
                onDoneFunc(success)
            }
        }
        else{
            CoroutineBackgroundTask<Boolean>()
                .executeInBackground {
                backgroundFunc()
            }.onDone { success ->
                onDoneFunc(success)
            }
        }
    }

    private fun matchDeleteItemWithNameQuantity(command : String) : CoroutineBackgroundTask<Boolean>?{
        val params = DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY.find(command)?.groupValues
            ?.asSequence()
            ?.map{it.trim().toLowerCase()}
            ?.filter{it.isNotBlank() && it.isNotEmpty()}
            ?.filter{!(it.matches(DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY))}
            ?.filter{!(it.matches(Regex("e(n)?")))}
            ?.filter{!(it.matches(Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*)")))}
            ?.filter{!(it.matches(Regex("(aus|von){1}")))}
            ?.filter{
                it != "der"
                        && it != "die"
                        && it != "das"
                        && it != "dem"
                        && it != "auf"
                        && it != "in"
                        && it != "liste"
                        && it != "alle"
                        && it != "heraus"
            }
            ?.toList()?.takeIf { it.size == 2 } ?: return null

        val itemStr = params.map { it.capitalizeEachWord() }.joinToString(" ")

        //creating functions
        val backgroundFunc : () -> Boolean = backgroundFunc@{
            val searchCount = params.first().trim().toIntOrNull()
            val searchName = params.last().trim().capitalizeEachWord()

            if(!(inventoryDao.getAllInventoryItemNames().contains(searchName))){
                return@backgroundFunc false
            }

            return@backgroundFunc if(searchCount != null){
                inventoryDao.deleteInventoryByNameCount(searchName,searchCount)
                true
            }
            else{
                false
            }

        }

        val onDoneFunc : (Boolean) -> Unit = {success ->
            if(success){
                contextRef.get().notNull {
                    Toast.makeText(it,getStringResource(R.string.assistent_msg_inventoryitem_with_param_deleted).replace("#","\"$itemStr\""),
                        Toast.LENGTH_SHORT).show();
                }
                ViewRefresher.inventoryRefresher.invoke()
            }
            else{
                contextRef.get().notNull {
                    it.showToastShort(R.string.errormsg_delete_inventory_failed)
                }
            }
        }

        //returning right task
        return if(shouldAskDeleteQuestion()){
            UserRequestedCoroutineBackgroundTask<Boolean>(
                contextRef,
                createDeleteMessage(replacement = itemStr, withQutationMarks = true)
            )
                .executeInBackground {
                    backgroundFunc()
                }
                .onDone {success ->
                    onDoneFunc(success)
                }
        }
        else{
            CoroutineBackgroundTask<Boolean>()
                .executeInBackground {
                    backgroundFunc()
                }
                .onDone {success ->
                    onDoneFunc(success)
                }
        }


    }

    private fun matchDeleteItemWithNameLocation(command: String) : CoroutineBackgroundTask<Boolean>?{
        val params : List<String> = DELETE_INVENTORY_ITEM_WITH_NAME_LOCATION.find(command)?.groupValues
            ?.asSequence()
            ?.map{it.trim()}
            ?.filter{it.isNotEmpty() && it.isNotBlank()}
            ?.filterIndexed{index,_ -> index != 0 }
            ?.filter{!(it.matches(Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*){1}")))}
            ?.filterIndexed{index, str -> !(index == 0 && str.matches(Regex("(e)*")))}
            ?.filter{!(it.matches(Regex("(aus|von){1}")))}
            ?.filter{!(it.matches(Regex("(der|dem)?")))}
            ?.toMutableList()
            ?.apply{
                if(last().contains(" heraus")){
                    this[lastIndex] = last().replace(" heraus","")
                }
            }?.takeIf { it.size == 2 } ?: return null


        //creating message
        val name = params.first().capitalizeEachWord()
        val location = params.last().capitalizeEachWord()

        val allShouldBeDeleted = command.contains("alle")


        val msg = if(allShouldBeDeleted){

            createDeleteMessageWithLocation(name,location,R.string.assistent_question_delete_all_inventory_with_name_and_plural,true)
        }
        else {
            createDeleteMessageWithLocation(name,location,withQutationMarks = true)
        }

        //creating functions
        val backgroundFunc : () -> Boolean = backgroundFunc@{
            if(!(inventoryDao.getAllInventoryItemNames().contains(name))
                && !(inventoryDao.getAllInventoryLocations().contains(location))){
                return@backgroundFunc false
            }

            if(allShouldBeDeleted){
                val products = AppDatabase.getInstance().itemDao().getProductsByNameOrPlural(name)

                if(products.isNotEmpty()){
                    inventoryDao.deleteInventoryItemByNameLocation(products[0].plural,location)
                    inventoryDao.deleteInventoryItemByNameLocation(products[0].name,location)
                }
            }

            inventoryDao.deleteInventoryItemByNameLocation(name,location)

            return@backgroundFunc true
        }

        val onDoneFunc : (Boolean) -> Unit = {success ->
            if(success){
                val sucessMsg = getStringResource(R.string.assistent_msg_inventoryitem_with_param_deleted_with_location)
                    .replace("#","\"${name.capitalizeEachWord()}\"")
                    .replace("~","\"${location.capitalizeEachWord()}\"")

                contextRef.get().notNull {
                    Toast.makeText(it,sucessMsg, Toast.LENGTH_SHORT).show()
                }
                ViewRefresher.inventoryRefresher.invoke()
            }
            else{
                val failMsg = getStringResource(R.string.assistent_msg_delete_failed)
                contextRef.get().notNull {
                    Toast.makeText(it,failMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        //returning right task
        return if(shouldAskDeleteQuestion()){
            UserRequestedCoroutineBackgroundTask<Boolean>(
                contextRef,
                msg
            )
                .executeInBackground {
                    backgroundFunc.invoke()
                }.onDone {success ->
                    onDoneFunc(success)
                }
        }
        else{
            CoroutineBackgroundTask<Boolean>()
                .executeInBackground {
                    backgroundFunc.invoke()
                }.onDone {success ->
                    onDoneFunc(success)
                }
        }


    }

    private fun matchDeleteItemWithAllParams(command: String) : CoroutineBackgroundTask<Boolean>?{
        val params = DELETE_INVENTORY_ITEM_WITH_ALL_PARAMS.find(command)?.groupValues
            ?.asSequence()
            ?.map{it.trim()}
            ?.map{it.replace(" heraus","")}
            ?.filter{ it.isNotEmpty() && it.isNotBlank() }
            ?.filterIndexed{index, _ -> index != 0 }
            ?.filter{ !(it.matches(Regex("(e)*"))) }
            ?.filter{ !(it.matches(Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*){1}"))) }
            ?.filter{ !(it.matches(Regex("(aus|von|der|dem)?"))) }
            ?.toMutableList()
            ?.apply {
                if(get(1).toLowerCase() == Units.KILOGRAM.getString().toLowerCase()){
                    remove("gramm")
                }
            }
            ?.takeIf { it.size == 4 } ?: return null

        val itemStr = params.map { it.capitalizeEachWord() }.slice(0..2).joinToString(" ")
        val msg = createDeleteMessageWithLocation(replacement = itemStr,location = params.last().capitalizeEachWord()
            ,withQutationMarks = true)

        //creating functions
        val backgroundFunc : () -> Boolean = backgroundFunc@{
            if(!(inventoryDao.getAllInventoryItemNames().contains(params[2].capitalizeEachWord()))){
                return@backgroundFunc false
            }

            val item : InventoryItem?= parser.parseInventory(params)

            return@backgroundFunc if(item != null){
                inventoryDao.deleteInventory(item.name,item.count,item.unit,item.location)
                true
            }
            else{
                false
            }
        }

        val onDoneFunc : (Boolean) -> Unit = {success ->
            if(success){
                contextRef.get().notNull {
                    Toast.makeText(it,getStringResource(R.string.assistent_msg_inventoryitem_with_param_deleted_with_location)
                        .replace("#","\"$itemStr\"")
                        .replace("~","\"${params[3].capitalizeEachWord()}\""),
                        Toast.LENGTH_SHORT)
                        .show()
                }
                ViewRefresher.inventoryRefresher.invoke()
            }
            else{
                contextRef.get().notNull {
                    it.showToastShort(R.string.errormsg_delete_inventory_failed)
                }
            }
        }

        return if(shouldAskDeleteQuestion()){
            UserRequestedCoroutineBackgroundTask<Boolean>(
                contextRef,
                msg
            ).executeInBackground {
                backgroundFunc()
            }.onDone { success ->
                onDoneFunc(success)
            }
        }
        else{
            CoroutineBackgroundTask<Boolean>()
                .executeInBackground {
                backgroundFunc()
            }.onDone { success ->
                onDoneFunc(success)
            }
        }


    }

    private fun matchDeleteItemWithNameQuantityLocation(command : String) : CoroutineBackgroundTask<Boolean>?{
        val params : List<String> = DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY_LOCATION.find(command)?.groupValues
            ?.asSequence()
            ?.map{it.trim()}
            ?.filter{it.isNotEmpty() && it.isNotBlank()}
            ?.filterIndexed{index,_ -> index != 0 }
            ?.filter{!(it.matches(Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*){1}")))}
            ?.filterIndexed{index, str -> !(index == 0 && str.matches(Regex("(e)*")))}
            ?.filter{!(it.matches(Regex("(aus|von){1}")))}
            ?.filter{!(it.matches(Regex("(der|dem)?")))}
            ?.toMutableList()
            ?.apply{
                if(last().contains(" heraus")){
                    this[this.lastIndex] = last().replace(" heraus","")
                }
            }?.takeIf { it.size == 3 } ?: return null


        //creating message for user
        val itemStr = params.slice(0..1).joinToString(" ") { it.capitalizeEachWord() }

        val msg = createDeleteMessageWithLocation(replacement = itemStr,location = params.last().capitalizeEachWord(),
            withQutationMarks = true)

        //creating functions
        val backgroundFunc : () -> Boolean = backgroundFunc@{
            val searchCount = params.first().trim().toIntOrNull()
            val searchName = params[1].trim().capitalizeEachWord()
            val searchLocation = params.last().trim()

            if(!(inventoryDao.getAllInventoryItemNames().contains(searchName))
                && !(inventoryDao.getAllInventoryLocations().contains(searchLocation))){
                return@backgroundFunc false
            }

            return@backgroundFunc if(searchCount != null){
                inventoryDao.deleteInventoryByNameCountLocation(searchName,searchCount,searchLocation)
                true
            }
            else{
                false
            }

        }

        val onDoneFunc : (Boolean) -> Unit = {success ->
            if(success){
                contextRef.get().notNull {
                    Toast.makeText(it,getStringResource(R.string.assistent_msg_inventoryitem_with_param_deleted).replace("#","\"$itemStr\""),
                        Toast.LENGTH_SHORT).show();
                }
                ViewRefresher.inventoryRefresher.invoke()
            }
            else{
                contextRef.get().notNull {
                    it.showToastShort(R.string.errormsg_delete_inventory_failed)
                }
            }
        }

        //returning right task
        return if(shouldAskDeleteQuestion()){
            UserRequestedCoroutineBackgroundTask<Boolean>(
                contextRef,
                msg
            )
                .executeInBackground {
                    backgroundFunc()
                }
                .onDone {success ->
                    onDoneFunc(success)
                }
        }
        else{
            CoroutineBackgroundTask<Boolean>()
                .executeInBackground {
                    backgroundFunc()
                }
                .onDone {success ->
                    onDoneFunc(success)
                }
        }

    }


    private fun matchClearLocation(command : String) : CoroutineBackgroundTask<Boolean>?{
        //getting location
        val params : List<String> = CLEAR_LOCATION.find(command)?.groupValues
            ?.asSequence()
            ?.map{it.trim()}
            ?.filterIndexed{index, _ -> index != 0}
            ?.filter{it.isNotEmpty() && it.isNotBlank()}
            ?.filter{!(it.matches(Regex("(alles|aus|von)?")))}
            ?.filter{!(it.matches(Regex("(lösch(e)*|(be)?reinig(e)*(n)*|leere|lehre)")))}
            ?.map{if(it.contains(" heraus")) it.replace(" heraus","") else it}
            ?.toMutableList()
            ?.apply{
                var v = lastIndex

                while(v > 0){
                    if(get(v-1).matches(Regex("(e|be|n)*"))){
                        removeAt(v-1)
                    }
                    v = lastIndex

                }
            }?.takeIf { it.size == 1 } ?: return null


        val location = params[0].trim().capitalizeEachWord()

        //creating message
        val question = getStringResource(R.string.assistent_question_clear_location)
            .replace("~", "\"$location\"")

        //creating functions
        val backgroundFunc : () -> Boolean = {

            if(inventoryDao.getAllInventoryLocations().contains(location)){
                inventoryDao.clearLocation(location)
                true
            }
            else{
                false
            }
        }

        val onDoneFunc : (Boolean) -> Unit = {success ->
            if (success){
                contextRef.get().notNull {
                    it.showToastShort(getStringResource(R.string.assistent_msg_clear_location)
                        .replace("~","\"$location\""))
                }

                ViewRefresher.inventoryRefresher.invoke()
            }
            else{
                contextRef.get().notNull {
                    it.showToastShort(R.string.errormsg_delete_inventory_failed)
                }
            }
        }

        return if(shouldAskDeleteQuestion()){
            UserRequestedCoroutineBackgroundTask<Boolean>(
                contextRef,
                question
            ).executeInBackground {
                backgroundFunc()
            }.onDone { success ->
                onDoneFunc(success)
            }
        }
        else{
            CoroutineBackgroundTask<Boolean>()
                .executeInBackground {
                backgroundFunc()
            }.onDone { success ->
                onDoneFunc(success)
            }
        }
    }

    //help functions
    private fun makeUserFeedbackAboutAdd(success: Boolean){
        contextRef.get().notNull {cntxt ->
            if(success){
                cntxt.showToastShort(R.string.assistent_msg_inventory_added)
            }
            else{
                cntxt.showToastShort(R.string.assistent_msg_sorry)
            }
        }
    }

    private fun shouldAskDeleteQuestion() : Boolean{
        return contextRef.get()?.getSetting(getStringResource(R.string.sharedpreference_settings_preferencekey_deleteQuestionSpeechAssistentAllowed),Boolean::class) ?: true
    }

    private fun createDeleteMessage(replacement : String, baseID : Int = R.string.assistent_question_delete_all_inventory_with_name, withQutationMarks : Boolean = false) : String{
        val base =contextRef.get()?.resources?.getString(baseID)
            ?: HomiumApplication.appContext?.getString(baseID)
            ?: "Elemente mit der Angabe # löschen?"

        return base.replace("#",if(withQutationMarks) "\"${replacement}\"" else replacement)
    }

    private fun createDeleteMessageWithLocation(replacement : String, location : String, baseID : Int = R.string.assistent_question_delete_all_inventory_with_location, withQutationMarks : Boolean = false) : String{
        val base =contextRef.get()?.resources?.getString(baseID)
            ?: HomiumApplication.appContext?.getString(baseID)
            ?: "Elemente mit der Angabe #  aus dem Ort ~ löschen?"

        return base.replace("#",if(withQutationMarks) "\"${replacement}\"" else replacement)
                   .replace("~",if(withQutationMarks) "\"${location}\"" else location)
    }

    private fun getStringResource(id : Int) : String{
        val cntxt = contextRef.get() ?: HomiumApplication.appContext ?: return ""

        return cntxt.resources.getString(id)
    }


}