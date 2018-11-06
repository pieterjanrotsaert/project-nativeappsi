package be.pjrotsaert.mijnhogent.api

import android.content.Context
import android.util.Base64
import android.util.JsonWriter
import be.pjrotsaert.mijnhogent.R
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import com.android.volley.toolbox.HurlStack
import org.apache.commons.lang3.StringEscapeUtils
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.function.IntToDoubleFunction


class Chamilo {

    private class GenericResponse {
        var statusCode: Int = 0
        var headers: HashMap<String, String> = HashMap()
        var cookies: HashMap<String, String> = HashMap()
        var body: String = ""
        var bodyBin: ByteArray = ByteArray(0)
        var error: String? = null
    }

    private class GenericRequest(
            method: Int,
            url: String,
            private val params: HashMap<String, String>,
            private val headers: MutableMap<String, String>?,
            private val listener: Response.Listener<GenericResponse>,
            errorListener: Response.ErrorListener
    ) : Request<GenericResponse>(method, url, errorListener) {

        init {
            if(headers != null && body != null){
                headers["Content-Length"] = body.size.toString()
                headers["Content-Type"]   = "application/x-www-form-urlencoded"
            }
        }

        override fun getParams(): MutableMap<String, String> {
            return params
        }

        override fun getHeaders(): MutableMap<String, String> = headers ?: super.getHeaders()
        override fun deliverResponse(response: GenericResponse) = listener.onResponse(response)
        override fun parseNetworkResponse(response: NetworkResponse?): Response<GenericResponse> {
            return if(response != null)
                Response.success(Chamilo.parseResponse(response), null)
            else
                Response.error(VolleyError("Response was null!"))
        }
    }

    companion object {
        val singleton = Chamilo()

        fun getInstance(appContext: Context): Chamilo {
            singleton.init(appContext)
            return singleton
        }

        private fun parseResponse(networkResponse: NetworkResponse) : GenericResponse{
            val result = GenericResponse()
            result.statusCode = networkResponse.statusCode
            result.bodyBin = networkResponse.data ?: ByteArray(0)
            for(header in networkResponse.allHeaders){
                if(header.name.toLowerCase() != "set-cookie")
                    result.headers[header.name] = header.value
                else
                {
                    var cookies = header.value.split(";")
                    for(cookie in cookies){
                        if(cookie.contains("=")){
                            var pair = cookie.split("=")
                            if(pair.count() >= 2)
                                result.cookies[pair[0].trim()] = pair[1].trim()
                        }
                    }
                }
            }
            try {
                result.body = String(networkResponse.data ?: ByteArray(0), Charset.forName(HttpHeaderParser.parseCharset(networkResponse.headers)))
            } catch (e: UnsupportedEncodingException) {
                result.error = e.message ?: "Unknown encoding error."
            }
            return result
        }

        private fun parseBodyPart(body: String, start: String, end: String): String? {
            val idxStart = body.indexOf(start)
            if(body.indexOf(start) != -1){
                val idxEnd = body.indexOf(end, idxStart + start.length)
                if(idxEnd != -1){
                    return body.substring(idxStart + start.length, idxEnd)
                }
                else
                    return null
            }
            else
                return null
        }

        private fun parseMultiBodyPart(body: String, start: String, end: String): ArrayList<String> {
            val list = ArrayList<String>()
            var curIdx = 0

            while(true){
                val idxStart = body.indexOf(start, curIdx)
                if(idxStart != -1){
                    val idxEnd = body.indexOf(end, idxStart + start.length)
                    if(idxEnd != -1){
                        curIdx = idxEnd + end.length
                        list.add(body.substring(idxStart + start.length, idxEnd))
                    }
                    else
                        break
                }
                else
                    break
            }
            return list
        }
    }

    private var ctx: Context? = null
    private var queue: RequestQueue?  = null

    private val urlHogentIdpBase        = "https://idp.hogent.be"
    private val urlChamiloBase          = "https://chamilo.hogent.be"
    private val urlLoginBase            = "https://login.hogent.be"

    private val urlHogentLoginAction    = "$urlLoginBase/?action="
    private val urlHogentSSO            = "$urlHogentIdpBase/saml/idp/profile/redirectorpost/sso"
    private val urlHogentSLS            = "$urlHogentIdpBase/saml/idp/profile/post/sls"
    private val urlHogentMyPolicy       = "$urlHogentIdpBase/my.policy"

