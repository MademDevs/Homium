package de.madem.homium.speech

@Deprecated("Replaced by Recoginzers")
val ADD_SHOPPING_ITEM = Regex("s[ei]tze ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) ([a-zA-ZäöüÄÖÜ]+) auf die einkaufsliste")
val ADD_SHOPPING_ITEM_WITHOUT_UNIT = Regex("s[ei]tze ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) auf die einkaufsliste")
val ADD_SHOPPING_ITEM_WITHOUT_UNIT_WITHOUT_QUANTITY = Regex("s[ei]tze ([a-zA-ZäöüÄÖÜ]+) auf die einkaufsliste")
val CLEAR_SHOPPING_LIST = Regex("(lösch(e)*|bereinig(e)*){1} [^ ]* einkaufsliste")
val DELETE_SHOPPING_WITH_NAME = Regex("lösch(e)*( alle)? ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der einkaufsliste( heraus)?")
val DELETE_SHOPPING_WITH_ALL_PARAMS = Regex("(lösch(e)*|welche){1} ([(a-z)(0-9)]+) ([a-zA-ZäöüÄÖÜ]+) ([a-zA-ZäöüÄÖÜ]+) (aus|von){1} der einkaufsliste( heraus)?")