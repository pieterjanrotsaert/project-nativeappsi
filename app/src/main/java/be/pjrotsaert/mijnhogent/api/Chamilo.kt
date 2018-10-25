package be.pjrotsaert.mijnhogent.api

import android.content.Context
import be.pjrotsaert.mijnhogent.R
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import com.android.volley.toolbox.HurlStack
import java.net.HttpURLConnection
import java.net.URL


class Chamilo {

    class GenericResponse {
        var statusCode: Int = 0
        var headers: HashMap<String, String> = HashMap()
        var cookies: HashMap<String, String> = HashMap()
        var body: String = ""
        var bodyBin: ByteArray = ByteArray(0)
        var error: String? = null
    }

    class GenericRequest(
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
                headers["Content-Type"] = "application/x-www-form-urlencoded"
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
    }

    var ctx: Context? = null
    var queue: RequestQueue?  = null

    val idpHogentUrl = "https://idp.hogent.be"
    val chamiloUrl = "https://chamilo.hogent.be"
    val loginUrl = "https://login.hogent.be"

    var cookies = HashMap<String, String>()
    var cookieBlacklist = ArrayList<String>()

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

        headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.67 Safari/537.36"
        headers["Accept-Encoding"] = "identity"
        headers["Accept"] = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
        headers["Origin"] = chamiloUrl
        headers["Accept-Language"] = "en-US,en;q=0.9,nl;q=0.8"
        headers["Cache-Control"] = "max-age=0"
        headers["Connection"] = "keep-alive"
        headers["Upgrade-Insecure-Requests"] = "1"

        for(cookie in cookies){
            if(!cookieBlacklist.contains(cookie.key.toLowerCase())){
                cookieString += cookie.key + "=" + cookie.value + "; "
            }
        }

        headers["Cookie"] = cookieString
        return headers
    }

    private fun initiateSession(cb: (token: Boolean) -> Unit) {
        if(cookies["SimpleSAMLSessionID"] != null)
            cb(true) // Session already initialized, return immediately
        else
        {
            // Simply GETting the chamilo homepage will give us a session id cookie
            sendRequest(Request.Method.GET, chamiloUrl + "/", constructHeaders(), null) {
                result ->
                cb(cookies["SimpleSAMLSessionID"] != null)
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

    fun isLoggedIn(): Boolean {
        return cookies["SimpleSAMLAuthToken"] != null
    }

    fun login(username: String, password: String, callback: (error: APIError?) -> Unit) {
        if(isLoggedIn()){
            callback(null)
            return
        }
        initiateSession { success ->
            if(success){
                val params = HashMap<String, String>()
                sendRequest(Request.Method.GET, chamiloUrl + "/index.php?application=Hogent%5CIntegration%5CSSO&go=adfs_login", constructHeaders(), params) {
                    result ->
                    val samlRequest: String = parseBodyPart(result.body, "name=\"SAMLRequest\" value=\"", "\"") ?: ""
                    val relayState: String = parseBodyPart(result.body, "name=\"RelayState\" value=\"", "\"") ?: ""

                    if(samlRequest.isNotEmpty() && relayState.isNotEmpty()){
                        params.clear()
                        params["SAMLRequest"] = samlRequest
                        params["RelayState"] = relayState
                        sendRequest(Request.Method.POST, idpHogentUrl + "/saml/idp/profile/redirectorpost/sso", constructHeaders(), params){
                            result ->
                                if(result.statusCode == 302){ // 302 -> Http redirect
                                    params.clear()

                                    // This is a useless request... But for some reason if we do don't make this request before proceeding, the server will return an internal error..
                                    // (Probably a bug with Chamilo?)
                                    sendRequest(Request.Method.GET, idpHogentUrl + "/my.policy", constructHeaders(), params) {}

                                    params["client_data"] = "SecurityDevice"
                                    params["post_url"] = idpHogentUrl + "/my.policy"
                                    sendRequest(Request.Method.POST, loginUrl + "/?action=", constructHeaders(), params) {
                                        result ->
                                            if(result.statusCode == 200){
                                                params.clear()
                                                params["username"] = username
                                                params["password"] = password
                                                sendRequest(Request.Method.POST, idpHogentUrl + "/my.policy", constructHeaders(), params) {
                                                    result ->
                                                    val dummy: String = parseBodyPart(result.body, "name=\"dummy\" value=\"", "\"") ?: ""
                                                    if(dummy.isNotEmpty()){
                                                        params.clear()
                                                        params["dummy"] = dummy
                                                        params["SAMLRequest"] = samlRequest
                                                        sendRequest(Request.Method.POST, idpHogentUrl + "/saml/idp/profile/redirectorpost/sso", constructHeaders(), params){
                                                            result ->
                                                            val samlResponse: String = parseBodyPart(result.body, "name=\"SAMLResponse\" value=\"", "\"") ?: ""
                                                            if(samlResponse.isNotEmpty()){
                                                                params.clear()
                                                                params["SAMLResponse"] = samlResponse
                                                                params["RelayState"] = relayState
                                                                sendRequest(Request.Method.POST, chamiloUrl + "/sso/module.php/saml/sp/saml2-acs.php/default-sp", constructHeaders(), params) {
                                                                    result ->
                                                                    if(isLoggedIn()) // SUCCESS
                                                                        callback(null)
                                                                    else
                                                                        callback(APIError(R.string.err_internal, R.string.err_internal_description))
                                                                }
                                                            }
                                                            else
                                                                callback(APIError(R.string.err_internal, R.string.err_internal_description))
                                                        }
                                                    }
                                                    else {
                                                        if(result.headers["Location"]?.contains("errorcode=21") ?: false) // errorcode for invalid username/password
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


}







