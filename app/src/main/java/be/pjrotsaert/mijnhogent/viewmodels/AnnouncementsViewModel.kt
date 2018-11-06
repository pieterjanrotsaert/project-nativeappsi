package be.pjrotsaert.mijnhogent.viewmodels

import android.arch.lifecycle.ViewModel
import be.pjrotsaert.mijnhogent.api.AnnouncementData

class AnnouncementsViewModel : ViewModel() {
    var announcements: ArrayList<AnnouncementData> = ArrayList()
}