    private val urlChamiloLogin         = "$urlChamiloBase/index.php?application=Hogent%5CIntegration%5CSSO&go=adfs_login"
    private val urlChamiloLogout        = "$urlChamiloBase/index.php?application=Chamilo%5CCore%5CUser&go=Logout"
    private val urlChamiloSAMLSubmit    = "$urlChamiloBase/sso/module.php/saml/sp/saml2-acs.php/default-sp"
    private val urlChamiloHome          = "$urlChamiloBase/index.php?application=Chamilo%5CCore%5CHome"
    private val urlChamiloGetActivities = "$urlChamiloBase/index.php?go=GetActivities&application=Hogent%5CApplication%5CSyllabusPlus%5CAjax"
    private val urlChamiloGetProfilePic = "$urlChamiloBase/index.php?application=Chamilo%5CCore%5CUser%5CAjax&go=UserPicture&user_id="
    private val urlChamiloCurriculum    = "$urlChamiloBase/index.php?application=Hogent\\Application\\Bamaflex"
    private val urlChamiloGetCourse     = "$urlChamiloBase/index.php?go=CourseViewer&application=Chamilo%5CApplication%5CWeblcms&course="


    private val urlChamiloSuffixGetAssignmentPublication    = "&tool=Assignment&browser=Table&tool_action=Display&publication="
    private val urlChamiloSuffixGetAssignment               = "&tool=Assignment"


    private var cookies = HashMap<String, String>()
    private var cookieBlacklist = ArrayList<String>()
    private var userId = 0
    private var userFullName = "Naam ongekend"
    private var userEmailAddress = "E-Mail ongekend"
    private var profileImageData = "" // Base64 encoded imagedata

    init {
        cookieBlacklist.add("domain")
        cookieBlacklist.add("path")
        cookieBlacklist.add("location")
        cookieBlacklist.add("expires")
        cookieBlacklist.add("mrhshint")
    }

    private fun updateCookies(_cookies: HashMap<String, String>){
        for(cookie in _cookies){
            if(cookie.value.isNotEmpty())
                cookies[cookie.key] = cookie.value
        }
    }

    private fun sendRequest(method: Int, url: String,  headers: HashMap<String, String>?, bodyParams: HashMap<String, String>?, callback: (result: GenericResponse) -> Unit ){
        val req = GenericRequest(method, url, bodyParams ?: HashMap(), headers,
            Response.Listener { response ->
                if(response != null){
                    updateCookies(response.cookies)
                    callback(response)
                }
            },
            Response.ErrorListener { error ->
                if(error.networkResponse != null){
                    val response = parseResponse(error.networkResponse)
                    updateCookies(response.cookies)
                    callback(response)
                }
            })
        queue?.add(req)
    }

    private fun constructHeaders(): HashMap<String, String>{
        val headers = HashMap<String, String>()

        var cookieString = ""
        for(cookie in cookies){
            if(!cookieBlacklist.contains(cookie.key.toLowerCase())){
                cookieString += cookie.key + "=" + cookie.value + "; "
            }
        }

        headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.67 Safari/537.36"
        headers["Accept-Encoding"] = "identity"
        headers["Accept"] = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
        headers["Origin"] = urlChamiloBase
        headers["Accept-Language"] = "en-US,en;q=0.9,nl;q=0.8"
        headers["Cache-Control"] = "max-age=0"
        headers["Connection"] = "keep-alive"
        headers["Upgrade-Insecure-Requests"] = "1"
        headers["Cookie"] = cookieString

        return headers
    }

    private fun initiateSession(cb: (token: Boolean) -> Unit) {
        if(isSessionInitialized())
            cb(true) // Session already initialized, return immediately
        else
        {
            // Simply GETting the chamilo homepage will give us a session id cookie
            sendRequest(Request.Method.GET, "$urlChamiloBase/", constructHeaders(), null) {
                result -> cb(isSessionInitialized())
            }
        }
    }

