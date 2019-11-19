package de.madem.homium.ui.fragments.shopping

import android.app.Activity
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.ItemDao
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.Product
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.ui.activities.shoppingitem.ShoppingItemEditActivity
import de.madem.homium.utilities.switchToActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ShoppingFragment : Fragment() {

    //fields
    private lateinit var shoppingViewModel: ShoppingViewModel

    private val testData = mutableListOf<ShoppingItem>()

    private lateinit var db: AppDatabase


    override fun onAttach(context: Context) {
        super.onAttach(context)
        db = AppDatabase.getInstance(context)
    }


    //on create
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //getting viewmodel
        shoppingViewModel = ViewModelProviders.of(this).get(ShoppingViewModel::class.java)

        //getting root layout
        val root = inflater.inflate(R.layout.fragment_shopping, container, false)


        if (this.context != null){
            //recyclerview
            val shoopingItemRecyclerView = root.findViewById<RecyclerView>(R.id.recyclerView_shopping)
            buildRecyclerView(shoopingItemRecyclerView,context)


            //recyclerview test button
            val testRecyclerView = root.findViewById<Button>(R.id.btn_testRecyclerView)
            testRecyclerView.setOnClickListener {
                testData.add(0,ShoppingItem("Testitem",100, Units.KILOGRAM.getString(context!!)))
                //TODO: Datenweg sauber ziehen
                shoppingViewModel.shoppingItemList.value = testData
                shoopingItemRecyclerView.adapter?.notifyDataSetChanged()
            }

            //swipe refresh layout
            val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh_shopping)
            swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this.context!!,R.color.colorPrimaryDark))
            swipeRefresh.setOnRefreshListener {
                //TODO: Implement action for swipe-refresh
                swipeRefresh.isRefreshing = true
                testData.clear()
                shoopingItemRecyclerView.adapter?.notifyDataSetChanged()
                Toast.makeText(context!!,resources.getString(R.string.notification_remove_all_bought_shoppingitems),Toast.LENGTH_SHORT).show()
                swipeRefresh.isRefreshing = false
            }

            shoppingViewModel.reloadShoppingItems(context!!)

            shoppingViewModel.shoppingItemList.observe(this, Observer {list ->
                val adapter = (shoopingItemRecyclerView.adapter as ShoppingItemListAdapter)

                println("UPDATE: $list")

                adapter.data = list.toMutableList()
                adapter.notifyDataSetChanged()
            })
        }


        //TODO: Implement Oberserver for RecyclerView



        //floating action button
        val btnAddShoppingItem = root.findViewById<FloatingActionButton>(R.id.floatingActionButton_addShoppingItem)

        btnAddShoppingItem.setOnClickListener {
            //implementing simple navigation to shopping item edit screen via intent
            switchToActivity(ShoppingItemEditActivity::class)
        }



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    //private functions
    private fun buildRecyclerView(recyclerView : RecyclerView, context: Context?){

        if(context != null){
            //layoutmanager
            recyclerView.layoutManager = LinearLayoutManager(context)

            //adapter
            //val adapter = ShoppingItemListAdapter(testData)

            //getShoppingItems
            val adapter = ShoppingItemListAdapter(getShoppingItemList())

            recyclerView.adapter = adapter

            //onclickactions
            adapter.setOnItemClickListener { position ->
                //TODO: Implement OnClick Action for Shopping item click
                Toast.makeText(context,"OnItemClicked",Toast.LENGTH_SHORT).show()

            }

            adapter.setOnItemLongClickListener {position ->
                //giving haptic feedback
                val vib = context.getSystemService(VIBRATOR_SERVICE) as? Vibrator
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O){
                    vib?.vibrate(30)
                }
                else{
                    vib?.vibrate(VibrationEffect.createOneShot(30,10))
                }

                //TODO: Implement OnClick Action for Shopping item longclick
                Toast.makeText(context,"OnItemLongClicked",Toast.LENGTH_SHORT).show()
                return@setOnItemLongClickListener true
            }



        }

    }

    private fun getShoppingItemList(): MutableList<ShoppingItem> {
        var list = listOf<ShoppingItem>()
        GlobalScope.launch {
            list = db.itemDao().getAllShopping()
        }
        return list.toMutableList()
    }


}