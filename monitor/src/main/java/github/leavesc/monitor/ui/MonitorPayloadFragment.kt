package github.leavesc.monitor.ui

import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import github.leavesc.monitor.R
import github.leavesc.monitor.viewmodel.MonitorViewModel
import kotlinx.android.synthetic.main.fragment_monitor_payload.*

/**
 * 作者：leavesC
 * 时间：2019/11/7 15:19
 * 描述：
 */
class MonitorPayloadFragment : Fragment() {

    companion object {

        private const val TYPE_REQUEST = 100

        private const val TYPE_RESPONSE = 200

        private const val TYPE_KEY = "keyType"

        private fun newInstance(type: Int): MonitorPayloadFragment {
            val fragment = MonitorPayloadFragment()
            val bundle = Bundle()
            bundle.putInt(TYPE_KEY, type)
            fragment.arguments = bundle
            return fragment
        }

        fun newInstanceRequest(): MonitorPayloadFragment {
            return newInstance(TYPE_REQUEST)
        }

        fun newInstanceResponse(): MonitorPayloadFragment {
            return newInstance(TYPE_RESPONSE)
        }
    }

    private val monitorViewModel by lazy {
        ViewModelProvider(activity!!).get(MonitorViewModel::class.java).apply {
            recordLiveData?.observe(this@MonitorPayloadFragment, Observer { monitorHttpInformation ->
                if (monitorHttpInformation != null) {
                    when (type) {
                        TYPE_REQUEST -> {
                            setText(monitorHttpInformation.getRequestHeadersString(true),
                                    monitorHttpInformation.formattedRequestBody, monitorHttpInformation.isRequestBodyIsPlainText)
                        }
                        TYPE_RESPONSE -> {
                            setText(monitorHttpInformation.getResponseHeadersString(true),
                                    monitorHttpInformation.formattedResponseBody, monitorHttpInformation.isResponseBodyIsPlainText)
                        }
                    }
                }
            })
        }
    }

    private var type: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            type = bundle.getInt(TYPE_KEY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_monitor_payload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        monitorViewModel.init()
    }

    private fun setText(headersString: String, bodyString: String, isPlainText: Boolean) {
        if (TextUtils.isEmpty(headersString)) {
            tvHeaders.visibility = View.GONE
        } else {
            tvHeaders.visibility = View.VISIBLE
            tvHeaders.text = Html.fromHtml(headersString)
        }
        if (!isPlainText) {
            tvBody.text = "(encoded or binary body omitted)"
        } else {
            tvBody.text = bodyString
        }
    }

}