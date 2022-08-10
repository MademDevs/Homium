package de.madem.homium.errors.businesslogicerrors

class NoDeletionWithNotExistingShoppingItemException
    : Exception("A not existing ShoppingItem Can not be deleted")