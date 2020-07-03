package github.leavesc.monitor.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import github.leavesc.monitor.R
import github.leavesc.monitor.adapter.MonitorAdapter
import github.leavesc.monitor.adapter.OnClickListener
import github.leavesc.monitor.db.HttpInformation
import github.leavesc.monitor.viewmodel.MonitorViewModel
import kotlinx.android.synthetic.main.activity_monitor.*

/**
 * 作者：leavesC
 * 时间：2019/11/7 15:20
 * 描述：
 */
class MonitorActivity : AppCompatActivity() {

    private val monitorViewModel by lazy {
        ViewModelProvider(this).get(MonitorViewModel::class.java).apply {
            allRecordLiveData.observe(this@MonitorActivity, Observer {
                monitorAdapter.setData(it)
            })
        }
    }

    private val monitorAdapter by lazy {
        MonitorAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        initView()
        monitorViewModel.init()
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tvToolbarTitle.text = getString(R.string.app_name)
        monitorAdapter.clickListener = object : OnClickListener {
            override fun onClick(position: Int, model: HttpInformation) {
                MonitorDetailsActivity.navTo(this@MonitorActivity, model.id)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = monitorAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_monitor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear) {
            monitorViewModel.clearAllCache()
            monitorViewModel.clearNotification()
        } else if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

}