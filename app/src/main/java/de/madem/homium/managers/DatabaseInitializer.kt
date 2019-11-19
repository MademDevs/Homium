package de.madem.homium.managers

import android.content.Context
import android.widget.Toast
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Product
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Deprecated("Former class for init the database. Same features can now be called from CoroutineBackgroundTask")
class DatabaseInitializer(private val context: Context, private val onDone: () -> Unit) {

    private val dao = AppDatabase.getInstance(context).itemDao()

    init {
        GlobalScope.launch {
            deleteAllProducts()
            loadProductsFromFile()
            withContext(Main) { onDone() }
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