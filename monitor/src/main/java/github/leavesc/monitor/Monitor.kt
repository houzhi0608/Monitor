package github.leavesc.monitor

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import github.leavesc.monitor.db.HttpInformation
import github.leavesc.monitor.db.MonitorHttpInformationDatabase
import github.leavesc.monitor.holder.ContextHolder
import github.leavesc.monitor.holder.NotificationHolder
import github.leavesc.monitor.ui.MonitorActivity

/**
 * 作者：leavesC
 * 时间：2019/11/7 14:48
 * 描述：
 */
object Monitor {

    fun getLaunchIntent(context: Context): Intent {
        val intent = Intent(context, MonitorActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }

    fun clearCache() {
        MonitorHttpInformationDatabase.INSTANCE.httpInformationDao.deleteAll()
    }

    fun queryAllRecord(limit: Int): LiveData<List<HttpInformation>> {
        return MonitorHttpInformationDatabase.INSTANCE.httpInformationDao.queryAllRecordObservable(limit)
    }

    private fun clearNotification() {
        NotificationHolder.getInstance(ContextHolder.context).clearBuffer()
        NotificationHolder.getInstance(ContextHolder.context).dismiss()
    }

    private fun showNotification(showNotification: Boolean) {
        NotificationHolder.getInstance(ContextHolder.context).showNotification(showNotification)
    }

    private fun queryAllRecord(): LiveData<List<HttpInformation>> {
        return MonitorHttpInformationDatabase.INSTANCE.httpInformationDao.queryAllRecordObservable()
    }

}