package com.medialert.medinotiapp.ui.activities.notebooks

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.adapters.NotebooksAdapter
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.models.Notebook
import com.medialert.medinotiapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NotebooksActivity : AppCompatActivity() {
    private lateinit var adapter: NotebooksAdapter
    private lateinit var database: MedinotiappDatabase
    private lateinit var sessionManager: SessionManager
    private var userId: Int = -1 // ID del usuario actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notebooks)
       // val emptyState = findViewById<TextView>(R.id.emptyState)
       // val recyclerView = findViewById<RecyclerView>(R.id.recyclerNotebooks)
        // Inicializar la base de datos
        database = MedinotiappDatabase.getDatabase(this)

        sessionManager = SessionManager(this);
        userId = sessionManager.getUserId()

        setupRecyclerView()
        setupFab()

        // Cargar las libretas del usuario
        loadNotebooks()
    }

    private fun setupRecyclerView() {
        adapter = NotebooksAdapter(
            onViewClick = { notebook -> viewNotebook(notebook) },
            onDeleteClick = { notebook -> deleteNotebook(notebook) },
            onAddNoteClick = { notebook -> addNoteToNotebook(notebook) }
        )
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerNotebooks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun addNoteToNotebook(notebook: Notebook) {
        val intent = Intent(this, AddNoteActivity::class.java)
        intent.putExtra("NOTEBOOK_ID", notebook.id) // Pasar el ID de la libreta
        startActivity(intent)
    }


    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddNotebook).setOnClickListener {
            showAddNotebookDialog()
        }
    }

    private fun loadNotebooks() {
        lifecycleScope.launch(Dispatchers.IO) {
            database.notebookDao().getNotebooksByUser(userId).collect { notebooks ->
                // Actualizar el adaptador en el hilo principal
                launch(Dispatchers.Main) {
                    adapter.submitList(notebooks)
                    val emptyState = findViewById<TextView>(R.id.emptyState)
                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerNotebooks)
                    if (notebooks.isEmpty()) {
                        emptyState.visibility = android.view.View.VISIBLE
                        recyclerView.visibility = android.view.View.GONE
                    } else {
                        emptyState.visibility = android.view.View.GONE
                        recyclerView.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }
    }

    private fun viewNotebook(notebook: Notebook) {
        val intent = Intent(this, NotebookDetailsActivity::class.java)
        intent.putExtra("NOTEBOOK_ID", notebook.id)
        startActivity(intent)
    }

    private fun deleteNotebook(notebook: Notebook) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Libreta")
            .setMessage("¿Deseas eliminar la libreta '${notebook.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    database.notebookDao().delete(notebook)

                    // Recargar la lista de libretas después de la eliminación
                    loadNotebooks()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun showAddNotebookDialog() {
        val input = EditText(this).apply {
            hint = "Nombre de la libreta"
        }

        AlertDialog.Builder(this)
            .setTitle("Añadir Libreta")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val notebook = Notebook(name = name, userId = userId)
                        database.notebookDao().insert(notebook)

                        // Recargar la lista de libretas después de agregar una nueva
                        loadNotebooks()
                    }
                } else {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}
