package de.madem.homium.ui.fragments.recipes

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.application.HomiumSettings
import de.madem.homium.constants.INTENT_DATA_TRANSFER_EDIT_RECIPE_ID
import de.madem.homium.constants.SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED
import de.madem.homium.application.HomiumApplication
import de.madem.homium.constants.IMPORT_RECIPE_DIALOG_TAG
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.ViewRefresher
import de.madem.homium.managers.adapters.RecipesListAdapter
import de.madem.homium.ui.activities.recipe.RecipeEditActivity
import de.madem.homium.ui.activities.recipe.RecipePresentationActivity
import de.madem.homium.ui.dialogs.RecipeImportDialog
import de.madem.homium.ui.dialogs.RecipeImportDialogListener
import de.madem.homium.utilities.*
import de.madem.homium.utilities.android_utilities.SearchViewHandler
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.getSetting
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.switchToActivity
import de.madem.homium.utilities.extensions.vibrate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class RecipesFragment : Fragment(), SearchViewHandler{

    private lateinit var recipesViewModel: RecipesViewModel
    private lateinit var root: View
    private lateinit var db: AppDatabase
    private lateinit var actionModeHandler: RecipeActionModeHandler

    private lateinit var adapter : RecipesListAdapter
    private var searchViewUtil : Pair<SearchView,MenuItem>? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        db = AppDatabase.getInstance()
    }

    override fun onResume() {
        super.onResume()

        //reload shopping items from database
        ViewRefresher.recipeViewRefresher = {
            recipesViewModel.reloadRecipes()
        }
        recipesViewModel.reloadRecipes()
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
        inflater.inflate(R.menu.recipes_fragment_actionbar_menu,menu)

        //searchview
        val searchItem = menu.findItem(R.id.search_recipes)
        val searchView = searchItem.actionView as? SearchView ?: return
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(adapter.isReadyForFiltering){
                    adapter.filter.filter(newText)
                }

                return false
            }
        })
        searchViewUtil = Pair(searchView,searchItem)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.import_recipes -> {importRecipe(); true}
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recipesViewModel =
            ViewModelProvider(this).get(RecipesViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_recipes, container, false)

        registerRecyclerView()
        registerFloatingActionButton()
        //registerSwipeRefresh()
        registerActionMode()

        return root
    }

    private fun registerActionMode() {
        fun onDeleteButtonClicked(itemHolders: Collection<RecipeActionModeHandler.ItemHolder>) {
            ConfirmDialog.show(context!!, R.string.recipe_actionmode_delete_question) {
                onConfirm = {
                    CoroutineBackgroundTask<Unit>()
                        .executeInBackground {
                            itemHolders.map { it.recipe }.forEach {
                                db.recipeDao().deleteRecipe(it)
                                GlobalScope.launch {
                                   //deleting picture
                                    File(it.image).let {file ->
                                        if(file.exists()){
                                            file.delete()

                                            if(file.exists()){
                                                file.canonicalFile.delete()

                                                if(file.exists()){
                                                    context?.deleteFile(file.name) ?: System.err.println("Failed Deleting picture")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        .onDone {
                            actionModeHandler.finishActionMode()
                            recipesViewModel.reloadRecipes()
                        }
                        .start()
                }
            }
        }

        fun onEditButtonClicked(itemHolder: RecipeActionModeHandler.ItemHolder) {
            Intent(activity, RecipeEditActivity::class.java)
                .apply {
                    putExtra(
                        INTENT_DATA_TRANSFER_EDIT_RECIPE_ID,
                        itemHolder.recipe.uid)
                }
                .also { startActivity(it) }
        }

        //init action mode
        actionModeHandler = RecipeActionModeHandler(context!!)

        //init action mode buttons
        with(actionModeHandler) {
            clickDeleteButtonHandler = ::onDeleteButtonClicked
            clickEditButtonHandler = ::onEditButtonClicked
        }

    }

    /*
    private fun registerSwipeRefresh() {
        val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh_recipes)
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this.context!!,R.color.colorPrimaryDark))
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            //TODO: Swipe Refresh implement something useful^^
        }
    }

     */

    private fun registerFloatingActionButton() {
        val btnAddRecipe = root.findViewById<FloatingActionButton>(
            R.id.floatingActionButton_addRecipe
        )

        btnAddRecipe.setOnClickListener {
            //close search view
            closeSearchView()
            //implementing simple navigation to shopping item edit screen via intent
            //switchToActivityForResult(REQUEST_CODE_SHOPPING, RecipeEditActivity::class)
            switchToActivity(RecipeEditActivity::class)
        }
    }

    private fun registerRecyclerView() {
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView_recipes)
        if(activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.layoutManager = GridLayoutManager(context, 2)
        } else {
            recyclerView.layoutManager = GridLayoutManager(context, 3)
        }
        adapter = RecipesListAdapter(this, recipesViewModel.recipeList)
        recyclerView.adapter = adapter
        adapter.shortClickListener = {recipe, viewHolder ->
            println(recipe.uid)

            if (actionModeHandler.isActionModeActive()) {
                //select item in action mode
                actionModeHandler.clickItem(recipe, viewHolder)
            } else {

                //update check status
                Intent(activity, RecipePresentationActivity::class.java)
                    .apply {
                        putExtra(
                            INTENT_DATA_TRANSFER_EDIT_RECIPE_ID
                            , recipe.uid)
                    }
                    .also { startActivity(it)}
            }


        }
        adapter.longClickListener = {recipe, viewHolder ->
            //giving haptic feedback if allowed
            val vibrationAllowed = HomiumSettings.vibrationEnabled//getSetting(
                //SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED,Boolean::class) ?: true
            if(vibrationAllowed){
                vibrate()
            }

            //start action mode
            actionModeHandler.startActionMode()
            actionModeHandler.clickItem(recipe, viewHolder)
            true
        }
    }

    //import recipe
    private fun importRecipe(){
        activity.notNull {
            RecipeImportDialog().show(it.supportFragmentManager, IMPORT_RECIPE_DIALOG_TAG)
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