    private fun init(appContext: Context){
        if(ctx == appContext)
            return

        ctx = appContext
        queue = Volley.newRequestQueue(ctx, object : HurlStack() {
            override fun createConnection(url: URL): HttpURLConnection {
                val connection = super.createConnection(url)
                connection.instanceFollowRedirects = false
                return connection
            }
        })
    }

    fun getActivities(startTime: DateTime, endTime: DateTime, activityType: String, callback: (activities: ArrayList<ActivityData>?, err: APIError?) -> Unit) {
        val calStart = Calendar.getInstance()
        val calEnd = Calendar.getInstance()
        calStart.time = startTime.toDate()
        calEnd.time = endTime.toDate()

        val list = ArrayList<ActivityData>()

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        formatter.timeZone = TimeZone.getTimeZone("GMT")

        getActivities(calStart, calEnd, activityType){
            jsonString, err ->
            if(err != null)
                callback(null, err)
            else try {
                val resultJson = JSONObject(jsonString)
                if(resultJson.getString("result_message") != "OK")
                    callback(null, APIError(R.string.err_internal, R.string.err_internal_description))
                else {
                    val innerJson = JSONObject(resultJson.getJSONObject("properties").getString("activities"))
                    val dataArr = innerJson.getJSONArray("data")
                    for(i in 0 until dataArr.length()){
                        val jsonData = dataArr.getJSONObject(i)
                        val d = ActivityData()
                        d.activityId = jsonData.getString("Activity_Id")

                        var date = jsonData.getString("StartDateTime")
                        date = date.substring(0, date.indexOf("+"))

                        d.startDateTime = formatter.parse(date)

                        date = jsonData.getString("EndDateTime")
                        date = date.substring(0, date.indexOf("+"))
                        d.endDateTime = formatter.parse(date)

                        d.activityDescription = jsonData.getString("Activity_Description")
                        d.weekLabel = jsonData.getString("WeekLabel")
                        d.activityType = jsonData.getString("ActivityType_Description")
                        d.locationDescription = jsonData.getString("Locations_Description")
                        d.moduleDescription = jsonData.getString("Modules_Description")
                        d.staffDescription = jsonData.getString("Staff_Description")
                        d.studentGroupDescription = jsonData.getString("StudentSetGroup_Description")
                        list.add(d)
                    }
                    callback(list, null)
                }
            }
            catch (ex: Exception){
                callback(null, APIError(R.string.err_internal, R.string.err_internal_description))
            }
        }
    }

    fun getActivities(startTime: Calendar, endTime: Calendar, activityType: String, callback: (activities: String, err: APIError?) -> Unit) {
        getActivities("${startTime.get(Calendar.YEAR)}-${startTime.get(Calendar.MONTH)+1}-${startTime.get(Calendar.DAY_OF_MONTH)}",
                "${endTime.get(Calendar.YEAR)}-${endTime.get(Calendar.MONTH)+1}-${endTime.get(Calendar.DAY_OF_MONTH)}", activityType, callback)
    }

    fun getActivities(startTime: String, endTime:String, activityType: String, callback: (activities: String, err: APIError?) -> Unit) {
        val params = HashMap<String, String>()
        params["start_time"] = startTime
        params["end_time"] = endTime
        params["subgroups"] = ""
        params["activity_type"] = activityType

        sendRequest(Request.Method.POST, urlChamiloGetActivities, constructHeaders(), params){
            result ->

            try{
                val json = JSONObject(result.body)
                callback(result.body, null)
            }
            catch (ex: Exception){
                callback("", APIError(R.string.err_notloggedin, R.string.err_notloggedin_description))
            }
        }
    }

    private fun downloadProfilePic(callback: (String, err: APIError?) -> Unit){
        sendRequest(Request.Method.GET, "$urlChamiloGetProfilePic$userId", constructHeaders(), null){
            result ->
            if(result.statusCode == 200){
                profileImageData = Base64.encodeToString(result.bodyBin, Base64.DEFAULT)
                callback(profileImageData, null)
            }
            else
                callback("", APIError(R.string.err_internal, R.string.err_internal_description))
        }
    }

    fun getProfilePic(callback: (imgBase64: String, err:APIError?) -> Unit){
        if(profileImageData.isNotEmpty())
            callback(profileImageData, null)
        else
            downloadProfilePic(callback)
    }

