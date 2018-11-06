package be.pjrotsaert.mijnhogent.viewmodels

import android.arch.lifecycle.ViewModel
import be.pjrotsaert.mijnhogent.api.CourseData

class CoursesViewModel : ViewModel() {
    var courses: ArrayList<CourseData> = ArrayList()
}
