package de.madem.homium.ui.fragments.shopping

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.ShoppingItem
import de.madem.homium.ui.activities.shoppingitem.ShoppingItemEditActivity
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.switchToActivity
import de.madem.homium.utilities.vibrate

class ShoppingFragment : Fragment() {

    //fields
    private lateinit var shoppingViewModel: ShoppingViewModel

    private lateinit var db: AppDatabase
    private lateinit var shoopingItemRecyclerView: RecyclerView


    override fun onAttach(context: Context) {
        super.onAttach(context)
        db = AppDatabase.getInstance(context)
    }

    override fun onResume() {
        super.onResume()
        this.reloadShoppingItems()
    }

    private fun reloadShoppingItems() {
        CoroutineBackgroundTask<List<ShoppingItem>>()
            .executeInBackground { AppDatabase.getInstance(context!!).itemDao().getAllShopping() }
            .onDone {
                val tmpAdapter = shoopingItemRecyclerView.adapter as ShoppingItemListAdapter
                tmpAdapter.data = it.toMutableList()
                tmpAdapter.notifyDataSetChanged()
            }
            .start()
    }

    //on create
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //getting viewmodel
        shoppingViewModel = ViewModelProviders.of(this).get(ShoppingViewModel::class.java)


        //getting root layout
        val root = inflater.inflate(R.layout.fragment_shopping, container, false)


        if (this.context != null){
            //recyclerview
            shoopingItemRecyclerView = root.findViewById<RecyclerView>(R.id.recyclerView_shopping)
            buildRecyclerView(shoopingItemRecyclerView,context)


            //recyclerview test button
            /*
            val testRecyclerView = root.findViewById<Button>(R.id.btn_testRecyclerView)
            testRecyclerView.setOnClickListener {
                testData.add(0,ShoppingItem("Testitem",100, Units.KILOGRAM.getString(context!!)))
                //TODO: Datenweg sauber ziehen
                shoppingViewModel.shoppingItemList.value = testData
                shoopingItemRecyclerView.adapter?.notifyDataSetChanged()
            }

             */

            //swipe refresh layout
            val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh_shopping)
            swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this.context!!,R.color.colorPrimaryDark))
            swipeRefresh.setOnRefreshListener {
                //TODO: Implement action for swipe-refresh
                swipeRefresh.isRefreshing = true
                //testData.clear()
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

            val adapter = ShoppingItemListAdapter(mutableListOf())

            recyclerView.adapter = adapter

            var actionMode : androidx.appcompat.view.ActionMode? = null

            val actionModeHandler = ShoppingActionModeHandler(context)
            actionModeHandler.clickEditButtonHandler = { item ->
                actionMode?.finish()
                Intent(activity, ShoppingItemEditActivity::class.java)
                    .apply {putExtra("item", item.uid) }
                    .also { startActivity(it) }
            }
            actionModeHandler.clickDeleteButtonHandler = { items, views ->
                CoroutineBackgroundTask<Unit>()
                    .executeInBackground {
                        items.forEach {
                            db.itemDao().deleteShoppingItemById(it.uid)
                        }
                    }
                    .onDone {
                        actionMode?.finish()
                        reloadShoppingItems()
                    }
                    .start()
            }

            //onclickactions
            adapter.setOnItemClickListener { position, view ->
                //TODO: Implement OnClick Action for Shopping item click
                Toast.makeText(context,"OnItemClicked",Toast.LENGTH_SHORT).show()


                actionModeHandler.selectItemIfMultisectActive(adapter.data[position], view)

                //end actionmode if there are no selected items anymore
                if(actionModeHandler.countSelected() == 0){
                    actionMode?.finish()
                }
            }


            adapter.setOnItemLongClickListener {position, view ->
                //giving haptic feedback
                context.vibrate()

                val activity = context as AppCompatActivity
                actionMode = activity.startSupportActionMode(actionModeHandler)
                actionMode?.setTitle(R.string.screentitle_main_actionmode_shopping)
                actionModeHandler.selectItemIfMultisectActive(adapter.data[position], view)

                //TODO: Implement OnClick Action for Shopping item longclick
                Toast.makeText(context,"OnItemLongClicked",Toast.LENGTH_SHORT).show()
                true
            }

        }
    }


}