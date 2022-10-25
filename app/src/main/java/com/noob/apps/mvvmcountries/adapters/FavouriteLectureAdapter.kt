package com.noob.apps.mvvmcountries.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.databinding.FavouriteLectureCellBinding
import com.noob.apps.mvvmcountries.models.Lecture

class FavouriteLectureAdapter : RecyclerView.Adapter<FavouriteLectureAdapter.ViewHolder>() {

    private var mList: List<Lecture>? = listOf()

    fun setData(list: List<Lecture>) {
        mList = list
        notifyItemRangeChanged(0, mList?.size ?: 0)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: FavouriteLectureCellBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.favourite_lecture_cell,
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

    class ViewHolder(var itemBinding: FavouriteLectureCellBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(lecture: Lecture, position: Int) {
            itemBinding.lecture = lecture
        }
    }

}