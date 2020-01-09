package de.madem.homium.ui.fragments.recipes

import android.content.Context
import android.view.MenuItem
import de.madem.homium.R
import de.madem.homium.managers.adapters.RecipesListAdapter
import de.madem.homium.managers.adapters.ShoppingItemListAdapter
import de.madem.homium.models.Recipe
import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.actionmode.ActionModeHandler
import de.madem.homium.utilities.actionmode.ActionModeItemHolder

class RecipeActionModeHandler(context: Context) : ActionModeHandler<RecipeActionModeHandler.ItemHolder>(context) {

    //protected properties
    override val actionModeSettings: ActionModeSettings
        get() = ActionModeSettings(
            titleResource = R.string.screentitle_main_actionmode_shopping,
            menuResource = R.menu.recipe_fragment_actionmode
        )


    //public api properties
    var clickDeleteButtonHandler: (Collection<ItemHolder>) -> Unit = { _ -> }


    //protected functions
    override fun onMenuItemClicked(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.recipe_item_am_btn_delete -> {
                clickDeleteButtonHandler.invoke(selectedItems)
                return true
            }
        }
        return false
    }

    //api methods
    fun clickItem(
        recipe: Recipe,
        adapterViewHolder: RecipesListAdapter.RecipesViewHolder
    ) {
        val itemHolder = ItemHolder.of(recipe, adapterViewHolder)
        clickItem(itemHolder)
    }


    //item holder class. Init only via ItemHolder.of()
    class ItemHolder private constructor(
        val recipe: Recipe,
        adapterViewHolder: RecipesListAdapter.RecipesViewHolder
    ) : ActionModeItemHolder() {

        //super properties
        override val itemView = adapterViewHolder.itemView

        //super implementations
        override fun equalsImpl(other: ActionModeItemHolder): Boolean {
            return if (other is ItemHolder) {
                recipe.uid == other.recipe.uid
            } else false
        }
        override fun hashCodeProvider(): Int = recipe.uid

        //static method for initialization
        companion object {
            fun of(
                recipe: Recipe,
                adapterViewHolder: RecipesListAdapter.RecipesViewHolder
            ) = ItemHolder(recipe, adapterViewHolder)
        }
    }
}