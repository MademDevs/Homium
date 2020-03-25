package de.madem.homium.ui.fragments.inventory

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.application.HomiumSettings
import de.madem.homium.constants.REQUEST_CODE_INVENTORY
import de.madem.homium.constants.SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.managers.adapters.InventoryItemListAdapter
import de.madem.homium.ui.activities.inventoryedit.InventoryItemEditActivity
import de.madem.homium.utilities.android_utilities.SearchViewHandler
import de.madem.homium.utilities.extensions.*

class InventoryFragment : Fragment(), SearchViewHandler {

    //private lateinit var binding: ResultPro
    private lateinit var actionModeHandler: InventoryActionModeHandler
    private lateinit var inventoryViewModel: InventoryViewModel
    private lateinit var root: View

    private lateinit var inventoryAdapter : InventoryItemListAdapter
    private var searchViewUtil : Pair<SearchView,MenuItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inventoryViewModel = ViewModelProviders.of(this).get(InventoryViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_inventory, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createActionMode()
        registerRecyclerView()
        registerFloatingActionButton()
    }

    override fun onPause() {
        super.onPause()

        if (::actionModeHandler.isInitialized) {
            actionModeHandler.finishActionMode()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.inventory_fragment_actionbar_menu,menu)

        //handling searchview
        val searchItem = menu.findItem(R.id.search_inventory)
        val searchView = searchItem.actionView as? SearchView ?: return
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(inventoryAdapter.isReadyForFiltering){
                    inventoryAdapter.filter.filter(newText)
                }
                return false
            }

        })
        searchViewUtil = Pair(searchView,searchItem)
    }

    override fun onResume() {
        super.onResume()

        inventoryViewModel.reloadInventoryItems()
        ViewRefresher.inventoryRefresher = inventoryViewModel::reloadInventoryItems
    }

    private fun createActionMode() {
        actionModeHandler = InventoryActionModeHandler(context!!)

        with(actionModeHandler) {
            clickDeleteButtonHandler = {

                AlertDialog.Builder(context)
                    .setMessage(R.string.inventory_edit_button_delete_question)
                    .setPositiveButton(R.string.answer_yes) { dialog, _ ->

                        val inventoryItems = it.map { it.inventoryItem }

                        inventoryViewModel.deleteInventoryItems(inventoryItems) {
                            showToastShort(R.string.notification_delete_inventoryitem_sucess)
                            dialog.dismiss()
                            finishActionMode()
                            inventoryViewModel.reloadInventoryItems()
                        }
                    }
                    .setNegativeButton(R.string.answer_no) { dialog, _ ->
                        dialog.dismiss()
                    }.show()

            }
        }

    }

    private fun registerRecyclerView() {
        val recyclerView = root.findViewById<RecyclerView>(R.id.rv_inventory)
        recyclerView.layoutManager = LinearLayoutManager(context)

        inventoryAdapter = InventoryItemListAdapter(this, inventoryViewModel.inventoryItems)

        //on click listener
        inventoryAdapter.shortClickListener = { item, holder ->

            if (actionModeHandler.isActionModeActive()) {

                //select item in action mode
                actionModeHandler.clickItem(item, holder)

            } else {

                //edit inventory item
                Intent(activity, InventoryItemEditActivity::class.java)
                    .apply { putExtra("item", item.uid) }
                    .also { startActivityForResult(it, REQUEST_CODE_INVENTORY) }

            }

        }

        //on long click
        inventoryAdapter.longClickListener = { item, viewHolder ->
            //giving haptic feedback if allowed
            val vibrationAllowed = HomiumSettings.vibrationEnabled//getSetting(
                //SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED,Boolean::class) ?: true
            if(vibrationAllowed){
                vibrate()
            }

            //start action mode
            actionModeHandler.startActionMode()
            actionModeHandler.clickItem(item, viewHolder)
            true
        }


        recyclerView.adapter = inventoryAdapter
    }


    private fun registerFloatingActionButton() {
        //floating action button
        val btnAddShoppingItem = root.findViewById<FloatingActionButton>(R.id.fab_add_inventory)

        btnAddShoppingItem.setOnClickListener {
            //close search view
            closeSearchView()
            //implementing simple navigation to inventory item edit screen via intent
            switchToActivityForResult(0, InventoryItemEditActivity::class)
        }
    }

    //searchViewHandler
    override fun closeSearchView() {
        searchViewUtil.notNull {
            it.first.isIconified = true
            it.second.collapseActionView()
        }
    }
}