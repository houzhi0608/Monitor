package github.leavesc.monitor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

import github.leavesc.monitor.db.HttpInformation
import github.leavesc.monitor.db.MonitorHttpInformationDatabase
import github.leavesc.monitor.holder.ContextHolder
import github.leavesc.monitor.holder.NotificationHolder

/**
 * 作者：leavesC
 * 时间：2019/11/7 14:47
 * 描述：
 */
internal class MonitorViewModel : ViewModel() {

    companion object {

        private const val LIMIT = 300
    }


    val allRecordLiveData: LiveData<List<HttpInformation>>

    var recordLiveData: LiveData<HttpInformation>? = null
        private set

    init {
        allRecordLiveData = MonitorHttpInformationDatabase.INSTANCE.httpInformationDao.queryAllRecordObservable(LIMIT)
    }

    fun clearAllCache() {
        Thread(Runnable { MonitorHttpInformationDatabase.INSTANCE.httpInformationDao.deleteAll() }).start()
    }

    fun clearNotification() {
        NotificationHolder.getInstance(ContextHolder.context).dismiss()
    }

    fun queryRecordById(id: Long) {
        recordLiveData = MonitorHttpInformationDatabase.INSTANCE.httpInformationDao.queryRecordObservable(id)
    }

    fun init() {

    }

}