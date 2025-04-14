package com.medialert.medinotiapp.ui.activities.notebooks

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.models.Note
import com.medialert.medinotiapp.databinding.ActivityAddNoteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var database: MedinotiappDatabase
    private var notebookId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = MedinotiappDatabase.getDatabase(this)

        // Obtener el ID de la libreta desde el Intent
        notebookId = intent.getLongExtra("NOTEBOOK_ID", -1)
        if (notebookId == -1L) {
            Toast.makeText(this, "Error al obtener la libreta", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSaveNote.setOnClickListener {
            val noteContent = binding.etNoteContent.text.toString().trim()
            if (noteContent.isNotEmpty()) {
                saveNote(noteContent)
            } else {
                Toast.makeText(this, "La nota no puede estar vacía", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNote(content: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val note = Note(
                notebookId = notebookId,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            database.noteDao().insert(note)
            runOnUiThread {
                Toast.makeText(this@AddNoteActivity, "Nota añadida correctamente", Toast.LENGTH_SHORT).show()
                finish() // Finalizar la actividad y volver a NotebookDetailsActivity
            }
        }
    }
}
