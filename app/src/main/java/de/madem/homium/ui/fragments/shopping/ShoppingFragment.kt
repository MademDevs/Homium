package de.madem.homium.ui.fragments.shopping

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
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

        //reload shopping items from database
        shoppingViewModel.reloadShoppingItems(context!!)
    }

    //on create
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //getting viewmodel
        shoppingViewModel = ViewModelProviders.of(this).get(ShoppingViewModel::class.java)


        //getting root layout
        val root = inflater.inflate(R.layout.fragment_shopping, container, false)


        if (this.context != null){
            //recyclerview
            shoopingItemRecyclerView = root.findViewById(R.id.recyclerView_shopping)
            buildRecyclerView(shoopingItemRecyclerView,context)

            //swipe refresh layout
            val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh_shopping)
            swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this.context!!,R.color.colorPrimaryDark))
            swipeRefresh.setOnRefreshListener {
                swipeRefresh.isRefreshing = true

                shoppingViewModel.deleteAllCheckedItems(context!!) {
                    swipeRefresh.isRefreshing = false
                    shoppingViewModel.reloadShoppingItems(context!!)

                    Toast.makeText(context!!,resources.getString(R.string.notification_remove_all_bought_shoppingitems),Toast.LENGTH_SHORT).show()
                }
            }
        }

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

            val adapter = ShoppingItemListAdapter(
                this, shoppingViewModel.shoppingItemList
            )

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
                AlertDialog.Builder(context)
                    .setMessage(R.string.shopping_list_delete_question)
                    .setPositiveButton(android.R.string.yes) { dialog, _ ->
                        CoroutineBackgroundTask<Unit>()
                            .executeInBackground {
                                items.forEach {
                                    db.itemDao().deleteShoppingItemById(it.uid)
                                }
                            }
                            .onDone {
                                actionMode?.finish()
                                shoppingViewModel.reloadShoppingItems(context)
                                dialog.dismiss()
                            }
                            .start()
                    }
                    .setNegativeButton(android.R.string.no) { dialog, _ ->
                        actionMode?.finish()
                        dialog.dismiss()
                    }.show()
            }

            //on click listener
            adapter.shortClickListener = {shoppingItem, viewHolder ->

                if (actionModeHandler.multiSelectActive) {

                    //select item in action mode
                    actionModeHandler.selectItemIfMultiSelectActive(
                        shoppingItem, viewHolder.itemView
                    )

                    //end action mode if there are no selected items anymore
                    if(actionModeHandler.countSelected() == 0){
                        actionMode?.finish()
                    }

                } else {
                    val newCheckStatus = !shoppingItem.checked

                    viewHolder.applyCheck(newCheckStatus) //set check status in view
                    shoppingItem.checked = newCheckStatus //set check status in model
                    shoppingViewModel.updateShoppingItem(context, shoppingItem) //update check status in database
                }



            }

            adapter.longClickListener = {shoppingItem, viewHolder ->
                //giving haptic feedback
                context.vibrate()

                val activity = context as AppCompatActivity
                actionMode = activity.startSupportActionMode(actionModeHandler)
                actionMode?.setTitle(R.string.screentitle_main_actionmode_shopping)
                actionModeHandler.selectItemIfMultiSelectActive(shoppingItem, viewHolder.itemView)

                //TODO: Implement OnClick Action for Shopping item longclick
                Toast.makeText(context,"OnItemLongClicked",Toast.LENGTH_SHORT).show()
                true
            }

        }
    }


}