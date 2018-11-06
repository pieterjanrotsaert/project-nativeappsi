package be.pjrotsaert.mijnhogent.activities

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.api.Chamilo
import be.pjrotsaert.mijnhogent.fragments.AnnouncementsFragment
import be.pjrotsaert.mijnhogent.fragments.AssignmentsFragment
import be.pjrotsaert.mijnhogent.fragments.RostersFragment
import be.pjrotsaert.mijnhogent.fragments.CoursesFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_mainactivity.*
import kotlinx.android.synthetic.main.nav_header_mainactivity.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        fun createIntent(ctx: Context): Intent {
            val intent = Intent(ctx, MainActivity::class.java)
            return intent
        }
    }

    private var summaryFragment         = CoursesFragment.newInstance()
    private var dayRosterFragment       = RostersFragment.newInstance()
    private var announcementsFragment   = AnnouncementsFragment.newInstance()
    private var assignmentsFragment     = AssignmentsFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        supportFragmentManager.beginTransaction().replace(R.id.contentFrame, summaryFragment).commit() // Set initial fragment to the 'summary' fragment.
        toolbar.title = getString(R.string.menu_summary)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.nav_drawer, menu)

        // Get the profile picture and put it in the drawer menu.
        Chamilo.getInstance(this).getProfilePic { imgBase64, err ->
            if(imgBase64.isNotEmpty()){
                val data = Base64.decode(imgBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                profilePicView.setImageBitmap(bitmap)
            }
        }

        txtFullName.text = Chamilo.getInstance(this).getUserFullName()
        txtEmailAddress.text = Chamilo.getInstance(this).getUserEmailAddress()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_summary -> {
                supportFragmentManager.beginTransaction().replace(R.id.contentFrame, summaryFragment).commit()
                toolbar.title = getString(R.string.menu_summary)
            }
            R.id.nav_rosters -> {
                supportFragmentManager.beginTransaction().replace(R.id.contentFrame, dayRosterFragment).commit()
                toolbar.title = getString(R.string.menu_rosters)
            }
            R.id.nav_assignments -> {
                supportFragmentManager.beginTransaction().replace(R.id.contentFrame, assignmentsFragment).commit()
                toolbar.title = getString(R.string.menu_assignments)
            }
            R.id.nav_announcements -> {
                supportFragmentManager.beginTransaction().replace(R.id.contentFrame, announcementsFragment).commit()
                toolbar.title = getString(R.string.menu_announcements)
            }
            R.id.nav_logout -> {

                Chamilo.getInstance(this).logout {
                    _ ->
                    val prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
                    prefs.edit().remove("session").apply()
                    startActivity(LoginActivity.createIntent(this))
                    finish()
                }
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