    fun getUserFullName(): String {
        return userFullName
    }

    fun getUserEmailAddress(): String {
        return userEmailAddress
    }

    fun getCourses(callback: (ArrayList<CourseData>?, err: APIError?) -> Unit){
        sendRequest(Request.Method.GET, urlChamiloCurriculum, constructHeaders(), null) {
            result ->
            if(result.statusCode == 200){
                val courseJsonString = parseBodyPart(result.body, "var courses = ", ";")
                val list = ArrayList<CourseData>()
                try {
                    val dataArr = JSONArray(courseJsonString)
                    for(i in 0 until dataArr.length()){
                        val d = CourseData()
                        val jsonData = dataArr.getJSONObject(i)
                        d.identifier = jsonData.getString("identifier")
                        d.bamaflex_course_id = jsonData.getInt("bamaflex_course_id")
                        d.parent_bamaflex_course_id = jsonData.getInt("parent_bamaflex_course_id")
                        d.chamilo_course_id = jsonData.getInt("chamilo_course_id")
                        d.course_type = jsonData.getString("course_type")
                        d.title = jsonData.getString("title")
                        d.chamilo_course_code = jsonData.getString("chamilo_course_code")
                        d.training_name = jsonData.getString("training_name")
                        d.training_code = jsonData.getString("training_code")
                        d.titular_name = jsonData.getString("titular_name")
                        d.credits = jsonData.getInt("credits")
                        d.subscribed_to_course = jsonData.getBoolean("subscribed_to_course")
                        list.add(d)
                    }
                    callback(list, null)
                } catch(ex: Exception){
                    callback(null, APIError(R.string.err_internal, R.string.err_internal_description))
                }
            }
            else
                callback(null, APIError(R.string.err_internal, R.string.err_internal_description))
        }
    }

    // Retrieves all assignments for a given course
    // WARNING: This function has to send multiple requests and may take a while to complete. (Unfortunately chamilo doesn't have a proper REST API)
    fun getAssignments(courseId: Int, callback: (ArrayList<AssignmentData>?, err: APIError?) -> Unit){
        val formatter = SimpleDateFormat("dd/MM/yyyy' om 'HH:mm")

        sendRequest(Request.Method.GET, "$urlChamiloGetCourse$courseId$urlChamiloSuffixGetAssignment", constructHeaders(), null) {
            result ->
            if(result.statusCode == 200){
                val assignmentIds = parseMultiBodyPart(result.body, "publication[]\" value=\"", "\"")
                val results = ArrayList<AssignmentData>()

                for(assignmentId in assignmentIds){
                    val assign = AssignmentData()
                    val assignmentBody = parseBodyPart(result.body, "Display&publication=$assignmentId", "<div class=\"clear\">&nbsp;</div></div></td>") ?: ""
                    val tableColumns = parseMultiBodyPart(assignmentBody, "<td>", "</td>")

                    assign.publicationId = assignmentId.toInt()
                    if(tableColumns.size >= 7){
                        assign.publicationDate = formatter.parse(tableColumns[1])
                        assign.poster = tableColumns[3]
                        assign.endTime = formatter.parse(tableColumns[5])
                        assign.submissions = tableColumns[6]
                        assign.submissionAllowed = assignmentBody.contains("assignment_display_action=Creator", true)
                    }
                    results.add(assign)
                }

                for(assign in results){
                    sendRequest(Request.Method.GET, "$urlChamiloGetCourse$courseId$urlChamiloSuffixGetAssignmentPublication${assign.publicationId}",
                            constructHeaders(), null){
                        result ->
                        assign.title = parseBodyPart(result.body, "<h3 class=\"title-underlined\">", "</h3>") ?: ""
                        assign.description = parseBodyPart(result.body, "<p>", "</p>") ?: ""
                        assign.description = assign.description.replace("<br>", "\r\n")
                        assign.description = assign.description.replace("<br/>", "\r\n")
                        assign.description = StringEscapeUtils.unescapeHtml4(assign.description)

                        assign._assignmentLoaded = true

                        var ready = true
                        for(assignment in results){
                            if(!assignment._assignmentLoaded)
                                ready = false
                        }
                        if(ready)
                            callback(results, null)
                    }
                }
            }
            else
                callback(null, APIError(R.string.err_internal, R.string.err_internal_description))
        }
    }

