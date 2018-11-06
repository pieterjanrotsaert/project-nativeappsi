package be.pjrotsaert.mijnhogent.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.viewmodels.AssignmentsViewModel

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
        // TODO: Use the ViewModel
    }

}
