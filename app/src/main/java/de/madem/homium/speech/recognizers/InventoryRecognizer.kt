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
import de.madem.homium.utilities.*
import java.lang.ref.WeakReference

class InventoryRecognizer(val contextRef : WeakReference<Context>) : PatternRecognizer{

    private val parser = InventoryCommandParser(contextRef)
    private val inventoryDao = AppDatabase.getInstance().inventoryDao()

    companion object{
        private val unitsAsRecognitionPattern = Units.asSpeechRecognitionPattern()
        val ADD_INVENTORY_ITEM_WITHOUT_LOCATION = Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1} (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}){1} ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das)? [iI]{1}nventar(liste)?( auf)?")
        val ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT = Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1} (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das)? [iI]{1}nventar(liste)?( auf)?")
        val ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT_COUNT = Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1} ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das)? [iI]{1}nventar(liste)?( auf)?")
        val CLEAR_INVENTORY_LIST = Regex("(lösch(e)*|(be)?reinig(e)*(n)*){1} [^ ]* [iI]{1}nventar(liste)?")
        val DELETE_INVENTORY_ITEM_WITH_NAME = Regex("lösch(e)*( alle)? ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} [derm]* [iI]{1}nventar(liste)?( heraus)?")
        //val DELETE_INVENTORY_WITH_NAME = Regex("lösch(e)*( alle)? ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
        //val DELETE_INVENTORY_WITH_ALL_PARAMS = Regex("(lösch(e)*|welche){1} (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}){1} ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
        //val DELETE_INVENTORY_WITH_NAME_QUANTITY = Regex("(lösch(e)*|welche){1} (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
    }

    override fun matchingTask(command: String): CoroutineBackgroundTask<Boolean>? {
        return when{
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION) -> matchAddInventoryWithoutLocation(command)
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT) -> matchAddInventoryWithoutLocationUnit(command)
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT_COUNT) -> matchAddInventoryWithoutLocationUnitCount(command)
            command.matches(CLEAR_INVENTORY_LIST) -> matchClearInventory()
            command.matches(DELETE_INVENTORY_ITEM_WITH_NAME) -> matchDeleteItemWithName(command)
            else -> null
        }


    }

    private fun matchAddInventoryWithoutLocation(command : String) : CoroutineBackgroundTask<Boolean>{
        return CoroutineBackgroundTask<Boolean>().executeInBackground {

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

    private fun matchAddInventoryWithoutLocationUnit(command: String) : CoroutineBackgroundTask<Boolean>{
        return CoroutineBackgroundTask<Boolean>().executeInBackground {
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

    private fun matchAddInventoryWithoutLocationUnitCount(command : String) :  CoroutineBackgroundTask<Boolean>{
        return CoroutineBackgroundTask<Boolean>().executeInBackground {
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

    private fun matchClearInventory() : CoroutineBackgroundTask<Boolean>{

        return UserRequestedCoroutineBackgroundTask<Boolean>(contextRef,R.string.assistent_question_delete_all_inventory).executeInBackground {

            if(inventoryDao.inventorySize() != 0){
                inventoryDao.clearInventory()

                true
            }
            else{
                false
            }

        }.onDone {sucess ->

            if(sucess){
                ViewRefresher.inventoryRefresher.invoke()
            }

            contextRef.get().notNull {cntxt ->
                if(sucess){
                    cntxt.showToastShort(R.string.notification_remove_all_inventoryitems)
                }
                else{
                    cntxt.showToastShort(R.string.errormsg_delete_inventory_failed)
                }
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

            deleteIndication(name,R.string.assistent_question_delete_all_shopping_with_name_and_plural,true)
        }
        else {
            deleteIndication(name,withQutationMarks = true)
        }


        return UserRequestedCoroutineBackgroundTask<Boolean>(contextRef,msg)
            .executeInBackground {

                if(!(inventoryDao.getAllInventoryItemNames().contains(name))){
                    return@executeInBackground false
                }

                if(allShouldBeDeleted){
                    val products = AppDatabase.getInstance().itemDao().getProductsByNameOrPlural(name)

                    if(products.isNotEmpty()){
                        inventoryDao.deleteInventoryItemByName(products[0].plural)
                        inventoryDao.deleteInventoryItemByName(products[0].name)
                    }
                }

                inventoryDao.deleteInventoryItemByName(name)

                true

            }.onDone {success ->

                if(success){
                    val sucessMsg = getStringRessource(R.string.assistent_msg_shoppingitem_with_param_deleted).replace("#","\"${name.capitalize()}\"")
                    contextRef.get().notNull {
                        Toast.makeText(it,sucessMsg, Toast.LENGTH_SHORT).show()
                    }
                    ViewRefresher.inventoryRefresher.invoke()
                }
                else{
                    val failMsg = getStringRessource(R.string.assistent_msg_delete_failed)
                    contextRef.get().notNull {
                        Toast.makeText(it,failMsg, Toast.LENGTH_SHORT).show()
                    }
                }

            }
    }

    //help functions
    private fun makeUserFeedbackAboutAdd(sucess: Boolean){
        contextRef.get().notNull {cntxt ->
            if(sucess){
                cntxt.showToastShort(R.string.assistent_msg_inventory_added)
            }
            else{
                cntxt.showToastShort(R.string.assistent_msg_sorry)
            }
        }
    }

    private fun deleteIndication(replacement : String, baseID : Int = R.string.assistent_question_delete_all_inventory_with_name,withQutationMarks : Boolean = false) : String{
        val base =contextRef.get()?.resources?.getString(baseID)
            ?: HomiumApplication.appContext?.getString(baseID)
            ?: "Elemente mit der Angabe # löschen?"

        return base.replace("#",if(withQutationMarks) "\"${replacement}\"" else replacement)
    }

    private fun getStringRessource(id : Int) : String{
        val cntxt = contextRef.get() ?: HomiumApplication.appContext ?: return ""

        return cntxt.resources.getString(id)
    }
}