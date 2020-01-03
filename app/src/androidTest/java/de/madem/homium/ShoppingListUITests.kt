package de.madem.homium

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.madem.homium.ui.activities.main.MainActivity
import de.madem.homium.utils.RecyclerViewUtils.atPosition
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ShoppingListUITests {

    companion object {
        const val TEST_ITEM_NAME = "Chips"
    }

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java)

    @Test
    fun add_button_click_switch_activity() {
        //click add button
        onView(withId(R.id.floatingActionButton_addShoppingItem))
            .perform(click())

        //check if text view is displayed on add screen
        onView(withId(R.id.shopping_item_edit_txtView_name))
            .check(matches(isDisplayed()))
    }

    @Test
    fun add_item_visible_in_recycler_view() {
        //click add button
        onView(withId(R.id.floatingActionButton_addShoppingItem))
            .perform(click())

        //check if text view is displayed on add screen
        onView(withId(R.id.shopping_item_edit_txtView_name))
            .check(matches(isDisplayed()))

        //type text e.g. "Chips" in text field
        onView(withId(R.id.shopping_item_edit_autoCmplTxt_name))
            .perform(typeText(TEST_ITEM_NAME))

        //click confirm button in actionbar
        onView(withId(R.id.shopping_item_edit_actionbar_confirm))
            .perform(click())

        //check if entry in recycler view is present
        onView(withId(R.id.recyclerView_shopping))
            .check(matches(atPosition(0, isDisplayed())))
    }



}