package be.pjrotsaert.mijnhogent.adapters

import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.activities.CourseActivity
import be.pjrotsaert.mijnhogent.api.AssignmentData
import be.pjrotsaert.mijnhogent.api.CourseData
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*


class AssignmentAdapter(data: ArrayList<AssignmentData>, clickable: Boolean = false): RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {

    private val assignmentList: ArrayList<AssignmentData> = data
    private val isClickable = clickable

    class AssignmentViewHolder: RecyclerView.ViewHolder {
        val cardTitle: TextView
        val endDate: TextView
        val assignmentTitle: TextView
        val description: TextView
        val layout: CardView

        constructor(view: View): super(view) {
            cardTitle = view.findViewById(R.id.txtCardTitle)
            endDate = view.findViewById(R.id.txtAssignmentDate)
            assignmentTitle = view.findViewById(R.id.txtAssignmentTitle)
            description = view.findViewById(R.id.txtAssignmentDescription)
            layout = view.findViewById(R.id.assignmentLayout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.single_assignment_layout, parent, false)
        return AssignmentViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return assignmentList.size
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, index: Int) {
        val model = assignmentList[index]

        val now = DateTime.now().toLocalDate().toDate()

        holder.cardTitle.text = model.subjectName
        holder.endDate.text = SimpleDateFormat("dd/MM/yyyy - HH:mm").format(model.endTime)
        if(model.endTime.time < now.time) {
            holder.endDate.setTextColor(ContextCompat.getColor(holder.endDate.context, R.color.textRed))
            holder.endDate.text = SimpleDateFormat("dd/MM/yyyy - HH:mm").format(model.endTime) + " (gemist!)"
        }
        else
            holder.endDate.setTextColor(ContextCompat.getColor(holder.endDate.context, R.color.primaryTextColor))

        holder.assignmentTitle.text = model.title
        holder.description.text = model.description

        if(isClickable){
            val course = model.course.copy()
            course.assignments.clear()
            course.announcements.clear()

            holder.layout.setOnClickListener {
                ContextCompat.startActivity(holder.layout.context, CourseActivity.newIntent(holder.layout.context, course), null)
            }
        }
    }
}