package de.madem.homium.ui.fragments.shopping

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
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import de.madem.homium.R
import de.madem.homium.application.HomiumSettings
import de.madem.homium.constants.INTENT_DATA_TRANSFER_EDIT_SHOPPING_ITEM_ID
import de.madem.homium.constants.REQUEST_CODE_SHOPPING
import de.madem.homium.constants.SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_REVERSED
import de.madem.homium.databinding.FragmentShoppingBinding
import de.madem.homium.di.utils.ShoppingToInventoryHandlerAssistedFactory
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.ShoppingItem
import de.madem.homium.ui.activities.shoppingitem.ShoppingItemEditActivity
import de.madem.homium.utilities.android_utilities.SearchViewHandler
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.*
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO Fox bug that Action-Mode Selection is deleted when checking list item using Actionmode
@AndroidEntryPoint
class ShoppingFragment : Fragment(), SearchViewHandler {

    //fields
    private val shoppingViewModel: ShoppingViewModel by viewModels()
    private lateinit var actionModeHandler: ShoppingActionModeHandler


    @Inject
    lateinit var shoppingToInventoryHandlerFactory: ShoppingToInventoryHandlerAssistedFactory
    private val shoppingToInventoryHandler : ShoppingToInventoryHandler by lazy {
        shoppingToInventoryHandlerFactory.create(this.requireContext())
    }

    private var searchViewUtil : Pair<SearchView,MenuItem>? = null

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
                //TODO Move this Filter-functionality to ViewModel
                /*if(recyclerViewAdapter.isReadyForFiltering){
                    recyclerViewAdapter.filter.filter(newText)
                }*/
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
        //getting root layout
        val binding = FragmentShoppingBinding.inflate(inflater, container, false)

        val recyclerViewAdapter = setupRecyclerView(binding)
        setupActionModeHandler()
        setupSwipeRefresh(binding)
        setupFloatingActionButton(binding)

        setupViewModelObservers(binding, recyclerViewAdapter)

        return binding.root
    }

    private fun refreshViewModelData(){
        val sorting = HomiumSettings.shoppingSort

        //TODO Fix Sorting change
        if(sorting == SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_REVERSED){
            //shoppingViewModel.reloadShoppingItems(true)
        }
        else{
            //shoppingViewModel.reloadShoppingItems()
        }

        println("refreshViewModelData")
    }

    //TODO Move this Logic to VM
    private fun updateShoppingItemCheckStatus(shoppingItem: ShoppingItem) {
        shoppingViewModel.setShoppingItemCheckState(shoppingItem, !shoppingItem.checked)
    }

    //private functions
    private fun setupRecyclerView(binding: FragmentShoppingBinding) : ShoppingItemListAdapter {
        val recyclerView = binding.recyclerViewShopping

        //init adapter
        val adapter = ShoppingItemListAdapter(
            onItemClick = { shoppingItem, viewHolder ->
                if (actionModeHandler.isActionModeActive()) {
                    //select item in action mode
                    actionModeHandler.clickItem(shoppingItem, viewHolder)
                } else {
                    //update check status
                    updateShoppingItemCheckStatus(shoppingItem)
                }
            },
            onItemLongClick = { shoppingItem, viewHolder ->
                //giving haptic feedback if allowed
                val vibrationAllowed = HomiumSettings.vibrationEnabled
                if(vibrationAllowed){
                    vibrate()
                }

                //start action mode
                actionModeHandler.startActionMode()
                actionModeHandler.clickItem(shoppingItem, viewHolder)
                true
            }
        )
        recyclerView.adapter = adapter

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
            }


            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean  = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val item = adapter.getItemAtPosition(position)
                if (direction == ItemTouchHelper.LEFT){
                    showDeleteActionDialog(
                        context = requireContext(),
                        onYes = { dialog ->
                            shoppingViewModel
                                .deleteShoppingItem(item)
                                .onCollect(viewLifecycleOwner) { success ->
                                    dialog.dismiss()
                                    if(!success) {
                                        adapter.notifyItemChanged(position)
                                    }
                                }
                        },
                        onNo = { dialog ->
                            dialog.dismiss()
                            adapter.notifyItemChanged(position)
                        }
                    )
                }
                else if (direction == ItemTouchHelper.RIGHT){
                    showShoppingItemEditScreen(item.uid)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return adapter
    }

    private fun setupSwipeRefresh(binding: FragmentShoppingBinding) {
        //swipe refresh layout
        val swipeRefresh = binding.swipeRefreshShopping

        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this.requireContext(),R.color.colorPrimaryDark))
        swipeRefresh.setOnRefreshListener {
            shoppingViewModel.setRefreshing(true)

            GlobalScope.launch(IO) {
                //get checked items
                val checkedItems = shoppingViewModel
                    .getAllCheckedShoppingItems()
                    .takeIf { it.isNotEmpty() }
                    ?: kotlin.run {
                        shoppingViewModel.setRefreshing(false)
                        return@launch
                    }

                GlobalScope.launch(Main) {

                    //handle shopping to inventory handler
                    shoppingToInventoryHandler.handleShoppingItems(checkedItems) {

                        //delete checked items
                        shoppingViewModel.deleteAllCheckedItems()
                    }

                }


            }


        }

        //disable swipe refresh on action mode start and enable on stop
        actionModeHandler.onStartActionMode += { swipeRefresh.isEnabled = false }
        actionModeHandler.onStopActionMode += { swipeRefresh.isEnabled = true }
    }

    private fun setupActionModeHandler() {
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
                    updateShoppingItemCheckStatus(itemHolder.shoppingItem)
                }
            }
        }
    }

    private fun setupFloatingActionButton(binding: FragmentShoppingBinding) {
        //floating action button
        val btnAddShoppingItem = binding.floatingActionButtonAddShoppingItem

        btnAddShoppingItem.setOnClickListener {
            //close search view
            closeSearchView()
            //implementing simple navigation to shopping item edit screen via intent
            switchToActivityForResult(REQUEST_CODE_SHOPPING,ShoppingItemEditActivity::class)
        }
    }

    private fun setupViewModelObservers(binding: FragmentShoppingBinding, recyclerViewAdapter: ShoppingItemListAdapter) {
        shoppingViewModel.shoppingItems.onCollect(viewLifecycleOwner) { shoppingItems ->
            recyclerViewAdapter.submitList(shoppingItems)
            binding.swipeRefreshShopping.isRefreshing = false
        }

        shoppingViewModel.toastNotifications.onCollect(viewLifecycleOwner) { msgResId ->
            showToastShort(msgResId)
        }

        shoppingViewModel.isRefreshing.onCollect(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshShopping.isRefreshing = isRefreshing
        }
    }

    // help functions
    private fun showShoppingItemEditScreen(itemId: Int){
        Intent(activity, ShoppingItemEditActivity::class.java)
            .apply { putExtra(INTENT_DATA_TRANSFER_EDIT_SHOPPING_ITEM_ID, itemId) }
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
            val shoppingList = shoppingViewModel.shoppingItems.value
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