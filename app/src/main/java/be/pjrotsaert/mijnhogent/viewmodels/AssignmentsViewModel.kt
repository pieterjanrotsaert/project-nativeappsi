package be.pjrotsaert.mijnhogent.viewmodels

import android.arch.lifecycle.ViewModel;
import be.pjrotsaert.mijnhogent.api.AssignmentData

class AssignmentsViewModel : ViewModel() {
    var assignments: ArrayList<AssignmentData> = ArrayList()
}
