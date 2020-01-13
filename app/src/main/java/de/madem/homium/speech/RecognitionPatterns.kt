package de.madem.homium.speech

import de.madem.homium.models.Units
// This file is now just a backup for regex pattern

@Deprecated("Replaced by Recoginzers")
// old patterns
private val ADD_SHOPPING_ITEM = Regex("[sS]{1}[ei]tze ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")
private val ADD_SHOPPING_ITEM_WITHOUT_UNIT = Regex("[sS]{1}[ei]tze ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")
private val ADD_SHOPPING_ITEM_WITHOUT_UNIT_WITHOUT_QUANTITY = Regex("[sS]{1}[ei]tze ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")
private val CLEAR_SHOPPING_LIST = Regex("(lösch(e)*|(be)?reinig(e)*(n)*){1} [^ ]* [eE]{1}inkaufsliste")
private val DELETE_SHOPPING_WITH_NAME = Regex("lösch(e)*( alle)? ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
private val DELETE_SHOPPING_WITH_ALL_PARAMS = Regex("(lösch(e)*|welche){1} ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
private val DELETE_SHOPPING_WITH_NAME_QUANTITY = Regex("(lösch(e)*|welche){1} ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")

private val ADD_INVENTORY_ITEM_WITHOUT_LOCATION = Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1} (( )*[(0-9)]+( )*) (${Units.asSpeechRecognitionPattern()}){1} ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das)? [iI]{1}nventar(liste)?( auf)?")
private val ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT = Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1} (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das)? [iI]{1}nventar(liste)?( auf)?")
private val ADD_INVENTORY_ITEM_WITHOUT_LOCATION_UNIT_COUNT = Regex("([sS]{1}[ei]tze|[nN]{1}ehme){1} ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das)? [iI]{1}nventar(liste)?( auf)?")
private val ADD_INVENTORY_ITEM = Regex("([sS]{1}[ei]tze|[lL]{1}ege){1} (( )*[(0-9)]+( )*) (${Units.asSpeechRecognitionPattern()}){1} ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das| den)? ([a-zA-ZäöüÄÖÜ( )*]+)")
private val ADD_INVENTORY_ITEM_WITHOUT_UNIT = Regex("([sS]{1}[ei]tze|[lL]{1}ege){1} (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das| den)? ([a-zA-ZäöüÄÖÜ( )*]+)")
private val ADD_INVENTORY_ITEM_WITHOUT_UNIT_COUNT = Regex("([sS]{1}[ei]tze|[lL]{1}ege){1} ([a-zA-ZäöüÄÖÜ( )*]+)( auf| in)( die| das| den)? ([a-zA-ZäöüÄÖÜ( )*]+)")
private val CLEAR_INVENTORY_LIST = Regex("(lösch(e)*|(be)?reinig(e)*(n)*){1} [^ ]* [iI]{1}nventar(liste)?")
private val DELETE_INVENTORY_ITEM_WITH_NAME = Regex("lösch(e)*( alle)? ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} [derm]* [iI]{1}nventar(liste)?( heraus)?")
private val DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY_UNIT = Regex("(lösch(e)*|welche){1} (( )*[(0-9)]+( )*) (${Units.asSpeechRecognitionPattern()}){1} ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} [derm]* [iI]{1}nventar(liste)?( heraus)?")
private val DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY = Regex("(lösch(e)*|welche){1} (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1} [derm]* [iI]{1}nventar(liste)?( heraus)?")
private val DELETE_INVENTORY_ITEM_WITH_NAME_LOCATION = Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*){1} ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1}( der| dem)? ([a-zA-ZäöüÄÖÜ( )*]+)[heraus]?")
private val DELETE_INVENTORY_ITEM_WITH_ALL_PARAMS = Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*){1} (( )*[(0-9)]+( )*) (${Units.asSpeechRecognitionPattern()}){1} ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1}( der| dem)? ([a-zA-ZäöüÄÖÜ( )*]+)[heraus]?")
private val DELETE_INVENTORY_ITEM_WITH_NAME_QUANTITY_LOCATION = Regex("(lösch(e)*|welche|entfern(e)*|nehm(e)*){1}( alle)? (( )*[(0-9)]+( )*) ([a-zA-ZäöüÄÖÜ( )*]+) (aus|von){1}( der| dem)? ([a-zA-ZäöüÄÖÜ( )*]+)[heraus]?")

//newer patterns
private val ADD_NEW_RECIPE = Regex("([eE]rstell[e]*[n]*|[eE]rzeug[e]*[n]*) \\S* \\S* Rezept( (namens|mit dem Titel) ([a-zA-ZäüöÄÜÖß ]+))?")
private val EDIT_RECIPE = Regex("([bB]earbeite[n]*[t]*|[eE]ditiere[n]*)[ das]*( Rezept)?( namens| mit dem Titel)? ([a-zA-ZäüöÄÜÖß ]+)")
private val SHOW_RECIPE = Regex("([zZ]eig[e]*[n]*|([öÖ]ffne[t]*[n]*))[ das]*( Rezept)?( namens| mit dem Titel)? ([a-zA-ZäüöÄÜÖß ]+)")
private val RECOMMEND_RECIPE = Regex("([sS]chlage[r]*( ein)?( zufälliges)? Rezept vor)|([wW]as gibt es heute zu[m]? ([aA]bendbrot|[aA]bendessen|[mM]ittag|[mM]ittagessen|[fF]rühstück|[eE]ssen)([ ]*\\?)?)")
private val COOK_RECIPE = Regex("([kK]och[e]*[n]*|[bB]ack[e]*[n]*)(( das)? [rR]ezept)?( namens| mit dem [tT]itel)? ([a-zA-ZäüöÄÜÖß ]+)")