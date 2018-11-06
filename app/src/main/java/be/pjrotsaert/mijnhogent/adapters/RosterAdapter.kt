package be.pjrotsaert.mijnhogent.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import be.pjrotsaert.mijnhogent.viewmodels.ActivityDayViewModel
import android.view.LayoutInflater
import android.widget.TableLayout
import android.widget.TextView
import be.pjrotsaert.mijnhogent.R
import java.text.SimpleDateFormat
import java.util.*


class RosterAdapter(data: ArrayList<ActivityDayViewModel>): RecyclerView.Adapter<RosterAdapter.ActivityViewHolder>() {

    private val dayList: ArrayList<ActivityDayViewModel> = data

    class ActivityViewHolder: RecyclerView.ViewHolder {
        val dateLabel: TextView
        val activityTable: TableLayout

        constructor(view: View): super(view) {
            dateLabel = view.findViewById(R.id.txtCardTitle)
            activityTable = view.findViewById(R.id.cardContentTable)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_layout, parent, false)
        return ActivityViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dayList.size
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, index: Int) {
        val model = dayList[index]

        val cal = Calendar.getInstance()
        cal.time = model.date
        var dayName = "Maandag"
        if(cal.get(Calendar.DAY_OF_WEEK) - 2 == 0) dayName = "Maandag"
        else if(cal.get(Calendar.DAY_OF_WEEK) - 2 == 1) dayName = "Dinsdag"
        else if(cal.get(Calendar.DAY_OF_WEEK) - 2 == 2) dayName = "Woensdag"
        else if(cal.get(Calendar.DAY_OF_WEEK) - 2 == 3) dayName = "Donderdag"
        else if(cal.get(Calendar.DAY_OF_WEEK) - 2 == 4) dayName = "Vrijdag"
        else if(cal.get(Calendar.DAY_OF_WEEK) - 2 == 5) dayName = "Zaterdag"
        else if(cal.get(Calendar.DAY_OF_WEEK) - 2 == 6) dayName = "Zondag"

        holder.dateLabel.text = "$dayName ${SimpleDateFormat("dd/MM/yyyy").format(model.date)}"
        holder.activityTable.removeAllViews()

        val inflater = LayoutInflater.from(holder.activityTable.context)
        var addSeparator = false

        for(activity in model.activities){

            if(addSeparator)
                holder.activityTable.addView(inflater.inflate(R.layout.table_row_separator, holder.activityTable, false))

            val row = inflater.inflate(R.layout.roster_table_row, holder.activityTable, false)

            val txtTime = row.findViewById<TextView>(R.id.txtTime)
            val txtLocation = row.findViewById<TextView>(R.id.txtLocation)
            val txtSubject = row.findViewById<TextView>(R.id.txtSubjectName)
            val txtTeacher = row.findViewById<TextView>(R.id.txtTeacher)


            txtTime.text = "${SimpleDateFormat("HH:mm").format(activity.startDateTime)} - ${SimpleDateFormat("HH:mm").format(activity.endDateTime)}"
            txtSubject.text = activity.activityDescription
            txtTeacher.text = activity.staffDescription
            txtLocation.text = activity.locationDescription

            holder.activityTable.addView(row)
            addSeparator = true
        }
    }
}