    // Populate each CourseData object with its relevant assignments
    fun getAllAssignments(courses: ArrayList<CourseData>, callback: (err: APIError?) -> Unit){

        var nCompleted = 0
        for(course in courses){
            getAssignments(course.chamilo_course_id){
                result, err ->

                if(result != null)
                    course.assignments = result
                else {
                    callback(err)
                    return@getAssignments
                }

                nCompleted++
                if(nCompleted == courses.size)
                    callback(null)
            }
        }
    }

    // Returns current session info (cookies) as a JSON string. The result can be passed to restoreSessionData()
    fun getSessionData(): String {
        val json = JSONObject()
        val cookieJson = JSONObject()
        for(pair in cookies)
            cookieJson.put(pair.key, pair.value)
        json.put("cookies", cookieJson)
        json.put("userId", userId)
        json.put("userFullName", userFullName)
        json.put("userEmailAddress", userEmailAddress)
        json.put("profileImageData", profileImageData)
        return json.toString()
    }

    fun restoreSessionData(sessionData: String, callback: (err: APIError?) -> Unit) {
        try {
            val json = JSONObject(sessionData)
            val cookieJson = json.getJSONObject("cookies")
            userId = json.getInt("userId")
            userFullName = json.getString("userFullName")
            userEmailAddress = json.getString("userEmailAddress")
            profileImageData = json.getString("profileImageData")
            for (k in cookieJson.keys())
                cookies[k] = cookieJson.getString(k)
        }
        catch(ex: Exception){
        }

        // Test if the session is still valid by calling getActivities(), if it doesn't return an error, the session is still valid.
        getActivities("1990-1-1", "1990-1-1", "lesson") { result, err ->
            if(err != null) {
                userId = 0
                cookies.clear() // Session restoration not successful, clear cookies.
            }
            callback(err)
        }
    }

    fun isLoggedIn(): Boolean {
        return cookies["SimpleSAMLAuthToken"] != null
    }

    fun isSessionInitialized(): Boolean {
        return cookies["SimpleSAMLSessionID"] != null
    }

