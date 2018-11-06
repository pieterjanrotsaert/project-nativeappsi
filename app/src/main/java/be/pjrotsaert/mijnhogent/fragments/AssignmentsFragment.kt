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
import be.pjrotsaert.mijnhogent.viewmodels.AssignmentsViewModel
import kotlinx.android.synthetic.main.assignments_fragment.*

class AssignmentsFragment : Fragment() {

    companion object {
        fun newInstance(): AssignmentsFragment {
            return AssignmentsFragment()
        }
    }

    private lateinit var viewModel: AssignmentsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.assignments_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AssignmentsViewModel::class.java)
        assignmentRefreshPull.setOnRefreshListener { requestAssignments() }

        assignmentRecycler.layoutManager = LinearLayoutManager(context)
        assignmentRecycler.itemAnimator = DefaultItemAnimator()
        assignmentRecycler.adapter = AssignmentAdapter(viewModel.assignments)
        requestAssignments()
    }

    fun requestAssignments(){
        assignmentRefreshPull.isRefreshing = true
        Chamilo.getInstance(context!!).getCourses { courses, err ->
            if(courses != null){
                Chamilo.getInstance(context!!).getAllAssignments(courses) { err ->
                    if(err == null){
                        viewModel.assignments.clear()

                        for(course in courses){
                            for(assignment in course.assignments){
                                if(assignment.submissionAllowed && !assignment.submitted){
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
            else if(err != null && view != null){
                Snackbar.make(view!!, getString(err.getDescriptionId()), Snackbar.LENGTH_LONG).show()
                assignmentRefreshPull.isRefreshing = false
            }

        }
    }

}
