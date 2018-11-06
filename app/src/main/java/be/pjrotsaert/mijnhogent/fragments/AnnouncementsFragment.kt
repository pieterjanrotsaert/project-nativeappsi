package be.pjrotsaert.mijnhogent.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.adapters.AnnouncementAdapter
import be.pjrotsaert.mijnhogent.api.Chamilo
import be.pjrotsaert.mijnhogent.viewmodels.AnnouncementsViewModel
import kotlinx.android.synthetic.main.announcements_fragment.*

class AnnouncementsFragment : Fragment() {

    companion object {
        fun newInstance(): AnnouncementsFragment {
            return AnnouncementsFragment()
        }
    }

    private lateinit var viewModel: AnnouncementsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.announcements_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AnnouncementsViewModel::class.java)
        announcementsRefreshPull.setOnRefreshListener { requestAnnouncements() }

        announcementsRecycler.layoutManager = LinearLayoutManager(context)
        announcementsRecycler.itemAnimator = DefaultItemAnimator()
        announcementsRecycler.adapter = AnnouncementAdapter(viewModel.announcements)
        announcementsRecycler.adapter?.notifyDataSetChanged()

        if(viewModel.announcements.size <= 0)
            requestAnnouncements()
    }

    fun requestAnnouncements(){
        announcementsRefreshPull.isRefreshing = true
        Chamilo.getInstance(context!!).getCourses { courses, err ->
            if(courses != null){
                Chamilo.getInstance(context!!).getAllAnnouncements(courses) { err ->
                    if(err == null){
                        viewModel.announcements.clear()

                        for(course in courses){
                            for(announcement in course.announcements){
                                viewModel.announcements.add(announcement)
                            }
                        }

                        viewModel.announcements.sortBy { ann -> -ann.publicationDate.time }
                        announcementsRecycler.adapter?.notifyDataSetChanged()
                    }
                    else
                        Snackbar.make(view!!, getString(err.getDescriptionId()), Snackbar.LENGTH_LONG).show()
                    announcementsRefreshPull.isRefreshing = false
                }
            }
            else if(err != null && view != null){
                Snackbar.make(view!!, getString(err.getDescriptionId()), Snackbar.LENGTH_LONG).show()
                announcementsRefreshPull.isRefreshing = false
            }
        }
    }
}