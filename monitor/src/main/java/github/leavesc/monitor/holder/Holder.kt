package github.leavesc.monitor.holder

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import github.leavesc.monitor.Monitor.getLaunchIntent
import github.leavesc.monitor.db.HttpInformation
import github.leavesc.monitor.service.ClearMonitorService
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.internal.bind.DateTypeAdapter
import github.leavesc.monitor.R
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

/**
 * 作者：leavesC
 * 时间：2019/11/7 14:44
 * 描述：
 */
@SuppressLint("StaticFieldLeak")
internal object ContextHolder {

    lateinit var context: Context

}

internal class NotificationHolder private constructor(context: Context) {

    companion object {

        private const val CHANNEL_ID = "monitorLeavesChannelId"

        private const val CHANNEL_NAME = "Http Notifications"

        private const val NOTIFICATION_TITLE = "Recording Http Activity"

        private const val NOTIFICATION_ID = 19950724

        private const val BUFFER_SIZE = 10

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: NotificationHolder? = null

        fun getInstance(context: Context): NotificationHolder {
            if (instance == null) {
                synchronized(NotificationHolder::class.java) {
                    if (instance == null) {
                        instance = NotificationHolder(context)
                    }
                }
            }
            return instance!!
        }
    }


    private val transactionBuffer = LongSparseArray<HttpInformation>()

    private val context = context.applicationContext

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var transactionCount: Int = 0

    @Volatile
    private var showNotification = true

    private val clearAction: NotificationCompat.Action
        get() {
            val intent = PendingIntent.getService(context, 200,
                    Intent(context, ClearMonitorService::class.java), PendingIntent.FLAG_ONE_SHOT)
            return NotificationCompat.Action(R.drawable.ic_launcher, "Clear", intent)
        }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN))
        }
    }

    @Synchronized
    fun show(transaction: HttpInformation) {
        if (showNotification) {
            addToBuffer(transaction)
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentIntent(getContentIntent(context))
                    .setLocalOnly(true)
                    .setOnlyAlertOnce(true)
                    .setSound(null)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setAutoCancel(false)
            val inboxStyle = NotificationCompat.InboxStyle()
            val size = transactionBuffer.size()
            if (size > 0) {
                builder.setContentText(transactionBuffer.valueAt(size - 1).notificationText)
                for (i in size - 1 downTo 0) {
                    inboxStyle.addLine(transactionBuffer.valueAt(i).notificationText)
                }
            }
            builder.setStyle(inboxStyle)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setSubText(transactionCount.toString())
            } else {
                builder.setNumber(transactionCount)
            }
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    @Synchronized
    private fun addToBuffer(httpInformation: HttpInformation) {
        transactionCount++
        transactionBuffer.put(httpInformation.id, httpInformation)
        if (transactionBuffer.size() > BUFFER_SIZE) {
            transactionBuffer.removeAt(0)
        }
    }

    @Synchronized
    fun showNotification(showNotification: Boolean) {
        this.showNotification = showNotification
    }

    @Synchronized
    fun clearBuffer() {
        transactionBuffer.clear()
        transactionCount = 0
    }

    @Synchronized
    fun dismiss() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun getContentIntent(context: Context): PendingIntent {
        return PendingIntent.getActivity(context, 100, getLaunchIntent(context), 0)
    }

}

internal object SerializableHolder {

    private var gson = GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date::class.java, DateTypeAdapter())
            .create()

    fun setPrettyPrinting(json: String): String {
        return try {
            gson.toJson(JsonParser.parseString(json))
        } catch (e: Exception) {
            json
        }
    }

    fun toJson(ob: Any): String {
        return gson.toJson(ob)
    }

    fun <T : Any> fromJson(json: String, t: Class<T>): T {
        return gson.fromJson(json, t)
    }

    fun <T> fromJson(json: String, t: Type): T {
        return gson.fromJson(json, t)
    }

    fun <T> fromJsonArray(json: String, clazz: Class<T>): List<T> {
        val type = ParameterizedTypeImpl(clazz)
        var ob: List<T>? = gson.fromJson<List<T>>(json, type)
        if (ob == null) {
            ob = ArrayList()
        }
        return ob
    }

    private class ParameterizedTypeImpl<T> constructor(val clazz: Class<T>) : ParameterizedType {

        override fun getActualTypeArguments(): Array<Type> {
            return arrayOf(clazz)
        }

        override fun getRawType(): Type {
            return List::class.java
        }

        override fun getOwnerType(): Type? {
            return null
        }
    }

}