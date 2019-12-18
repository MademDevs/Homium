package de.madem.homium.speech.recognizers

import android.content.Context
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units
import de.madem.homium.speech.commandparser.InventoryCommandParser
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.notNull
import de.madem.homium.utilities.showToastShort
import java.lang.ref.WeakReference

class InventoryRecognizer(val contextRef : WeakReference<Context>) : PatternRecognizer{

    private val parser = InventoryCommandParser(contextRef)
    private val dao = AppDatabase.getInstance().inventoryDao()

    companion object{
        private val unitsAsRecognitionPattern = Units.asSpeechRecognitionPattern()
        val ADD_INVENTORY_ITEM_WITHOUT_LOCATION = Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1} (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}){1} ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das)? [iI]{1}nventar(liste)?( auf)?")
        val ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT = Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1} (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das)? [iI]{1}nventar(liste)?( auf)?")
        val ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT_COUNT = Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1} ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das)? [iI]{1}nventar(liste)?( auf)?")
        //val CLEAR_INVENTORY_LIST = Regex("(lösch(e)*|(be)?reinig(e)*(n)*){1} [^ ]* [iI]{1}nventar(liste)?")
        //val DELETE_INVENTORY_WITH_NAME = Regex("lösch(e)*( alle)? ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
        //val DELETE_INVENTORY_WITH_ALL_PARAMS = Regex("(lösch(e)*|welche){1} (( )*[(0-9)]+( )*) (${unitsAsRecognitionPattern}){1} ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
        //val DELETE_INVENTORY_WITH_NAME_QUANTITY = Regex("(lösch(e)*|welche){1} (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
    }

    override fun matchingTask(command: String): CoroutineBackgroundTask<Boolean>? {
        return when{
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION) -> matchAddInventoryWithoutLocation(command)
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT) -> matchAddInventoryWithoutLocationUnit(command)
            command.matches(ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT_COUNT) -> matchAddInventoryWithoutLocationUnitCount(command)
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

            dao.insertInventoryItems(parsedItem)
            ViewRefresher.inventoryRefresher.invoke()

            return@executeInBackground true



        }.onDone {
            makeUserFeedbackAboutAdd(it)
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
            dao.insertInventoryItems(resultItem)

            //view refresh
            ViewRefresher.inventoryRefresher.invoke()

            //return
            true

        }.onDone {
            makeUserFeedbackAboutAdd(it)
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
            dao.insertInventoryItems(resultItem)

            //view refresh
            ViewRefresher.inventoryRefresher.invoke()

            //return
            true


        }.onDone {
            makeUserFeedbackAboutAdd(it)
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
}