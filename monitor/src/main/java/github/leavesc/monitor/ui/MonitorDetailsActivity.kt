package github.leavesc.monitor.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import github.leavesc.monitor.R
import github.leavesc.monitor.db.HttpInformation
import github.leavesc.monitor.utils.FormatUtils
import github.leavesc.monitor.viewmodel.MonitorViewModel
import kotlinx.android.synthetic.main.activity_monitor_details.*
import java.util.*

/**
 * 作者：leavesC
 * 时间：2019/11/7 15:19
 * 描述：
 */
class MonitorDetailsActivity : AppCompatActivity() {

    companion object {

        private const val KEY_ID = "keyId"

        fun navTo(context: Context, id: Long) {
            val intent = Intent(context, MonitorDetailsActivity::class.java)
            intent.putExtra(KEY_ID, id)
            context.startActivity(intent)
        }
    }

    private val monitorViewModel by lazy {
        ViewModelProvider(this).get(MonitorViewModel::class.java).apply {
            recordLiveData?.observe(this@MonitorDetailsActivity, Observer { httpInformation ->
                this@MonitorDetailsActivity.httpInformation = httpInformation
                if (httpInformation != null) {
                    tvToolbarTitle.text = String.format("%s  %s", httpInformation.method, httpInformation.path)
                } else {
                    tvToolbarTitle.text = null
                }
            })
        }
    }

    private var httpInformation: HttpInformation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor_details)
        initView()
        val id = intent.getLongExtra(KEY_ID, 0)
        monitorViewModel.queryRecordById(id)
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val fragmentPagerAdapter = PagerAdapter(supportFragmentManager)
        fragmentPagerAdapter.addFragment(MonitorOverviewFragment.newInstance(), "overview")
        fragmentPagerAdapter.addFragment(MonitorPayloadFragment.newInstanceRequest(), "request")
        fragmentPagerAdapter.addFragment(MonitorPayloadFragment.newInstanceResponse(), "response")
        viewPager.adapter = fragmentPagerAdapter
        viewPager.offscreenPageLimit = 3
        tabs.setupWithViewPager(viewPager)
    }

    private class PagerAdapter constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val fragmentList = ArrayList<Fragment>()

        private val fragmentTitleList = ArrayList<String>()

        internal fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.share) {
            if (httpInformation != null) {
                share(FormatUtils.getShareText(httpInformation!!))
            }
        } else if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    private fun share(content: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, null))
    }

}