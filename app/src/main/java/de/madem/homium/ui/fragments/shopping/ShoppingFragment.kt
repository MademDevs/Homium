package de.madem.homium.ui.fragments.shopping

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.ui.activities.shoppingitem.ShoppingItemEditActivity
import de.madem.homium.utilities.switchToActivity

class ShoppingFragment : Fragment() {

    //fields
    private lateinit var shoppingViewModel: ShoppingViewModel

    private val testData = mutableListOf<ShoppingItem>()

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
                testData.add(0,ShoppingItem("Testitem ${System.currentTimeMillis()}",100, Units.KILOGRAM.getString(context!!)))
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
            val adapter = ShoppingItemListAdapter(testData)
            recyclerView.adapter = adapter

            val actionModeHandler = ShoppingActionModeHandler(context)
            var actionMode : androidx.appcompat.view.ActionMode? = null

            //onclickactions
            adapter.setOnItemClickListener { position, view ->
                //TODO: Implement OnClick Action for Shopping item click
                Toast.makeText(context,"OnItemClicked",Toast.LENGTH_SHORT).show()

                actionModeHandler.selectItemIfMultisectActive(testData[position], view)

                //end actionmode if there are no selected items anymore
                if(actionModeHandler.countSelected() == 0){
                    actionMode?.finish()
                }

            }

            adapter.setOnItemLongClickListener {position, view ->
                //giving haptic feedback
                val vib = context.getSystemService(VIBRATOR_SERVICE) as? Vibrator
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O){
                    vib?.vibrate(30)
                }
                else{
                    vib?.vibrate(VibrationEffect.createOneShot(30,10))
                }

                val activity = context as AppCompatActivity
                actionMode = activity.startSupportActionMode(actionModeHandler)
                actionMode?.setTitle(R.string.screentitle_main_actionmode_shopping)
                actionModeHandler.selectItemIfMultisectActive(testData[position], view)

                //TODO: Implement OnClick Action for Shopping item longclick
                Toast.makeText(context,"OnItemLongClicked",Toast.LENGTH_SHORT).show()
                return@setOnItemLongClickListener true
            }



        }

    }

}