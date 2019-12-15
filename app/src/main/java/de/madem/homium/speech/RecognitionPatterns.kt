package de.madem.homium.speech

@Deprecated("Replaced by Recoginzers")
val ADD_SHOPPING_ITEM = Regex("[sS]{1}[ei]tze ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")
val ADD_SHOPPING_ITEM_WITHOUT_UNIT = Regex("[sS]{1}[ei]tze ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")
val ADD_SHOPPING_ITEM_WITHOUT_UNIT_WITHOUT_QUANTITY = Regex("[sS]{1}[ei]tze ([a-zA-ZäöüÄÖÜ( )*]+) auf die [eE]{1}inkaufsliste")
val CLEAR_SHOPPING_LIST = Regex("(lösch(e)*|(be)?reinig(e)*(n)*){1} [^ ]* [eE]{1}inkaufsliste")
val DELETE_SHOPPING_WITH_NAME = Regex("lösch(e)*( alle)? ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
val DELETE_SHOPPING_WITH_ALL_PARAMS = Regex("(lösch(e)*|welche){1} ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")
val DELETE_SHOPPING_WITH_NAME_QUANTITY = Regex("(lösch(e)*|welche){1} ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der [eE]{1}inkaufsliste( heraus)?")