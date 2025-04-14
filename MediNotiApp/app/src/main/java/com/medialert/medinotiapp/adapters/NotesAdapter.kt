package com.medialert.medinotiapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.models.Note
import java.text.SimpleDateFormat
import java.util.Locale

class NotesAdapter : ListAdapter<Note, NotesAdapter.NoteViewHolder>(DiffCallback()) {

    private val timestampFormat = SimpleDateFormat("EEE dd/MM/yyyy HH:mm", Locale("es", "ES"))

    inner class NoteViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        private val tvContent: TextView = view.findViewById(R.id.tvContent)

        fun bind(note: Note) {
            tvTimestamp.text = timestampFormat.format(note.timestamp)
            tvContent.text = note.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }
}
