package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import de.madem.homium.R
import de.madem.homium.utilities.FakePageFragment

class RecipePresentationActivity2 : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    val PERCENTAGE_TO_ANIMATE_AVATAR = 20
    var mIsAvatarShown = true

    var mProfileImage: ImageView? = null
    var mMaxScrollSize = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_presentationx)

        val tabLayout =
            findViewById<View>(R.id.materialup_tabs) as TabLayout
        val viewPager =
            findViewById<View>(R.id.materialup_viewpager) as ViewPager
        val appbarLayout =
            findViewById<View>(R.id.materialup_appbar) as AppBarLayout
        mProfileImage =
            findViewById<View>(R.id.materialup_profile_image) as ImageView
        val toolbar =
            findViewById<View>(R.id.materialup_toolbar) as Toolbar
        toolbar.setNavigationOnClickListener { onBackPressed() }
        appbarLayout.addOnOffsetChangedListener(this)
        mMaxScrollSize = appbarLayout.totalScrollRange
        viewPager.adapter = TabsAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
    }


    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (mMaxScrollSize == 0) mMaxScrollSize = appBarLayout!!.totalScrollRange
        val percentage = Math.abs(verticalOffset) * 100 / mMaxScrollSize
        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false
            mProfileImage!!.animate()
                .scaleY(0f).scaleX(0f)
                .setDuration(200)
                .start()
        }
        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true
            mProfileImage!!.animate()
                .scaleY(1f).scaleX(1f)
                .start()
        }
    }

    class TabsAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        override fun getCount(): Int {
            return Companion.TAB_COUNT
        }

        override fun getItem(i: Int): Fragment {
            return FakePageFragment.newInstance()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return "Tab $position"
        }

        companion object {
            private const val TAB_COUNT = 2
        }
    }


}