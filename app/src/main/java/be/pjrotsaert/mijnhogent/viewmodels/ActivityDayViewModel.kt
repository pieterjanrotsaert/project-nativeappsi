package be.pjrotsaert.mijnhogent.viewmodels

import be.pjrotsaert.mijnhogent.api.ActivityData
import java.util.*

class ActivityDayViewModel {
    var date: Date = Date()
    var activities: ArrayList<ActivityData> = ArrayList()
}