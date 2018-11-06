package be.pjrotsaert.mijnhogent.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.viewmodels.SummaryViewModel

class SummaryFragment : Fragment() {

    companion object {
        fun newInstance(): SummaryFragment {
            return SummaryFragment()
        }
    }

    private lateinit var viewModel: SummaryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.summary_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SummaryViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
