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
import be.pjrotsaert.mijnhogent.api.CourseData
import be.pjrotsaert.mijnhogent.viewmodels.AnnouncementsViewModel
import kotlinx.android.synthetic.main.announcements_fragment.*

class AnnouncementsFragment : Fragment() {

    companion object {
        fun newInstance(courseId: Int = 0, clickable: Boolean = false, abortOnDestroy: Boolean = true): AnnouncementsFragment {
            val frag =  AnnouncementsFragment()
            val bundle = Bundle()
            bundle.putInt("courseId", courseId)
            bundle.putBoolean("clickable", clickable)
            bundle.putBoolean("abortOnDestroy", abortOnDestroy)
            frag.arguments = bundle
            return frag
        }
    }

    private lateinit var viewModel: AnnouncementsViewModel
    private var courseId = 0
    private var clickable = false
    private var abortOnDestroy = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        courseId = arguments?.getInt("courseId") ?: 0
        clickable = arguments?.getBoolean("clickable") ?: false
        abortOnDestroy = arguments?.getBoolean("abortOnDestroy") ?: false

        return inflater.inflate(R.layout.announcements_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AnnouncementsViewModel::class.java)
        announcementsRefreshPull.setOnRefreshListener { requestAnnouncements() }

        announcementsRecycler.layoutManager = LinearLayoutManager(context)
        announcementsRecycler.itemAnimator = DefaultItemAnimator()
        announcementsRecycler.adapter = AnnouncementAdapter(viewModel.announcements, clickable)
        announcementsRecycler.adapter?.notifyDataSetChanged()

        if(viewModel.announcements.size <= 0)
            requestAnnouncements()
    }

    override fun onDestroyView() {
        if(abortOnDestroy)
            Chamilo.getInstance(context!!).abortRequests()
        super.onDestroyView()
    }

    fun requestAnnouncementsForCourse(courses: ArrayList<CourseData>){
        Chamilo.getInstance(context!!).getAllAnnouncements(courses) { err ->
            if(view == null)
                return@getAllAnnouncements

            if(err == null){
                viewModel.announcements.clear()

                for(course in courses){
                    for(announcement in course.announcements){
                        announcement.course = course
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

    fun requestAnnouncements(){
        announcementsRefreshPull.isRefreshing = true
        if(courseId == 0) {
            Chamilo.getInstance(context!!).getCourses { courses, err ->
                if(view == null)
                    return@getCourses

                if (courses != null) {
                    requestAnnouncementsForCourse(courses)
                } else if (err != null && view != null) {
                    Snackbar.make(view!!, getString(err.getDescriptionId()), Snackbar.LENGTH_LONG).show()
                    announcementsRefreshPull.isRefreshing = false
                }
            }
        }
        else {
            val course = CourseData()
            val list = ArrayList<CourseData>()
            course.chamilo_course_id = courseId
            list.add(course)
            requestAnnouncementsForCourse(list)
        }
    }
}