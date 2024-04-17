package com.example.musicapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.OnRecentSearchItemClickListener
import com.example.musicapp.R
import com.example.musicapp.RecentSearchListener
import com.example.musicapp.model.RecentSearch

class RecentSearchAdapter(
    private val items: MutableList<RecentSearch>,
    private val itemClickListener: OnRecentSearchItemClickListener,
    private val recentSearchListener: RecentSearchListener?
) :
    RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.tvSearchContent)
        val closeBtn: TextView = itemView.findViewById(R.id.icClose)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentSearchAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_reecent_search, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecentSearchAdapter.ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.content.text = currentItem.content

        fun deleteItem(position: Int) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(currentItem.content)
        }

        holder.closeBtn.setOnClickListener {
            deleteItem(position)
            recentSearchListener?.saveRecentSearch(items)
        }

    }


    override fun getItemCount(): Int {
        return items.size
    }

}