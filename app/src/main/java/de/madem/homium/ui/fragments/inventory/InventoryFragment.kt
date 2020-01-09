package de.madem.homium.ui.fragments.inventory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_INVENTORY
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.managers.adapters.InventoryItemListAdapter
import de.madem.homium.ui.activities.inventoryedit.InventoryItemEditActivity
import de.madem.homium.utilities.getSetting
import de.madem.homium.utilities.showToastShort
import de.madem.homium.utilities.switchToActivityForResult
import de.madem.homium.utilities.vibrate

class InventoryFragment : Fragment() {

    //private lateinit var binding: ResultPro
    private lateinit var actionModeHandler: InventoryActionModeHandler
    private lateinit var inventoryViewModel: InventoryViewModel
    private lateinit var root: View

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

        val inventoryAdapter = InventoryItemListAdapter(this, inventoryViewModel.inventoryItems)

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
            val vibrationAllowed = getSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),Boolean::class) ?: true
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
            //implementing simple navigation to inventory item edit screen via intent
            switchToActivityForResult(0, InventoryItemEditActivity::class)
        }
    }


}