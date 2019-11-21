package de.madem.homium.managers

import android.content.Context
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Product
import de.madem.homium.utilities.CoroutineBackgroundTask

class DatabaseInitializer(private val context: Context, private val doneCallback: () -> Unit) {

    private val dao = AppDatabase.getInstance(context).itemDao()
    private val backgroundTask = CoroutineBackgroundTask<Unit>()

    init {
        with(backgroundTask) {
            executeInBackground {
                deleteAllProducts()
                loadProductsFromFile()
            }
            onDone { doneCallback.invoke() }
            start()
        }
    }

    private fun deleteAllProducts() {
        dao.deleteAllProduct()
    }

    private fun loadProductsFromFile() {
        var fileReader = context.assets.open("productsCSV.csv").bufferedReader()
        var line = fileReader.readLine()
        while(line != null) {
            val splitted = line.split(";")
            dao.insertProduct(Product(splitted[0], splitted[1], splitted[2]))
            line = fileReader.readLine()
        }
        println("PRODUCTSIZE: ${dao.productSize()}")
    }

    private fun loadDummyProducts() {
        dao.insertProduct(
            Product("Apfel", "kg", "2"),
            Product("Ananas", "Stück", "1"),
            Product("Brötchen", "Stück", "5"),
            Product("Hackfleisch", "g", "500")
        )
    }
}