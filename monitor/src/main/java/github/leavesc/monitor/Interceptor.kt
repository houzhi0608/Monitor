package github.leavesc.monitor

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import github.leavesc.monitor.db.HttpInformation
import github.leavesc.monitor.db.MonitorHttpInformationDatabase
import github.leavesc.monitor.holder.ContextHolder
import github.leavesc.monitor.holder.NotificationHolder
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import okio.BufferedSource
import okio.GzipSource
import okio.buffer
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * 作者：leavesC
 * 时间：2019/11/7 14:48
 * 描述：
 */
class MonitorInterceptor(context: Context) : Interceptor {

    companion object {

        private const val TAG = "MonitorInterceptor"

        private val CHARSET_UTF8 = Charset.forName("UTF-8")
    }

    private var maxContentLength = 250000L

    init {
        ContextHolder.context = context.applicationContext
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body
        val httpInformation = HttpInformation()
        httpInformation.requestDate = Date()
        httpInformation.setRequestHttpHeaders(request.headers)
        httpInformation.method = request.method
        val url = request.url.toString()
        httpInformation.url = url
        if (!TextUtils.isEmpty(url)) {
            val uri = Uri.parse(url)
            httpInformation.host = uri.host
            httpInformation.path = uri.path!! + if (uri.query != null) "?" + uri.query!! else ""
            httpInformation.scheme = uri.scheme
        }
        if (requestBody != null) {
            val contentType = requestBody.contentType()
            if (contentType != null) {
                httpInformation.requestContentType = contentType.toString()
            }
            if (requestBody.contentLength() != -1L) {
                httpInformation.requestContentLength = requestBody.contentLength()
            }
        }
        httpInformation.isRequestBodyIsPlainText = !bodyHasUnsupportedEncoding(request.headers)
        if (requestBody != null && httpInformation.isRequestBodyIsPlainText) {
            val source = getNativeSource(Buffer(), bodyGzipped(request.headers))
            val buffer = source.buffer
            requestBody.writeTo(buffer)
            val charset: Charset?
            val contentType = requestBody.contentType()
            charset = if (contentType != null) {
                contentType.charset(CHARSET_UTF8)
            } else {
                CHARSET_UTF8
            }
            if (isPlaintext(buffer)) {
                httpInformation.requestBody = readFromBuffer(buffer, charset)
            } else {
                httpInformation.isResponseBodyIsPlainText = false
            }
        }
        val id = insert(httpInformation)
        httpInformation.id = id
        val startTime = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            httpInformation.error = e.toString()
            update(httpInformation)
            throw e
        }

        httpInformation.responseDate = Date()
        httpInformation.duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        httpInformation.setRequestHttpHeaders(response.request.headers)
        httpInformation.protocol = response.protocol.toString()
        httpInformation.responseCode = response.code
        httpInformation.responseMessage = response.message
        val responseBody = response.body
        if (responseBody != null) {
            httpInformation.responseContentLength = responseBody.contentLength()
            val contentType = responseBody.contentType()
            if (contentType != null) {
                httpInformation.responseContentType = contentType.toString()
            }
        }
        httpInformation.setResponseHttpHeaders(response.headers)
        httpInformation.isResponseBodyIsPlainText = !bodyHasUnsupportedEncoding(response.headers)
        if (response.promisesBody() && httpInformation.isResponseBodyIsPlainText) {
            val source = getNativeSource(response)
            source.request(java.lang.Long.MAX_VALUE)
            val buffer = source.buffer
            var charset: Charset? = CHARSET_UTF8
            if (responseBody != null) {
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    try {
                        charset = contentType.charset(CHARSET_UTF8)
                    } catch (e: UnsupportedCharsetException) {
                        update(httpInformation)
                        return response
                    }

                }
            }
            if (isPlaintext(buffer)) {
                httpInformation.responseBody = readFromBuffer(buffer.clone(), charset)
            } else {
                httpInformation.isResponseBodyIsPlainText = false
            }
            httpInformation.responseContentLength = buffer.size
        }
        update(httpInformation)
        return response
    }

    private fun insert(httpInformation: HttpInformation): Long {
        showNotification(httpInformation)
        return MonitorHttpInformationDatabase.INSTANCE.httpInformationDao.insert(httpInformation)
    }

    private fun update(httpInformation: HttpInformation) {
        showNotification(httpInformation)
        MonitorHttpInformationDatabase.INSTANCE.httpInformationDao.update(httpInformation)
    }

    private fun showNotification(httpInformation: HttpInformation) {
        NotificationHolder.getInstance(ContextHolder.context).show(httpInformation)
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false
        }

    }

    private fun bodyHasUnsupportedEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null &&
                !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }

    private fun readFromBuffer(buffer: Buffer, charset: Charset?): String {
        val bufferSize = buffer.size
        val maxBytes = min(bufferSize, maxContentLength)
        var body: String
        body = try {
            buffer.readString(maxBytes, charset!!)
        } catch (e: EOFException) {
            "\\n\\n--- Unexpected end of content ---"
        }
        if (bufferSize > maxContentLength) {
            body += "\\n\\n--- Content truncated ---"
        }
        return body
    }

    @Throws(IOException::class)
    private fun getNativeSource(response: Response): BufferedSource {
        if (bodyGzipped(response.headers)) {
            val source = response.peekBody(maxContentLength).source()
            if (source.buffer.size < maxContentLength) {
                return getNativeSource(source, true)
            } else {
                Log.e(TAG, "gzip encoded response was too long")
            }
        }
        return response.body!!.source()
    }

    private fun getNativeSource(input: BufferedSource, isGzipped: Boolean): BufferedSource {
        return if (isGzipped) {
            val source = GzipSource(input)
            source.buffer()
        } else {
            input
        }
    }

    private fun bodyGzipped(headers: Headers): Boolean {
        return "gzip".equals(headers["Content-Encoding"], ignoreCase = true)
    }

    fun maxContentLength(max: Long): MonitorInterceptor {
        this.maxContentLength = max
        return this
    }

}