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
import be.pjrotsaert.mijnhogent.adapters.AssignmentAdapter
import be.pjrotsaert.mijnhogent.api.Chamilo
import be.pjrotsaert.mijnhogent.api.CourseData
import be.pjrotsaert.mijnhogent.viewmodels.AssignmentsViewModel
import kotlinx.android.synthetic.main.assignments_fragment.*

class AssignmentsFragment : Fragment() {

    companion object {
        fun newInstance(courseId: Int = 0, clickable: Boolean = false, abortOnDestroy: Boolean = true): AssignmentsFragment {
            val frag =  AssignmentsFragment()
            val bundle = Bundle()
            bundle.putInt("courseId", courseId)
            bundle.putBoolean("clickable", clickable)
            bundle.putBoolean("abortOnDestroy", abortOnDestroy)
            frag.arguments = bundle
            return frag
        }
    }

    private lateinit var viewModel: AssignmentsViewModel
    private var courseId = 0
    private var clickable = false
    private var abortOnDestroy = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        courseId = arguments?.getInt("courseId") ?: 0
        clickable = arguments?.getBoolean("clickable") ?: false
        abortOnDestroy = arguments?.getBoolean("abortOnDestroy") ?: false
        return inflater.inflate(R.layout.assignments_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AssignmentsViewModel::class.java)
        assignmentRefreshPull.setOnRefreshListener { requestAssignments() }

        assignmentRecycler.layoutManager = LinearLayoutManager(context)
        assignmentRecycler.itemAnimator = DefaultItemAnimator()
        assignmentRecycler.adapter = AssignmentAdapter(viewModel.assignments, clickable)
        assignmentRecycler.adapter?.notifyDataSetChanged()

        if(viewModel.assignments.size <= 0)
            requestAssignments()
    }

    override fun onDestroyView() {
        if(abortOnDestroy)
            Chamilo.getInstance(context!!).abortRequests()
        super.onDestroyView()
    }

    fun requestAssignmentsForCourse(courses: ArrayList<CourseData>){
        Chamilo.getInstance(context!!).getAllAssignments(courses, false) { err ->
            if(view == null)
                return@getAllAssignments

            if(err == null){
                viewModel.assignments.clear()

                for(course in courses){
                    for(assignment in course.assignments){
                        if(assignment.submissionAllowed && !assignment.submitted){
                            assignment.course = course
                            viewModel.assignments.add(assignment)
                        }
                    }
                }

                viewModel.assignments.sortBy { ass -> ass.endTime.time }
                assignmentRecycler.adapter?.notifyDataSetChanged()
            }
            else
                Snackbar.make(view!!, getString(err.getDescriptionId()), Snackbar.LENGTH_LONG).show()
            assignmentRefreshPull.isRefreshing = false
        }
    }

    fun requestAssignments(){
        assignmentRefreshPull.isRefreshing = true
        if(courseId == 0) {
            Chamilo.getInstance(context!!).getCourses { courses, err ->
                if(view == null)
                    return@getCourses

                if (courses != null) {
                    requestAssignmentsForCourse(courses)
                } else if (err != null && view != null) {
                    Snackbar.make(view!!, getString(err.getDescriptionId()), Snackbar.LENGTH_LONG).show()
                    assignmentRefreshPull.isRefreshing = false
                }

            }
        }
        else {
            val course = CourseData()
            val list = ArrayList<CourseData>()
            course.chamilo_course_id = courseId
            list.add(course)
            requestAssignmentsForCourse(list)
        }
    }
}
