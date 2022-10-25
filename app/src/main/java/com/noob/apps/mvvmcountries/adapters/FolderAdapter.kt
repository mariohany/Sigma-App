package com.noob.apps.mvvmcountries.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.databinding.FolderCellBinding
import com.noob.apps.mvvmcountries.models.Attachments

class FolderAdapter(
    private val listener: RecyclerViewClickListener

) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    private var mList: List<Attachments>? = listOf()

    fun setData(list: List<Attachments>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: FolderCellBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.folder_cell,
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
            holder.bind(it, position)
        }
    }

    inner class ViewHolder(var itemBinding: FolderCellBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(attachments: Attachments, position: Int) {
            itemBinding.notification = attachments
            itemBinding.container.setOnClickListener {
                listener.openFile(
                    attachments.url
                )
            }
        }

    }
}