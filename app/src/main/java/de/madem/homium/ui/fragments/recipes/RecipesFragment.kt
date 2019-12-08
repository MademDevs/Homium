package de.madem.homium.ui.fragments.recipes

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_RECIPES
import de.madem.homium.constants.REQUEST_CODE_SHOPPING
import de.madem.homium.databases.AppDatabase
import de.madem.homium.managers.adapters.RecipesListAdapter
import de.madem.homium.ui.activities.recipe.RecipeEditActivity
import de.madem.homium.ui.activities.shoppingitem.ShoppingItemEditActivity
import de.madem.homium.utilities.showToastShort
import de.madem.homium.utilities.switchToActivity
import de.madem.homium.utilities.switchToActivityForResult

class RecipesFragment : Fragment() {

    private lateinit var recipesViewModel: RecipesViewModel
    private lateinit var root: View
    private lateinit var db: AppDatabase

    override fun onAttach(context: Context) {
        super.onAttach(context)
        db = AppDatabase.getInstance()
    }

    override fun onResume() {
        super.onResume()

        //reload shopping items from database
        recipesViewModel.reloadRecipes()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recipesViewModel =
            ViewModelProviders.of(this).get(RecipesViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_recipes, container, false)

        registerRecyclerView()
        registerFloatingActionButton()
        registerSwipeRefresh()

        return root
    }

    private fun registerSwipeRefresh() {
        val swipeRefresh = root.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh_recipes)
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this.context!!,R.color.colorPrimaryDark))
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            //TODO: Swipe Refresh implement something useful^
            recipesViewModel.deleteAllRecipes {
                swipeRefresh.isRefreshing = false
                recipesViewModel.reloadRecipes()
            }
        }
    }

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
            Toast.makeText(context, "Selected ${recipe.name}, ${recipe.uid}", Toast.LENGTH_LONG).show()
            Intent(activity, RecipeEditActivity::class.java)
                .apply {putExtra("recipe", recipe.uid)}
                .also { startActivityForResult(it, REQUEST_CODE_RECIPES) }
        }
    }

}