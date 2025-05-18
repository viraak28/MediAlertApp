package com.medialert.medinotiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.models.Notebook

class NotebooksAdapter(
    private val onViewClick: (Notebook) -> Unit,
    private val onDeleteClick: (Notebook) -> Unit,
    private val onAddNoteClick: (Notebook) -> Unit // Nuevo callback para añadir nota
) : ListAdapter<Notebook, NotebooksAdapter.NotebookViewHolder>(DiffCallback()) {

    inner class NotebookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view.findViewById(R.id.tvNotebookName)
        private val btnViewNotebook: ImageButton = view.findViewById(R.id.btnViewNotebook)
        private val btnDeleteNotebook: Button = view.findViewById(R.id.btnDeleteNotebook)
        private val btnAddNote: Button = view.findViewById(R.id.btnAddNote) // Nuevo botón

        fun bind(notebook: Notebook) {
            titleTextView.text = notebook.name
            btnViewNotebook.setOnClickListener { onViewClick(notebook) }
            btnDeleteNotebook.setOnClickListener { onDeleteClick(notebook) }
            btnAddNote.setOnClickListener { onAddNoteClick(notebook) } // Manejar clic en añadir nota
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotebookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notebook, parent, false)
        return NotebookViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotebookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Notebook>() {
        override fun areItemsTheSame(oldItem: Notebook, newItem: Notebook): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notebook, newItem: Notebook): Boolean {
            return oldItem == newItem
        }
    }
}
