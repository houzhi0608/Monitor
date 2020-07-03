package github.leavesc.monitor.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import github.leavesc.monitor.holder.SerializableHolder
import github.leavesc.monitor.utils.FormatUtils
import okhttp3.Headers
import java.util.*

/**
 * 作者：leavesC
 * 时间：2019/11/7 14:38
 * 描述：
 */
data class HttpHeader(val name: String, val value: String)

@Entity(tableName = "monitor_httpInformation")
class HttpInformation {

    companion object {

        private const val DEFAULT_RESPONSE_CODE = -100
    }

    enum class Status {
        Requested, Complete, Failed
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    lateinit var requestDate: Date
    var responseDate: Date? = null
    var duration: Long = 0
    var method = ""
    var url = ""
    var host: String? = ""
    var path = ""
    var scheme: String? = ""
    var protocol = ""

    var requestHeaders = ""
    var requestBody = ""
    var requestContentType = ""
    var requestContentLength: Long = 0
    var isRequestBodyIsPlainText = true

    var responseCode = DEFAULT_RESPONSE_CODE
    var responseHeaders: String? = null
    var responseBody: String? = null
    var responseMessage: String? = null
    var responseContentType: String? = null
    var responseContentLength: Long = 0
    var isResponseBodyIsPlainText = true

    var error: String? = null

    val status: Status
        get() = when {
            error != null -> Status.Failed
            responseCode == DEFAULT_RESPONSE_CODE -> Status.Requested
            else -> Status.Complete
        }

    val notificationText: String
        get() {
            return when (status) {
                Status.Failed -> " ! ! !  $path"
                Status.Requested -> " . . .  $path"
                else -> "$responseCode $path"
            }
        }

    val responseSummaryText: String
        get() {
            return when (status) {
                Status.Failed -> error ?: ""
                Status.Requested -> ""
                else -> "$responseCode $responseMessage"
            }
        }

    private val requestHeaderList: List<HttpHeader>
        get() = SerializableHolder.fromJsonArray(requestHeaders, HttpHeader::class.java)

    val formattedRequestBody: String
        get() = FormatUtils.formatBody(requestBody, requestContentType)

    val formattedResponseBody: String
        get() {
            val body = responseBody
            return if (body == null) {
                ""
            } else {
                FormatUtils.formatBody(body, responseContentType)
            }
        }

    private val responseHeaderList: List<HttpHeader>
        get() {
            val headers = responseHeaders ?: return arrayListOf()
            return SerializableHolder.fromJsonArray(headers, HttpHeader::class.java)
        }

    val durationFormat: String
        get() = "$duration ms"

    val isSsl: Boolean
        get() = "https".equals(scheme, ignoreCase = true)

    val totalSizeString: String
        get() = FormatUtils.formatBytes(requestContentLength + responseContentLength)

    fun setRequestHttpHeaders(headers: Headers?) {
        requestHeaders = if (headers != null) {
            val httpHeaders = ArrayList<HttpHeader>()
            var i = 0
            val count = headers.size
            while (i < count) {
                httpHeaders.add(HttpHeader(headers.name(i), headers.value(i)))
                i++
            }
            SerializableHolder.toJson(httpHeaders)
        } else {
            ""
        }
    }

    fun setResponseHttpHeaders(headers: Headers?) {
        responseHeaders = if (headers != null) {
            val httpHeaders = ArrayList<HttpHeader>()
            var i = 0
            val count = headers.size
            while (i < count) {
                httpHeaders.add(HttpHeader(headers.name(i), headers.value(i)))
                i++
            }
            SerializableHolder.toJson(httpHeaders)
        } else {
            ""
        }
    }

    fun getRequestHeadersString(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(requestHeaderList, withMarkup)
    }

    fun getResponseHeadersString(withMarkup: Boolean): String {
        return FormatUtils.formatHeaders(responseHeaderList, withMarkup)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HttpInformation
        if (id != other.id) return false
        if (requestDate != other.requestDate) return false
        if (responseDate != other.responseDate) return false
        if (duration != other.duration) return false
        if (method != other.method) return false
        if (url != other.url) return false
        if (host != other.host) return false
        if (path != other.path) return false
        if (scheme != other.scheme) return false
        if (protocol != other.protocol) return false
        if (requestHeaders != other.requestHeaders) return false
        if (requestBody != other.requestBody) return false
        if (requestContentType != other.requestContentType) return false
        if (requestContentLength != other.requestContentLength) return false
        if (isRequestBodyIsPlainText != other.isRequestBodyIsPlainText) return false
        if (responseCode != other.responseCode) return false
        if (responseHeaders != other.responseHeaders) return false
        if (responseBody != other.responseBody) return false
        if (responseMessage != other.responseMessage) return false
        if (responseContentType != other.responseContentType) return false
        if (responseContentLength != other.responseContentLength) return false
        if (isResponseBodyIsPlainText != other.isResponseBodyIsPlainText) return false
        if (error != other.error) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + requestDate.hashCode()
        result = 31 * result + (responseDate?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (host?.hashCode() ?: 0)
        result = 31 * result + path.hashCode()
        result = 31 * result + (scheme?.hashCode() ?: 0)
        result = 31 * result + protocol.hashCode()
        result = 31 * result + requestHeaders.hashCode()
        result = 31 * result + requestBody.hashCode()
        result = 31 * result + requestContentType.hashCode()
        result = 31 * result + requestContentLength.hashCode()
        result = 31 * result + isRequestBodyIsPlainText.hashCode()
        result = 31 * result + responseCode
        result = 31 * result + (responseHeaders?.hashCode() ?: 0)
        result = 31 * result + (responseBody?.hashCode() ?: 0)
        result = 31 * result + (responseMessage?.hashCode() ?: 0)
        result = 31 * result + (responseContentType?.hashCode() ?: 0)
        result = 31 * result + responseContentLength.hashCode()
        result = 31 * result + isResponseBodyIsPlainText.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "HttpInformation(id=$id, requestDate=$requestDate, responseDate=$responseDate, duration=$duration, method='$method', url='$url', host=$host, path='$path', scheme=$scheme, protocol='$protocol', requestHeaders='$requestHeaders', requestBody='$requestBody', requestContentType='$requestContentType', requestContentLength=$requestContentLength, isRequestBodyIsPlainText=$isRequestBodyIsPlainText, responseCode=$responseCode, responseHeaders=$responseHeaders, responseBody=$responseBody, responseMessage=$responseMessage, responseContentType=$responseContentType, responseContentLength=$responseContentLength, isResponseBodyIsPlainText=$isResponseBodyIsPlainText, error=$error)"
    }

}