package de.madem.homium.ui.fragments.shopping

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.ItemDao
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.ShoppingItem
import de.madem.homium.ui.activities.shoppingitem.ShoppingItemEditActivity
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.showToastShort
import de.madem.homium.utilities.switchToActivity
import de.madem.homium.utilities.vibrate

class ShoppingFragment : Fragment() {

    //fields
    private lateinit var shoppingViewModel: ShoppingViewModel
    private lateinit var root: View
    private lateinit var actionModeHandler: ShoppingActionModeHandler
    private lateinit var databaseDao: ItemDao

    override fun onAttach(context: Context) {
        super.onAttach(context)
        databaseDao = AppDatabase.getInstance(context).itemDao()
    }

    override fun onResume() {
        super.onResume()

        //reload shopping items from database
        shoppingViewModel.reloadShoppingItems(context!!)
    }

    //on create
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //getting view model
        shoppingViewModel = ViewModelProviders.of(this).get(ShoppingViewModel::class.java)

        //getting root layout
        root = inflater.inflate(R.layout.fragment_shopping, container, false)

        registerRecyclerView()
        registerActionMode()
        registerSwipeRefresh()
        registerFloatingActionButton()

        return root
    }

    private fun updateShoppingItemCheckStatus(shoppingItem: ShoppingItem, viewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder) {
        val newCheckStatus = !shoppingItem.checked

        viewHolder.applyCheck(newCheckStatus) //set check status in view
        shoppingItem.checked = newCheckStatus //set check status in model
        shoppingViewModel.updateShoppingItem(context!!, shoppingItem) //update check status in database
    }

    //private functions
    private fun registerRecyclerView(){
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView_shopping)

        //set layout manager
        recyclerView.layoutManager = LinearLayoutManager(context)

        //init adapter
        val adapter = ShoppingItemListAdapter(this, shoppingViewModel.shoppingItemList)
        recyclerView.adapter = adapter

        //on click listener
        adapter.shortClickListener = {shoppingItem, viewHolder ->

            if (actionModeHandler.isActionModeActive()) {

                //select item in action mode
                actionModeHandler.clickItem(shoppingItem, viewHolder)

            } else {

                //update check status
                updateShoppingItemCheckStatus(shoppingItem, viewHolder)
            }
        }

        adapter.longClickListener = {shoppingItem, viewHolder ->
            //giving haptic feedback
            context?.vibrate()

            //start action mode
            actionModeHandler.startActionMode()
            actionModeHandler.clickItem(shoppingItem, viewHolder)

            //show tmp toast
            context?.showToastShort(R.string.dummy, "OnItemLongClicked")
            true
        }

    }

    private fun registerSwipeRefresh() {
        //swipe refresh layout
        val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh_shopping)
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this.context!!,R.color.colorPrimaryDark))
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true

            shoppingViewModel.deleteAllCheckedItems(context!!) {
                swipeRefresh.isRefreshing = false
                shoppingViewModel.reloadShoppingItems(context!!)

                context?.showToastShort(R.string.notification_remove_all_bought_shoppingitems)
            }
        }
    }

    private fun registerActionMode() {
        actionModeHandler = ShoppingActionModeHandler(context!!)

        with(actionModeHandler) {

            clickEditButtonHandler = { item ->
                finishActionMode()
                Intent(activity, ShoppingItemEditActivity::class.java)
                    .apply {putExtra("item", item.uid) }
                    .also { startActivity(it) }
            }

            clickDeleteButtonHandler = { items, _ ->
                AlertDialog.Builder(context)
                    .setMessage(R.string.shopping_list_delete_question)
                    .setPositiveButton(android.R.string.yes) { dialog, _ ->
                        CoroutineBackgroundTask<Unit>()
                            .executeInBackground {
                                items.forEach {
                                    databaseDao.deleteShoppingItemById(it.uid)
                                }
                            }
                            .onDone {
                                finishActionMode()
                                shoppingViewModel.reloadShoppingItems(context)
                                dialog.dismiss()
                            }
                            .start()
                    }
                    .setNegativeButton(android.R.string.no) { dialog, _ ->
                        finishActionMode()
                        dialog.dismiss()
                    }.show()
            }

            clickCheckButtonHandler = { items, viewHolders ->
                items.forEachIndexed {index, shoppingItem ->
                    //update check status
                    updateShoppingItemCheckStatus(shoppingItem, viewHolders[index])
                }
            }
        }
    }

    private fun registerFloatingActionButton() {
        //floating action button
        val btnAddShoppingItem = root.findViewById<FloatingActionButton>(
            R.id.floatingActionButton_addShoppingItem
        )

        btnAddShoppingItem.setOnClickListener {
            //implementing simple navigation to shopping item edit screen via intent
            switchToActivity(ShoppingItemEditActivity::class)
        }
    }

}