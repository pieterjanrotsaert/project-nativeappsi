package be.pjrotsaert.mijnhogent.api
import kotlinx.serialization.*

@Serializable
data class CourseData (
    var identifier: String = "",
    var bamaflex_course_id: Int = 0,
    var parent_bamaflex_course_id: Int = 0,
    var chamilo_course_id: Int = 0,
    var course_type: String = "eOLOD",
    var statuses: ArrayList<String> = ArrayList(),
    var title: String = "",
    var chamilo_course_code: String = "",
    var training_name: String = "",
    var training_code: String = "",
    var titular_name: String = "",
    var credits: Int = 0,
    var subscribed_to_course: Boolean = false,
    var teacher_in_course: Boolean = false,
    var allowed_to_create: Boolean = false,
    var allowed_to_delete: Boolean = false,
    var fullDescription: String = "",


    var assignments: ArrayList<AssignmentData> = ArrayList(),
    var announcements: ArrayList<AnnouncementData> = ArrayList()
)