package github.leavesc.monitor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import github.leavesc.monitor.R
import github.leavesc.monitor.utils.FormatUtils
import github.leavesc.monitor.viewmodel.MonitorViewModel
import kotlinx.android.synthetic.main.fragment_monitor_overview.*

/**
 * 作者：leavesC
 * 时间：2019/11/7 15:19
 * 描述：
 */
class MonitorOverviewFragment : Fragment() {

    companion object {

        fun newInstance(): MonitorOverviewFragment {
            return MonitorOverviewFragment()
        }
    }

    private val monitorViewModel by lazy {
        ViewModelProvider(activity!!).get(MonitorViewModel::class.java).apply {
            recordLiveData?.observe(this@MonitorOverviewFragment, Observer { monitorHttpInformation ->
                monitorHttpInformation?.apply {
                    tv_url.text = url
                    tv_method.text = method
                    tv_protocol.text = protocol
                    tv_status.text = status.toString()
                    tv_response.text = responseSummaryText
                    tv_ssl.text = if (isSsl) "Yes" else "No"
                    tv_request_time.text = FormatUtils.getDateFormatLong(requestDate)
                    tv_response_time.text = FormatUtils.getDateFormatLong(responseDate)
                    tv_duration.text = durationFormat
                    tv_request_size.text = FormatUtils.formatBytes(requestContentLength)
                    tv_response_size.text = FormatUtils.formatBytes(responseContentLength)
                    tv_total_size.text = totalSizeString
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_monitor_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        monitorViewModel.init()
    }

}