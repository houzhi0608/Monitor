package github.leavesc.monitor.utils

import android.text.TextUtils
import github.leavesc.monitor.db.HttpHeader
import github.leavesc.monitor.db.HttpInformation
import github.leavesc.monitor.holder.SerializableHolder
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult

/**
 * 作者：leavesC
 * 时间：2019/11/7 14:57
 * 描述：
 */
internal object FormatUtils {

    private val TIME_SHORT = SimpleDateFormat("HH:mm:ss SSS", Locale.CHINA)

    private val TIME_LONG = SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA)

    private fun formatData(date: Date?, format: SimpleDateFormat): String {
        return if (date == null) {
            ""
        } else format.format(date)
    }

    fun getDateFormatShort(date: Date): String {
        return formatData(date, TIME_SHORT)
    }

    fun getDateFormatLong(date: Date?): String {
        return formatData(date, TIME_LONG)
    }

    fun formatBytes(bytes: Long): String {
        return formatByteCount(bytes, true)
    }

    private fun formatByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    fun formatHeaders(httpHeaders: List<HttpHeader>?, withMarkup: Boolean): String {
        val out = StringBuilder()
        if (httpHeaders != null) {
            for ((name, value) in httpHeaders) {
                out.append(if (withMarkup) "<b>" else "")
                        .append(name)
                        .append(": ")
                        .append(if (withMarkup) "</b>" else "")
                        .append(value)
                        .append(if (withMarkup) "<br />" else "\n")
            }
        }
        return out.toString()
    }

    fun formatBody(body: String, contentType: String?): String {
        return when {
            contentType?.contains("json", true) == true -> SerializableHolder.setPrettyPrinting(body)
            contentType?.contains("xml", true) == true -> formatXml(body)
            else -> body
        }
    }

    private fun formatXml(xml: String): String {
        return try {
            val serializer = SAXTransformerFactory.newInstance().newTransformer()
            serializer.setOutputProperty(OutputKeys.INDENT, "yes")
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            val xmlSource = SAXSource(InputSource(ByteArrayInputStream(xml.toByteArray())))
            val res = StreamResult(ByteArrayOutputStream())
            serializer.transform(xmlSource, res)
            String((res.outputStream as ByteArrayOutputStream).toByteArray())
        } catch (e: Exception) {
            xml
        }

    }

    fun getShareText(httpInformation: HttpInformation): String {
        var text = ""
        text += "Url" + ": " + check(httpInformation.url) + "\n"
        text += "Method" + ": " + check(httpInformation.method) + "\n"
        text += "Protocol" + ": " + check(httpInformation.protocol) + "\n"
        text += "Status" + ": " + check(httpInformation.status.toString()) + "\n"
        text += "Response" + ": " + check(httpInformation.responseSummaryText) + "\n"
        text += "SSL" + ": " + httpInformation.isSsl + "\n"
        text += "\n"
        text += "Request Time" + ": " + getDateFormatLong(httpInformation.requestDate) + "\n"
        text += "Response Time" + ": " + getDateFormatLong(httpInformation.responseDate) + "\n"
        text += "Duration" + ": " + check(httpInformation.durationFormat) + "\n"
        text += "\n"
        text += "Request Size" + ": " + formatBytes(httpInformation.requestContentLength) + "\n"
        text += "Response Size" + ": " + formatBytes(httpInformation.responseContentLength) + "\n"
        text += "Total Size" + ": " + check(httpInformation.totalSizeString) + "\n"
        text += "\n"
        text += "---------- " + "Request" + " ----------\n\n"
        var headers = httpInformation.getRequestHeadersString(false)
        if (!TextUtils.isEmpty(headers)) {
            text += headers + "\n"
        }
        text += if (httpInformation.isRequestBodyIsPlainText)
            check(httpInformation.formattedRequestBody)
        else
            "(encoded or binary body omitted)"
        text += "\n"
        text += "---------- " + "Response" + " ----------\n\n"
        headers = httpInformation.getResponseHeadersString(false)
        if (!TextUtils.isEmpty(headers)) {
            text += headers + "\n"
        }
        text += if (httpInformation.isResponseBodyIsPlainText)
            check(httpInformation.formattedResponseBody)
        else
            "(encoded or binary body omitted)"
        return text
    }

    private fun check(string: String?): String {
        return string ?: ""
    }

}