package com.noob.apps.mvvmcountries.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.databinding.NotificationCellBinding
import com.noob.apps.mvvmcountries.models.Notification

class NotificationAdapter: RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var mList: List<Notification>? = listOf()

    fun setData(list: List<Notification>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: NotificationCellBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.notification_cell,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mList?.get(position)?.let {
            holder.bind(it)
        }
    }

    class ViewHolder(var itemBinding: NotificationCellBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(notification: Notification) {
            itemBinding.notification = notification
        }

    }

}