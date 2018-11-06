package be.pjrotsaert.mijnhogent.api

import java.util.*

class AssignmentData {
    var title = ""
    var description = ""
    var subjectName = ""
    var poster = ""
    var publicationId = 0
    var publicationDate = Date()
    var endTime = Date()
    var submissions = "0/0"
    var submissionAllowed = false
    var submitted = false
    var _assignmentLoaded = false
}