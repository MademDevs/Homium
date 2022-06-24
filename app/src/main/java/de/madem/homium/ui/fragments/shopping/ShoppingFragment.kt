package de.madem.homium.ui.fragments.shopping

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.madem.homium.R
import de.madem.homium.application.HomiumSettings
import de.madem.homium.constants.REQUEST_CODE_SHOPPING
import de.madem.homium.constants.SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_REVERSED
import de.madem.homium.di.utils.ShoppingToInventoryHandlerAssistedFactory
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.ShoppingItem
import de.madem.homium.ui.activities.shoppingitem.ShoppingItemEditActivity
import de.madem.homium.utilities.android_utilities.SearchViewHandler
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.showToastShort
import de.madem.homium.utilities.extensions.switchToActivityForResult
import de.madem.homium.utilities.extensions.vibrate
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingFragment : Fragment(), SearchViewHandler {

    //fields
    private val shoppingViewModel: ShoppingViewModel by viewModels()
    private lateinit var root: View
    private lateinit var actionModeHandler: ShoppingActionModeHandler


    @Inject
    lateinit var shoppingToInventoryHandlerFactory: ShoppingToInventoryHandlerAssistedFactory
    private val shoppingToInventoryHandler : ShoppingToInventoryHandler by lazy {
        shoppingToInventoryHandlerFactory.create(this.requireContext())
    }

    private var searchViewUtil : Pair<SearchView,MenuItem>? = null

    //GUI
    private lateinit var recyclerViewAdapter : ShoppingItemListAdapter

    override fun onResume() {
        super.onResume()

        //reload shopping items from database
        refreshViewModelData()
        println("ON RESUME")
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
        inflater.inflate(R.menu.shopping_fragment_actionbar_menu,menu)

        //handling searchview
        val searchItem = menu.findItem(R.id.search_shopping)
        val searchView = searchItem.actionView as? SearchView ?: return
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(recyclerViewAdapter.isReadyForFiltering){
                    recyclerViewAdapter.filter.filter(newText)
                }
                return false
            }

        })

        searchViewUtil = Pair(searchView,searchItem)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.share_shopping -> {shareShoppingList(); true}
            else -> super.onOptionsItemSelected(item)
        }
    }


    //on create view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ViewRefresher.shoppingRefresher = {
            refreshViewModelData()
        }

        //getting root layout
        root = inflater.inflate(R.layout.fragment_shopping, container, false)

        registerRecyclerView()
        registerActionMode()
        registerSwipeRefresh()
        registerFloatingActionButton()

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("ON ACTIVITY RESULT")
        if(requestCode == REQUEST_CODE_SHOPPING){
            if(resultCode == RESULT_OK){
                val dataChanged = data?.getBooleanExtra("shoppingListChanged",false) ?: false
                if(dataChanged){
                    //shoppingViewModel.reloadShoppingItems(context!!)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun refreshViewModelData(){
        val sorting = HomiumSettings.shoppingSort//getSetting(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_SORT,String::class) ?: SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_NORMAL

        if(sorting == SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_REVERSED){
            shoppingViewModel.reloadShoppingItems(true)
        }
        else{
            shoppingViewModel.reloadShoppingItems()
        }

        println("refreshViewModelData")


    }

    private fun updateShoppingItemCheckStatus(shoppingItem: ShoppingItem, viewHolder: ShoppingItemListAdapter.ShoppingItemViewHolder) {
        val newCheckStatus = !shoppingItem.checked

        viewHolder.applyCheck(newCheckStatus) //set check status in view
        shoppingItem.checked = newCheckStatus //set check status in model
        shoppingViewModel.updateShoppingItem(shoppingItem) //update check status in database
    }

    //private functions
    private fun registerRecyclerView(){
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView_shopping)

        //set layout manager
        recyclerView.layoutManager = LinearLayoutManager(context)

        //init adapter
        recyclerViewAdapter = ShoppingItemListAdapter(this, shoppingViewModel.shoppingItemList)
        recyclerView.adapter = recyclerViewAdapter

        //on click listener
        recyclerViewAdapter.shortClickListener = {shoppingItem, viewHolder ->

            if (actionModeHandler.isActionModeActive()) {
                //select item in action mode
                actionModeHandler.clickItem(shoppingItem, viewHolder)
            } else {

                //update check status
                updateShoppingItemCheckStatus(shoppingItem, viewHolder)
            }
        }

        recyclerViewAdapter.longClickListener = {shoppingItem, viewHolder ->
            //giving haptic feedback if allowed
            val vibrationAllowed = HomiumSettings.vibrationEnabled//getSetting(
                //SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED,Boolean::class) ?: true
            if(vibrationAllowed){
                vibrate()
            }

            //start action mode
            actionModeHandler.startActionMode()
            actionModeHandler.clickItem(shoppingItem, viewHolder)
            true
        }

        //item touch helper
        val itemTouchHelper = ItemTouchHelper(
            object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
        ){

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                val swipesToLeft = dX < 0
                val colorRes = if(swipesToLeft) android.R.color.holo_red_dark else R.color.colorAccent
                val iconRes = if(swipesToLeft) R.drawable.ic_delete else R.drawable.ic_edit

                RecyclerViewSwipeDecorator
                    .Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(requireContext(), colorRes))
                    .addActionIcon(iconRes)
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                println("onchild draw")
            }



            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean  = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT){
                    showDeleteActionDialog(
                    context = requireContext(),
                    onYes = { dialog ->
                            CoroutineBackgroundTask<Unit>()
                                .executeInBackground {
                                    val item = recyclerViewAdapter.data[viewHolder.absoluteAdapterPosition]
                                    shoppingViewModel.deleteShoppingItem(item)
                                }
                                .onDone {
                                    refreshViewModelData()
                                    dialog.dismiss()
                                }
                                .start()
                        },
                        onNo = { dialog ->
                            dialog.dismiss()
                            //TODO: change to more efficent implementation
                            refreshViewModelData()
                        }
                    )
                }
                else if (direction == ItemTouchHelper.RIGHT){
                    showShoppingItemEditScreen(recyclerViewAdapter.data[viewHolder.absoluteAdapterPosition].uid)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    private fun registerSwipeRefresh() {
        //swipe refresh layout
        val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh_shopping)

        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this.requireContext(),R.color.colorPrimaryDark))
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true

            GlobalScope.launch(IO) {
                //get checked items
                val checkedItems = shoppingViewModel.getAllCheckedShoppingItems()

                GlobalScope.launch(Main) {

                    //handle shopping to inventory handler
                    shoppingToInventoryHandler.handleShoppingItems(checkedItems) {

                        //delete checked items
                        shoppingViewModel.deleteAllCheckedItems {
                            GlobalScope.launch(Main) {
                                swipeRefresh.isRefreshing = false
                                refreshViewModelData()

                                showToastShort(R.string.notification_remove_bought_shoppingitems)
                            }
                        }
                    }

                }


            }


        }

        //disable swipe refresh on action mode start and enable on stop
        actionModeHandler.onStartActionMode += { swipeRefresh.isEnabled = false }
        actionModeHandler.onStopActionMode += { swipeRefresh.isEnabled = true }
    }

    private fun registerActionMode() {
        actionModeHandler = ShoppingActionModeHandler(requireActivity())

        with(actionModeHandler) {

            clickEditButtonHandler = { itemHolder ->
                finishActionMode()
                showShoppingItemEditScreen(itemHolder.shoppingItem.uid)
            }

            clickDeleteButtonHandler = { itemHolders ->
                showDeleteActionDialog(
                    context = context,
                    onYes = { dialog ->
                        CoroutineBackgroundTask<Unit>()
                            .executeInBackground {
                                val shoppingCart = itemHolders.map { it.shoppingItem }

                                shoppingCart.forEach { shoppingViewModel.deleteShoppingItem(it) }
                            }
                            .onDone {
                                finishActionMode()
                                refreshViewModelData()
                                dialog.dismiss()
                            }
                            .start()
                    },
                    onNo = { dialog ->
                        finishActionMode()
                        dialog.dismiss()
                    }
                )
            }

            clickCheckButtonHandler = { itemHolders ->
                itemHolders.forEachIndexed {index, itemHolder ->
                    //update check status
                    updateShoppingItemCheckStatus(itemHolder.shoppingItem, itemHolder.adapterViewHolder)
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
            //close search view
            closeSearchView()
            //implementing simple navigation to shopping item edit screen via intent
            switchToActivityForResult(REQUEST_CODE_SHOPPING,ShoppingItemEditActivity::class)
        }
    }

    // help functions
    private fun showShoppingItemEditScreen(itemId: Int){
        Intent(activity, ShoppingItemEditActivity::class.java)
            .apply {putExtra("item", itemId) }
            .also { startActivityForResult(it, REQUEST_CODE_SHOPPING) }
    }

    private fun showDeleteActionDialog(
        context: Context,
        onYes: (DialogInterface) -> Unit = {},
        onNo: (DialogInterface) -> Unit = {}
    ){
        AlertDialog.Builder(context)
            .setMessage(R.string.shopping_list_delete_question)
            .setPositiveButton(R.string.answer_yes) { dialog, _ ->
                onYes.invoke(dialog)
            }
            .setNegativeButton(R.string.answer_no) { dialog, _ ->
                onNo.invoke(dialog)
            }.show()
    }

    //functions for searchviewhandler
    override fun closeSearchView() {
        searchViewUtil.notNull {
            it.first.isIconified = true
            it.second.collapseActionView()
        }
    }

    //functions for sharing shopping list
    private fun shareShoppingList(){
        CoroutineBackgroundTask<String>().executeInBackground {
            val shoppingList = shoppingViewModel.shoppingItemList.value ?: listOf()
            return@executeInBackground "${resources.getString(R.string.share_text_shopping)} \n- ${shoppingList.joinToString(
                "\n- ") { it.toString() }}"
        }.onDone {result ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT,result)

            this.activity?.notNull {
                if(shareIntent.resolveActivity(it.packageManager) != null){
                    startActivity(shareIntent)
                }
            }
        }.start()
    }

}