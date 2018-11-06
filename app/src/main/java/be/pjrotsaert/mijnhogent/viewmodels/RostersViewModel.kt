package be.pjrotsaert.mijnhogent.viewmodels

import android.arch.lifecycle.ViewModel;
import org.joda.time.DateTime

class RostersViewModel : ViewModel() {
    var currentDate: DateTime = DateTime.now()
    var currentActivityType = "lesson"
    val dayList: ArrayList<ActivityDayViewModel> = ArrayList()
}
