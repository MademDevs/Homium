package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.RecipeDao
import de.madem.homium.models.Recipe
import de.madem.homium.utilities.CoroutineBackgroundTask


class PresentationMode: Fragment() {

    private lateinit var recipe: Recipe
    private lateinit var dao: RecipeDao
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: PresentationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dao = AppDatabase.getInstance().recipeDao()
        val recipeId = activity?.intent?.extras?.getInt("recipe")
        println(recipeId)
        if(recipeId != null) {
            CoroutineBackgroundTask<Recipe>()
                .executeInBackground { dao.getRecipeById(recipeId) }
                .onDone { recipe = it }
                .start()
        }
        return inflater.inflate(R.layout.activity_presentation_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PresentationAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = adapter
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            tab.text = "Schritt ${(position + 1)}"
        }.attach()
    }

    class PresentationAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 10
        override fun createFragment(position: Int): Fragment {
            val fragment = PresentationFragment()
            fragment.arguments = Bundle().apply {
                putInt("int", position+1)
            }
            return fragment
        }
    }

    class PresentationFragment: Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_presentation, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            arguments?.takeIf { it.containsKey("int") }?.apply {
                val textView = view.findViewById<TextView>(R.id.presentation_txtView)
                textView.text = (getInt("int").toString() + "\n"+ "\n"+ "\n"+ "\n"+ "\n" + "a")
            }
        }
    }

}