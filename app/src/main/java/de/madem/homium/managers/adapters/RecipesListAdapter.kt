package de.madem.homium.managers.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.madem.homium.R
import de.madem.homium.models.Recipe
import de.madem.homium.utilities.pictures.BitmapUtil
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask

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


    class RecipesViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val txtName = itemView.findViewById<TextView>(R.id.txtView_recipesName)
        val image = itemView.findViewById<ImageView>(R.id.recipes_imgView)

    }

}