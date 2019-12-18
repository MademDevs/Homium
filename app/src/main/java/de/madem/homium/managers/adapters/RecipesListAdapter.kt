package de.madem.homium.managers.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.models.Ingredients
import de.madem.homium.models.Recipe

class RecipesListAdapter(owner: LifecycleOwner, liveData: MutableLiveData<List<Recipe>>)
    : RecyclerView.Adapter<RecipesListAdapter.RecipesViewHolder>() {

    var data = liveData.value ?: listOf()

    init {
        liveData.observe(owner, Observer { list ->
            data = list
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecipesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_listitem, parent, false)
        return RecipesViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecipesViewHolder, position: Int) {
        val recipe = data[position]

        with(holder) {
            txtName.text = recipe.name
            //set Image!!
            setPic(image, recipe.image)
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    shortClickListener(recipe, this)
                }
            }
        }
    }

    private fun setPic(imgView: ImageView, currentPhotoPath: String) {
        // Get the dimensions of the View
        if(currentPhotoPath.isNotEmpty()) {
            BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->
                imgView.setImageBitmap(bitmap)
            }
        } else {
            imgView.setImageResource(R.mipmap.empty_picture)
        }
    }

    class RecipesViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val txtName = itemView.findViewById<TextView>(R.id.txtView_recipesName)
        val image = itemView.findViewById<ImageView>(R.id.recipes_imgView)
    }

}