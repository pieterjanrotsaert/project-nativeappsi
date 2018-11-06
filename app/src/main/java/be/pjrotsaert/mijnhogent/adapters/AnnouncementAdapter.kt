package be.pjrotsaert.mijnhogent.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.api.AnnouncementData
import java.text.SimpleDateFormat
import java.util.ArrayList


class AnnouncementAdapter(data: ArrayList<AnnouncementData>): RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    private val announcementList: ArrayList<AnnouncementData> = data

    class AnnouncementViewHolder: RecyclerView.ViewHolder {
        val subjectName: TextView
        val pubDate: TextView
        val assignmentTitle: TextView
        val description: TextView
        val poster: TextView 

        constructor(view: View): super(view) {
            subjectName     = view.findViewById(R.id.txtAnnouncementSubject)
            pubDate         = view.findViewById(R.id.txtAnnouncementDate)
            assignmentTitle = view.findViewById(R.id.txtAnnouncementTitle)
            description     = view.findViewById(R.id.txtAnnouncementDescription)
            poster          = view.findViewById(R.id.txtAnnouncementPoster)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.single_announcement_layout, parent, false)
        return AnnouncementViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return announcementList.size
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, index: Int) {
        val model = announcementList[index]

        holder.subjectName.text = model.subjectName
        holder.pubDate.text = SimpleDateFormat("dd/MM/yyyy - HH:mm").format(model.publicationDate)
        holder.poster.text = model.poster
        holder.assignmentTitle.text = model.title
        holder.description.text = model.description
    }
}