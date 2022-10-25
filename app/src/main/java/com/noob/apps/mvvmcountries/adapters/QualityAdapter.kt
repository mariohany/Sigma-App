package com.noob.apps.mvvmcountries.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.databinding.QualityItemCellBinding
import com.noob.apps.mvvmcountries.models.Files
import com.noob.apps.mvvmcountries.ui.details.CourseDetailsActivity

class QualityAdapter(
    context: Context,
    private val listener: RecyclerViewClickListener
) : RecyclerView.Adapter<QualityAdapter.ViewHolder>() {
    private var selectedPosition = 0
    private var mList: List<Files>? = listOf()
    private var mContext: Context = context
    fun setData(list: List<Files>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: QualityItemCellBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.quality_item_cell,
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

    inner class ViewHolder(private var itemBinding: QualityItemCellBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(it: Files, position: Int) {
            itemBinding.lecture = it
            itemBinding.container.setOnClickListener {
                CourseDetailsActivity.lastQualityPosition = position
                listener.onQualitySelected(
                    CourseDetailsActivity.lastQualityPosition
                )
            }
            if (CourseDetailsActivity.lastQualityPosition == position) {
                itemBinding.checkImage.background =
                    AppCompatResources.getDrawable(mContext, R.drawable.ic_blue_circle)
            }
            else{
                itemBinding.checkImage.background =
                    AppCompatResources.getDrawable(mContext, R.drawable.ic_gray_circle)
            }
        }

    }
}