package de.madem.homium.managers.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.pictures.BitmapUtil

class RecipesListAdapter(owner: LifecycleOwner, liveData: MutableLiveData<List<Recipe>>, private val onFetchIngredientsByRecipeId: (Int) -> List<RecipeIngredient>)
    : RecyclerView.Adapter<RecipesListAdapter.RecipesViewHolder>(), Filterable {

    var data = liveData.value?.toMutableList() ?: mutableListOf()
    private var dataBackup = data.toMutableList()
    var isReadyForFiltering = false
    private set(value) {
        field = value
    }

    init {
        liveData.observe(owner, Observer { list ->
            isReadyForFiltering = false
            data = list.toMutableList()
            dataBackup = data.toMutableList()
            notifyDataSetChanged()
        })
    }

    //fields
    var shortClickListener : (item: Recipe, holder: RecipesViewHolder) -> Unit = { _, _ ->
        //nothing to do here :D
    }

    var longClickListener : (item: Recipe, holder: RecipesViewHolder) -> Boolean = { _, _ ->
        //nothing to do here
        false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_listitem, parent, false)
        return RecipesViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecipesViewHolder, position: Int) {
        val recipe = data[position]

        with(holder) {
            txtName.text = recipe.name
            //image.setPictureFromPath(recipe.image)
            //set Image asynchronously!!
            val path = recipe.image
            if(path.isNotEmpty()) {
                val cachedBitmap : Bitmap? = BitmapUtil.imageCache.get(path);

                if(cachedBitmap == null){
                    CoroutineBackgroundTask<Bitmap>().executeInBackground {
                        BitmapUtil.loadBitmapFromPath(path)
                    }.onDone {bitmap ->
                        image.setImageBitmap(bitmap)
                        BitmapUtil.imageCache.put(path,bitmap)
                    }.start()
                }
                else{
                    image.setImageBitmap(cachedBitmap);
                }


            }
            else {
                image.setImageResource(R.mipmap.empty_picture)
            }
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    shortClickListener(recipe, this)
                }
            }
            itemView.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    longClickListener(recipe, this)
                } else {
                    false
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecipesViewHolder) {
        super.onViewAttachedToWindow(holder)
        isReadyForFiltering = true
    }


    class RecipesViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val txtName = itemView.findViewById<TextView>(R.id.txtView_recipesName)
        val image = itemView.findViewById<ImageView>(R.id.recipes_imgView)

    }

    //Filter
    override fun getFilter(): Filter {
        return object: Filter(){
            override fun performFiltering(searchText: CharSequence?): FilterResults {
                println("perform filtering recipes")
                val resultList = mutableListOf<Recipe>()

                if(searchText == null || searchText.isEmpty() || searchText.isBlank()){
                    resultList.addAll(dataBackup)
                }
                else{

                    val filterArg = searchText.toString().toLowerCase().trim()
                    dataBackup.forEach { rec ->
                        val name = rec.name.toLowerCase()
                        val nameMatches = name.contains(filterArg)


                        if(nameMatches){
                            resultList.add(rec)
                        }
                        else{
                            val ingredients = onFetchIngredientsByRecipeId(rec.uid)

                            val ingredientSucess = searchInIngredients(filterArg, ingredients)
                            if(ingredientSucess){
                                resultList.add(rec)
                            }
                        }
                    }
                }

                return FilterResults().apply {
                    values = resultList
                }
            }

            override fun publishResults(searchText: CharSequence?, results: FilterResults?) {
                data.clear()
                val resList = results?.values as? List<Recipe> ?: listOf()
                data.addAll(resList)
                println("done filtering recipes")
                notifyDataSetChanged()
            }

            //help functions
            private fun searchInIngredients(filterArg : String, ingredients: List<RecipeIngredient>) : Boolean{
                ingredients.map { it.name.toLowerCase() }.forEach {
                    if(it.contains(filterArg)){
                        return true
                    }
                }

                return false
            }
        }
    }
}