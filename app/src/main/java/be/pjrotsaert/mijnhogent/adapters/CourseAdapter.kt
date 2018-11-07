package be.pjrotsaert.mijnhogent.adapters

import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.activities.CourseActivity
import be.pjrotsaert.mijnhogent.api.CourseData

class CourseAdapter(data: ArrayList<CourseData>): RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private val courseList: ArrayList<CourseData> = data

    class CourseViewHolder: RecyclerView.ViewHolder {
        val courseName: TextView
        val titularisName: TextView
        val courseType: TextView
        val sp: TextView
        val separator: ImageView
        val layout: ConstraintLayout

        constructor(view: View): super(view) {
            courseName      = view.findViewById(R.id.txtName)
            titularisName   = view.findViewById(R.id.txtTitularis)
            courseType      = view.findViewById(R.id.txtType)
            sp              = view.findViewById(R.id.txtSP)
            separator       = view.findViewById(R.id.courseSeparator)
            layout          = view.findViewById(R.id.courseEntryLayout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.course_fragment_entry, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    override fun onBindViewHolder(holder: CourseViewHolder, index: Int) {
        val model = courseList[index]

        holder.courseName.text = model.title
        holder.sp.text = "SP: ${model.credits}"
        holder.titularisName.text = model.titular_name
        holder.courseType.text = model.course_type
        if(index == courseList.size - 1)
            holder.separator.visibility = View.INVISIBLE
        else
            holder.separator.visibility = View.VISIBLE

        holder.layout.setOnClickListener {
            startActivity(holder.layout.context, CourseActivity.newIntent(holder.layout.context, model), null)
        }
    }
}