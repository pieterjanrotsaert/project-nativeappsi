package be.pjrotsaert.mijnhogent.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.api.CourseData
import be.pjrotsaert.mijnhogent.fragments.AnnouncementsFragment
import be.pjrotsaert.mijnhogent.fragments.AssignmentsFragment
import be.pjrotsaert.mijnhogent.fragments.CourseInfoFragment
import kotlinx.android.synthetic.main.activity_course.*
import kotlinx.serialization.json.JSON


class CourseActivity : AppCompatActivity() {

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            if(position == 0)
                return CourseInfoFragment.newInstance(JSON.stringify(CourseData.serializer(), courseData))
            else if(position == 1)
                return AnnouncementsFragment.newInstance(courseData.chamilo_course_id, false, false)
            else //if(position == 2)
                return AssignmentsFragment.newInstance(courseData.chamilo_course_id, false, false)
        }

        override fun getCount(): Int {
            return 3 // Show 3 total pages.
        }
    }

    companion object {
        val EXTRA_COURSE_DATA = "be.pjrotsaert.mijnhogent.extras.coursedata"

        fun newIntent(context: Context, course: CourseData): Intent {
            val intent = Intent(context, CourseActivity::class.java)
            intent.putExtra(EXTRA_COURSE_DATA, JSON.stringify(CourseData.serializer(), course))
            return intent
        }
    }

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var courseData = CourseData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        courseData = JSON.parse(CourseData.serializer(), intent.getStringExtra(EXTRA_COURSE_DATA))
        toolbar.title = courseData.title

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        courseTabPageContainer.adapter = mSectionsPagerAdapter

        courseTabPageContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(courseTabPageContainer))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true // No menu needed for this activity.
    }
}
