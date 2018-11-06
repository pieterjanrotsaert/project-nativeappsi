package be.pjrotsaert.mijnhogent.api

class CourseData {
    var identifier = ""
    var bamaflex_course_id = 0
    var parent_bamaflex_course_id = 0
    var chamilo_course_id = 0
    var course_type = "eOLOD"
    var statuses = ArrayList<String>()
    var title = ""
    var chamilo_course_code = ""
    var training_name = ""
    var training_code = ""
    var titular_name = ""
    var credits = 0
    var subscribed_to_course = false
    var teacher_in_course = false
    var allowed_to_create = false
    var allowed_to_delete = false

    var assignments = ArrayList<AssignmentData>()
    var announcements = ArrayList<AnnouncementData>()
}