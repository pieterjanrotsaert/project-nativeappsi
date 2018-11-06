package be.pjrotsaert.mijnhogent.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.viewmodels.AnnouncementsViewModel

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
        // TODO: Use the ViewModel
    }

}