    fun login(username: String, password: String, callback: (error: APIError?) -> Unit) {
        if(isLoggedIn()){
            callback(null)
            return
        }
        initiateSession { success ->
            if(success){
                val params = HashMap<String, String>()
                sendRequest(Request.Method.GET, urlChamiloLogin, constructHeaders(), params) {
                    result ->
                    val samlRequest: String = parseBodyPart(result.body, "name=\"SAMLRequest\" value=\"", "\"") ?: ""
                    val relayState: String = parseBodyPart(result.body, "name=\"RelayState\" value=\"", "\"") ?: ""

                    if(samlRequest.isNotEmpty() && relayState.isNotEmpty()){
                        params.clear()
                        params["SAMLRequest"] = samlRequest
                        params["RelayState"] = relayState
                        sendRequest(Request.Method.POST, urlHogentSSO, constructHeaders(), params){
                            result ->
                            if(result.statusCode == 302) { // 302 -> Http redirect
                                params.clear()

                                // This is a useless request... But for some reason if we do don't make this request before proceeding,
                                // the server will return an internal error.. (Probably a bug with Chamilo?)
                                sendRequest(Request.Method.GET, urlHogentMyPolicy, constructHeaders(), params) { _ ->
                                    params["client_data"] = "SecurityDevice"
                                    params["post_url"] = urlHogentMyPolicy
                                    sendRequest(Request.Method.POST, urlHogentLoginAction, constructHeaders(), params) { result ->
                                        if (result.statusCode == 200) {
                                            params.clear()
                                            params["username"] = username
                                            params["password"] = password
                                            sendRequest(Request.Method.POST, urlHogentMyPolicy, constructHeaders(), params) { result ->
                                                val dummy: String = parseBodyPart(result.body, "name=\"dummy\" value=\"", "\"") ?: ""
                                                if (dummy.isNotEmpty()) {
                                                    params.clear()
                                                    params["dummy"] = dummy
                                                    params["SAMLRequest"] = samlRequest
                                                    sendRequest(Request.Method.POST, urlHogentSSO, constructHeaders(), params) { result ->
                                                        val samlResponse: String = parseBodyPart(result.body, "name=\"SAMLResponse\" value=\"", "\"")
                                                                ?: ""
                                                        if (samlResponse.isNotEmpty()) {
                                                            params.clear()
                                                            params["SAMLResponse"] = samlResponse
                                                            params["RelayState"] = relayState
                                                            sendRequest(Request.Method.POST, urlChamiloSAMLSubmit, constructHeaders(), params) { result ->
                                                                if (isLoggedIn()) { // SUCCESS
                                                                    sendRequest(Request.Method.GET, urlChamiloHome, constructHeaders(), null){ result ->
                                                                        if(result.statusCode == 302){ // For some reason, the server returns a redirect on the first GET..
                                                                            sendRequest(Request.Method.GET, urlChamiloHome, constructHeaders(), null) { result ->
                                                                                val idStr = parseBodyPart(result.body, "user_id=", "\"") ?: ""
                                                                                userId = idStr.toIntOrNull() ?: 0
                                                                                userFullName = parseBodyPart(result.body, "account-data-name\">", "<") ?: "Naam ongekend"
                                                                                userEmailAddress = parseBodyPart(result.body, "account-data-email\">", "<") ?: "E-mail ongekend"
                                                                                profileImageData = ""

                                                                                callback(null)
                                                                            }
                                                                        }
                                                                        else {
                                                                            val idStr = parseBodyPart(result.body, "user_id=", "\"") ?: ""
                                                                            userId = idStr.toIntOrNull() ?: 0
                                                                            callback(null)
                                                                        }
                                                                    }
                                                                }
                                                                else
                                                                    callback(APIError(R.string.err_internal, R.string.err_internal_description))
                                                            }
                                                        }
                                                        else
                                                            callback(APIError(R.string.err_internal, R.string.err_internal_description))
                                                    }
                                                } else {
                                                    if (result.headers["Location"] == null)
                                                        callback(APIError(R.string.err_internal, R.string.err_internal_description))
                                                    else if (result.headers["Location"]!!.contains("errorcode=21")) // 21 is the errorcode for invalid username/password
                                                        callback(APIError(R.string.err_wrongcredentials, R.string.err_wrongcredentials_description))
                                                    else
                                                        callback(APIError(R.string.err_internal, R.string.err_internal_description))
                                                }
                                            }
                                        }
                                        else
                                            callback(APIError(R.string.err_internal, R.string.err_internal_description))
                                    }
                                }
                            }
                            else
                                callback(APIError(R.string.err_internal, R.string.err_internal_description))
                        }
                    }
                    else
                        callback(APIError(R.string.err_internal, R.string.err_internal_description))
                }
            }
            else
                callback(APIError(R.string.err_creatingsession, R.string.err_creatingsession_description))
        }
    }

    fun logout(callback: (err: APIError?) -> Unit){
        sendRequest(Request.Method.GET, urlChamiloLogout, constructHeaders(), null) {
            result ->
            if(result.statusCode == 200){
                val samlRequest: String = parseBodyPart(result.body, "name=\"SAMLRequest\" value=\"", "\"") ?: ""
                val params = HashMap<String, String>()
                params["SAMLRequest"] = samlRequest
                sendRequest(Request.Method.POST, urlHogentSLS, constructHeaders(), params){
                    result ->
                    val samlResponse: String = parseBodyPart(result.body, "name=\"SAMLResponse\" value=\"", "\"") ?: ""
                    if(samlResponse.isNotEmpty()){
                        params.clear()
                        params["SAMLResponse"] = samlResponse
                        sendRequest(Request.Method.POST, "$urlChamiloBase/", constructHeaders(), params){
                            result ->
                            if(result.statusCode == 200){
                                cookies.clear()
                                callback(null)
                            }
                            else{
                                cookies.clear()
                                callback(APIError(R.string.err_internal, R.string.err_internal_description))
                            }
                        }
                    }
                    else{
                        cookies.clear()
                        callback(APIError(R.string.err_internal, R.string.err_internal_description))
                    }
                }
            }
            else{
                cookies.clear()
                callback(APIError(R.string.err_internal, R.string.err_internal_description))
            }
        }
    }

}







