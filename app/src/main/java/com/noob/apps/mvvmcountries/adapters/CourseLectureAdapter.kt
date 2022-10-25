package com.noob.apps.mvvmcountries.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.databinding.LecturesCourseCellBinding
import com.noob.apps.mvvmcountries.models.LectureDetails
import com.noob.apps.mvvmcountries.models.User

class CourseLectureAdapter(
    user: User,
    context: Context,
    private val listener: RecyclerViewClickListener
) : RecyclerView.Adapter<CourseLectureAdapter.ViewHolder>() {
    private var mList: List<LectureDetails>? = listOf()
    private var lastPosition = -1
    private var mContext: Context = context
    private var mUser: User = user

    fun setData(list: List<LectureDetails>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: LecturesCourseCellBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.lectures_course_cell,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        mList?.get(position)?.let {
            holder.bind(it, position)
        }
    }

    inner class ViewHolder(var itemBinding: LecturesCourseCellBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(it: LectureDetails, position: Int) {
            itemBinding.lecture = it
            val number = position + 1
            itemBinding.container.setOnClickListener {
                lastPosition = position
                notifyDataSetChanged()
                listener.onRecyclerViewItemClick(
                    position
                )
            }

            if (!it.description.isNullOrEmpty()){
                itemBinding.descriptionTv.isVisible = true
                itemBinding.descriptionTv.text = it.description
            }else{
                itemBinding.descriptionTv.isVisible = false
            }

            if (position == mList?.size?.minus(1))
                itemBinding.sep.visibility = View.INVISIBLE

            val isViewed = (it.views_online.toIntOrNull()?:0) > 0
            itemBinding.viewedIv.isVisible = isViewed
            if (position == lastPosition) {
                itemBinding.container.setBackgroundColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.cell_bg_color
                    )
                )

                itemBinding.number.typeface = (ResourcesCompat.getFont(mContext, R.font.bold))
                itemBinding.name.typeface = (ResourcesCompat.getFont(mContext, R.font.bold))
                itemBinding.play.visibility = View.VISIBLE


            } else {
                itemBinding.play.visibility = View.INVISIBLE
                itemBinding.number.typeface = (ResourcesCompat.getFont(mContext, R.font.regular))
                itemBinding.name.typeface = (ResourcesCompat.getFont(mContext, R.font.regular))
                itemBinding.container.setBackgroundColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.white
                    )
                )

            }
            //   holder.itemBinding.number.text = number.toString() + "_"
            //  holder.itemBinding.number.visibility = View.INVISIBLE
            try {
                val duration = it.duration
                val minutes: Long = (duration.toLong() / 60)
                itemBinding.duration.text = minutes.toString() + " " + mContext.resources.getString(R.string.mintes)
            } catch (e: Exception) {
                e.message
            }
        }

    }
}