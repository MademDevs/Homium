package de.madem.homium.ui.fragments.recipes

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.managers.adapters.RecipesListAdapter

class RecipesFragment : Fragment() {

    private lateinit var recipesViewModel: RecipesViewModel
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recipesViewModel =
            ViewModelProviders.of(this).get(RecipesViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_recipes, container, false)

        registerRecyclerView()

        return root
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
            Toast.makeText(context, "Selected ${recipe.name}", Toast.LENGTH_LONG).show()
        }
    }

}