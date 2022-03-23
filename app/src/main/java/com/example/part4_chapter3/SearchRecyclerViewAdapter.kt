package com.example.part4_chapter3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.part4_chapter3.databinding.ItemSearchResultBinding
import com.example.part4_chapter3.model.SearchResultEntity

class SearchRecyclerViewAdapter(val searchResultClickListener: (SearchResultEntity) -> Unit) :
    ListAdapter<SearchResultEntity, SearchRecyclerViewAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        val binding: ItemSearchResultBinding
        ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item:SearchResultEntity) = with(binding) {
            titleTextView.text = item.fullAdress
            subTitleTextView.text = item.name
            itemView.setOnClickListener {
                searchResultClickListener(item)
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSearchResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<SearchResultEntity>() {
            override fun areItemsTheSame(oldItem:SearchResultEntity, newItem:SearchResultEntity): Boolean {
                return oldItem.locationLatLng == newItem.locationLatLng
            }

            override fun areContentsTheSame(oldItem:SearchResultEntity, newItem:SearchResultEntity): Boolean {
                return oldItem == newItem
            }

        }
    }
}