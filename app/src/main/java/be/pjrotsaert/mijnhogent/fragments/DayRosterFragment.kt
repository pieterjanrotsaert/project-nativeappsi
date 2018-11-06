package be.pjrotsaert.mijnhogent.fragments

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import be.pjrotsaert.mijnhogent.viewmodels.DayRosterViewModel
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.adapters.RosterAdapter
import be.pjrotsaert.mijnhogent.api.APIError
import be.pjrotsaert.mijnhogent.api.ActivityData
import be.pjrotsaert.mijnhogent.api.Chamilo
import be.pjrotsaert.mijnhogent.viewmodels.ActivityDayViewModel
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener
import kotlinx.android.synthetic.main.day_roster_fragment.*
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.ArrayList


class DayRosterFragment : Fragment(), DatePickerListener {

    companion object {
        fun newInstance(): DayRosterFragment {
            return DayRosterFragment()
        }
    }

    private lateinit var viewModel: DayRosterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.day_roster_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DayRosterViewModel::class.java)
        datePicker.setListener(this).init()
        datePicker.setDate(viewModel.currentDate)

        lessonExamsSegmentGroup.setTintColor(context?.resources?.getColor(R.color.colorPrimary)!!)

        buttonLessons.setOnCheckedChangeListener {
            _, state ->
            if(state && viewModel.currentActivityType != "lesson"){
                viewModel.currentActivityType = "lesson"
                requestActivities()
            }

        }

        buttonExams.setOnCheckedChangeListener {
            _, state ->
            if(state && viewModel.currentActivityType != "exam"){
                viewModel.currentActivityType = "exam"
                requestActivities()
            }
        }

        if(viewModel.currentActivityType == "lesson")
            buttonLessons.isChecked = true
        else
            buttonExams.isChecked = true

        activityRefreshPull.setOnRefreshListener { requestActivities() }

        rosterRecycler.layoutManager = LinearLayoutManager(context)
        rosterRecycler.itemAnimator = DefaultItemAnimator()
        rosterRecycler.adapter = RosterAdapter(viewModel.dayList)
        requestActivities()
    }

    override fun onDateSelected(dateSelected: DateTime?) {
        if(dateSelected != null) {
            viewModel.currentDate = dateSelected
            datePicker.setDate(dateSelected)
        }
        requestActivities()
    }

    fun requestActivities(){
        val startDate = viewModel.currentDate
        val endDate = startDate.plusDays(7)

        activityRefreshPull.isRefreshing = true
        Chamilo.getInstance(context!!).getActivities(startDate, endDate, viewModel.currentActivityType) {
            activities, err ->

            activityRefreshPull.isRefreshing = false
            viewModel.dayList.clear()

            val dayMap = HashMap<Long, ArrayList<ActivityData>>()

            if(activities != null){
                for(activity in activities){
                    val t = Date(activity.startDateTime.year, activity.startDateTime.month, activity.startDateTime.date).time
                    if(dayMap[t] == null)
                        dayMap[t] = ArrayList()
                    dayMap[t]?.add(activity)
                }
            }
            else if(err != null && view != null)
                Snackbar.make(view!!, err.getDescription(context!!), Snackbar.LENGTH_LONG).show()

            val sortedDays = dayMap.keys.toSortedSet()
            for(k in sortedDays){
                val dayModel = ActivityDayViewModel()
                dayModel.date = Date(k)
                dayModel.activities = dayMap[k]!!
                dayModel.activities.sortBy { a -> a.startDateTime.time }
                viewModel.dayList.add(dayModel)
            }

            rosterRecycler.adapter?.notifyDataSetChanged()
        }
    }
}
