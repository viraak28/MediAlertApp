package com.medialert.medinotiapp.ui.activities.notebooks

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.adapters.NotesAdapter
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotebookDetailsActivity : AppCompatActivity() {

    private lateinit var database: MedinotiappDatabase
    private lateinit var adapter: NotesAdapter
    private lateinit var currentWeek: Calendar
    private lateinit var tvWeekRange: TextView
    private lateinit var btnPreviousWeek: ImageButton
    private lateinit var btnNextWeek: ImageButton
    private var notebookId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notebook_details)

        database = MedinotiappDatabase.getDatabase(this)
        notebookId = intent.getLongExtra("NOTEBOOK_ID", -1)
        currentWeek = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
        }

        setupViews()
        setupRecyclerView()
        setupNavigationButtons()
        loadNotesForCurrentWeek()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        loadNotesForCurrentWeek()
    }

    private fun setupViews() {
        tvWeekRange = findViewById(R.id.txtWeekRange)
        btnPreviousWeek = findViewById(R.id.btnPreviousWeek)
        btnNextWeek = findViewById(R.id.btnNextWeek)
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter()
        val recyclerNotes = findViewById<RecyclerView>(R.id.recyclerNotes)
        recyclerNotes.layoutManager = LinearLayoutManager(this)
        recyclerNotes.adapter = adapter
    }

    private fun setupNavigationButtons() {
        btnPreviousWeek.setOnClickListener {
            navigateWeek(-1)
        }
        btnNextWeek.setOnClickListener {
            navigateWeek(1)
        }
    }

    private fun navigateWeek(weeks: Int) {
        currentWeek.add(Calendar.WEEK_OF_YEAR, weeks)
        loadNotesForCurrentWeek()
    }

    private fun loadNotesForCurrentWeek() {
        val (start, end) = getWeekRange()
        tvWeekRange.text = formatWeekRange(start, end)

        lifecycleScope.launch(Dispatchers.IO) {
            val notes = database.noteDao().getNotesByDateRange(
                notebookId = notebookId,
                start = start.timeInMillis,
                end = end.timeInMillis
            ).sortedBy { it.timestamp }

            runOnUiThread {
                adapter.submitList(notes)
                updateNavigationButtons(start)
            }
        }
    }

    private fun getWeekRange(): Pair<Calendar, Calendar> {
        val start = currentWeek.clone() as Calendar
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)

        val end = currentWeek.clone() as Calendar
        end.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)

        return Pair(start, end)
    }

    private fun formatWeekRange(start: Calendar, end: Calendar): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return "${formatter.format(start.time)} - ${formatter.format(end.time)}"
    }

    private fun updateNavigationButtons(start: Calendar) {
        lifecycleScope.launch(Dispatchers.IO) {
            val hasNotesBefore = database.noteDao().hasNotesBefore(notebookId, start.timeInMillis)

            runOnUiThread {
                btnPreviousWeek.visibility = if (hasNotesBefore) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddNote).setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            intent.putExtra("NOTEBOOK_ID", notebookId) // Pasar el ID de la libreta
            startActivity(intent)
        }
    }
}
