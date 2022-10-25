package com.noob.apps.mvvmcountries.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.databinding.CountriesListItemBinding
import com.noob.apps.mvvmcountries.models.Country

class CountriesListAdapter : RecyclerView.Adapter<CountriesListAdapter.ViewHolder>() {

    private var mList: List<Country>? = listOf()

    fun setData(list: List<Country>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: CountriesListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.countries_list_item,
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

    class ViewHolder(var itemBinding: CountriesListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(country: Country) {
            itemBinding.country = country
        }
    }
}