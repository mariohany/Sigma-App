package com.noob.apps.mvvmcountries.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.databinding.LibraryCellBinding
import com.noob.apps.mvvmcountries.models.Library

class LibraryAdapter: RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    private var mList: List<Library>? = listOf()

    fun setData(list: List<Library>) {
        mList = list
        notifyItemRangeChanged(0, mList?.size ?: 0)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: LibraryCellBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.library_cell,
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

    inner class ViewHolder(private val itemBinding: LibraryCellBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(library: Library) {
            itemBinding.library = library
        }
    }

}
