package de.madem.homium.ui.fragments.recipes

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.adapters.RecipesListAdapter
import de.madem.homium.ui.activities.recipe.RecipeEditActivity
import de.madem.homium.ui.activities.recipe.RecipePresentationActivity
import de.madem.homium.utilities.*
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.getSetting
import de.madem.homium.utilities.extensions.switchToActivity
import de.madem.homium.utilities.extensions.vibrate

class RecipesFragment : Fragment() {

    private lateinit var recipesViewModel: RecipesViewModel
    private lateinit var root: View
    private lateinit var db: AppDatabase
    private lateinit var actionModeHandler: RecipeActionModeHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        db = AppDatabase.getInstance()
    }

    override fun onResume() {
        super.onResume()

        //reload shopping items from database
        recipesViewModel.reloadRecipes()
    }

    override fun onPause() {
        super.onPause()

        if (::actionModeHandler.isInitialized) {
            actionModeHandler.finishActionMode()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recipesViewModel =
            ViewModelProviders.of(this).get(RecipesViewModel::class.java)
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
                    putExtra(resources.getString(R.string.data_transfer_intent_edit_recipe_id),
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
        val adapter = RecipesListAdapter(this, recipesViewModel.recipeList)
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
                        putExtra(resources.getString(R.string.data_transfer_intent_edit_recipe_id)
                            , recipe.uid)
                    }
                    .also { startActivity(it)}
            }


        }
        adapter.longClickListener = {recipe, viewHolder ->
            //giving haptic feedback if allowed
            val vibrationAllowed = getSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),Boolean::class) ?: true
            if(vibrationAllowed){
                vibrate()
            }

            //start action mode
            actionModeHandler.startActionMode()
            actionModeHandler.clickItem(recipe, viewHolder)
            true
        }
    }

}