package de.madem.homium.errors.businesslogicerrors

open class CrudException(operation: String,type: String, itemAttrs: String) : Exception("$operation for Item with attributes ($itemAttrs) in $type failed")
class DeletionFailedException(type: String, itemAttrs: String) : CrudException("Deletion", type, itemAttrs)
class UpdateFailedException(type: String, itemAttrs: String) : CrudException("Update", type, itemAttrs)
class InsertFailedException(type: String, itemAttrs: String) : CrudException("Insert", type, itemAttrs)