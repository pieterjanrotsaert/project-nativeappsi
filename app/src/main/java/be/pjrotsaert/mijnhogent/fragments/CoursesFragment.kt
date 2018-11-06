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
import be.pjrotsaert.mijnhogent.adapters.CourseAdapter
import be.pjrotsaert.mijnhogent.api.Chamilo
import be.pjrotsaert.mijnhogent.viewmodels.CoursesViewModel
import kotlinx.android.synthetic.main.courses_fragment.*

class CoursesFragment : Fragment() {

    companion object {
        fun newInstance(): CoursesFragment {
            return CoursesFragment()
        }
    }

    private lateinit var viewModel: CoursesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.courses_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CoursesViewModel::class.java)
        coursesRefreshPull.setOnRefreshListener { requestCourses() }

        coursesRecycler.layoutManager = LinearLayoutManager(context)
        coursesRecycler.itemAnimator = DefaultItemAnimator()
        coursesRecycler.adapter = CourseAdapter(viewModel.courses)
        coursesRecycler.adapter?.notifyDataSetChanged()

        if(viewModel.courses.size <= 0)
            requestCourses()
    }

    fun requestCourses(){
        coursesRefreshPull.isRefreshing = true
        Chamilo.getInstance(context!!).getCourses { courses, err ->
            if(courses != null){
                viewModel.courses.clear()
                var totalCredits = 0
                for(course in courses) {
                    viewModel.courses.add(course)
                    totalCredits += course.credits
                }
                txtTotalCredits.text = "${getString(R.string.total_credits)} $totalCredits"
                coursesRecycler.adapter?.notifyDataSetChanged()
            }
            else if(err != null && view != null)
                Snackbar.make(view!!, getString(err.getDescriptionId()), Snackbar.LENGTH_LONG).show()
            coursesRefreshPull.isRefreshing = false
        }
    }
}
