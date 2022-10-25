package com.noob.apps.mvvmcountries.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.databinding.CourcesSectionItemBinding
import com.noob.apps.mvvmcountries.models.Course

class SectionAdapter(
    private val listener: NewRecyclerViewClickListener<Course>
) : RecyclerView.Adapter<SectionAdapter.ViewHolder>() {

    private var mList: Map<String, List<Course>>? = mapOf()

    fun setData(list: Map<String, List<Course>>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: CourcesSectionItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.cources_section_item,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mList?.entries?.forEachIndexed { index, entry ->
            if (index == position) {
                holder.bind(entry.key, entry.value)
                return
            }
        }
    }

    inner class ViewHolder(var itemBinding: CourcesSectionItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root),
        NewRecyclerViewClickListener<Course> {

        val adapter = CourseAdapter(this)

        fun bind(key: String, value: List<Course>) {
            itemBinding.subjectTv.text = key
            itemBinding.rvLectures.layoutManager = GridLayoutManager(itemView.context, 2)
            itemBinding.rvLectures.adapter = adapter
            adapter.setData(value)

        }

        override fun onItemClick(item: Course) {
            listener.onItemClick(item)
        }
    }

}
