package github.leavesc.monitor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import github.leavesc.monitor.R
import github.leavesc.monitor.db.HttpInformation
import github.leavesc.monitor.utils.FormatUtils

/**
 * 作者：leavesC
 * 时间：2019/11/7 14:46
 * 描述：
 */
internal class MonitorAdapter(context: Context) : RecyclerView.Adapter<MonitorViewHolder>() {

    private val colorSuccess = ContextCompat.getColor(context, R.color.monitor_status_success)
    private val colorRequested = ContextCompat.getColor(context, R.color.monitor_status_requested)
    private val colorError = ContextCompat.getColor(context, R.color.monitor_status_error)
    private val color300 = ContextCompat.getColor(context, R.color.monitor_status_300)
    private val color400 = ContextCompat.getColor(context, R.color.monitor_status_400)
    private val color500 = ContextCompat.getColor(context, R.color.monitor_status_500)

    private val asyncListDiffer = AsyncListDiffer(this, DiffUtilItemCallback())

    internal var clickListener: OnClickListener? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MonitorViewHolder {
        return MonitorViewHolder(viewGroup)
    }

    override fun onBindViewHolder(holder: MonitorViewHolder, position: Int) {
        val httpInformation = asyncListDiffer.currentList[position]
        holder.tv_id.text = httpInformation.id.toString()
        holder.tv_path.text = String.format("%s  %s", httpInformation.method, httpInformation.path)
        holder.tv_host.text = httpInformation.host
        holder.tv_requestDate.text = FormatUtils.getDateFormatShort(httpInformation.requestDate)
        holder.iv_ssl.visibility = if (httpInformation.isSsl) View.VISIBLE else View.GONE
        when (httpInformation.status) {
            HttpInformation.Status.Complete -> {
                holder.tv_code.text = httpInformation.responseCode.toString()
                holder.tv_duration.text = httpInformation.durationFormat
                holder.tv_size.text = httpInformation.totalSizeString
            }
            HttpInformation.Status.Failed -> {
                holder.tv_code.text = "!!!"
                holder.tv_duration.text = null
                holder.tv_size.text = null
            }
            else -> {
                holder.tv_code.text = "..."
                holder.tv_duration.text = null
                holder.tv_size.text = null
            }
        }
        setStatusColor(holder, httpInformation)
        holder.view.setOnClickListener {
            clickListener?.onClick(holder.adapterPosition, httpInformation)
        }
    }

    private fun setStatusColor(holder: MonitorViewHolder, httpInformation: HttpInformation) {
        val color: Int = when {
            httpInformation.status == HttpInformation.Status.Failed -> colorError
            httpInformation.status == HttpInformation.Status.Requested -> colorRequested
            httpInformation.responseCode >= 500 -> color500
            httpInformation.responseCode >= 400 -> color400
            httpInformation.responseCode >= 300 -> color300
            else -> colorSuccess
        }
        holder.tv_code.setTextColor(color)
        holder.tv_path.setTextColor(color)
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    fun setData(dataList: List<HttpInformation>) {
        asyncListDiffer.submitList(dataList)
    }

    fun clear() {
        asyncListDiffer.submitList(null)
    }

}

internal class MonitorViewHolder(viewGroup: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.item_monitor, viewGroup, false)) {

    val view: View = itemView
    val tv_id: TextView = view.findViewById(R.id.tv_id)
    val tv_code: TextView = view.findViewById(R.id.tv_code)
    val tv_path: TextView = view.findViewById(R.id.tv_path)
    val tv_host: TextView = view.findViewById(R.id.tv_host)
    val iv_ssl: ImageView = view.findViewById(R.id.iv_ssl)
    val tv_requestDate: TextView = view.findViewById(R.id.tv_requestDate)
    val tv_duration: TextView = view.findViewById(R.id.tv_duration)
    val tv_size: TextView = view.findViewById(R.id.tv_size)

}

private class DiffUtilItemCallback : DiffUtil.ItemCallback<HttpInformation>() {

    override fun areItemsTheSame(oldItem: HttpInformation, newItem: HttpInformation): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: HttpInformation, newItem: HttpInformation): Boolean {
        return oldItem == newItem
    }

}

internal interface OnClickListener {
    fun onClick(position: Int, model: HttpInformation)
}