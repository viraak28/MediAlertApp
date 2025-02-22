package com.medialert.medinotiapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.databinding.ItemTakeBinding
import com.medialert.medinotiapp.models.Take
import java.time.format.DateTimeFormatter

class TakeAdapter(private val takes: List<Take>) : RecyclerView.Adapter<TakeAdapter.TakeViewHolder>() {

    inner class TakeViewHolder(val binding: ItemTakeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(take: Take) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            binding.tvTakeTimestamp.text = take.timestamp.format(formatter)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TakeViewHolder {
        val binding = ItemTakeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TakeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TakeViewHolder, position: Int) {
        holder.bind(takes[position])
    }

    override fun getItemCount(): Int = takes.size
}
