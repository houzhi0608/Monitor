package github.leavesc.monitor.service

import android.app.IntentService
import android.content.Intent
import github.leavesc.monitor.holder.NotificationHolder

/**
 * 作者：leavesC
 * 时间：2019/11/7 14:45
 * 描述：
 */
internal class ClearMonitorService : IntentService(ClearMonitorService::class.java.name) {

    override fun onHandleIntent(intent: Intent?) {
        val holder = NotificationHolder.getInstance(this)
        holder.clearBuffer()
        holder.dismiss()
    }

}