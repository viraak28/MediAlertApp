package com.medialert.medinotiapp.ui.activities.medications

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medialert.medinotiapp.adapters.MedicationAdapter
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityDailyMedicationsBinding
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class DailyMedicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyMedicationsBinding
    private lateinit var medicationDatabase: MedinotiappDatabase
    private lateinit var sessionManager: SessionManager
    private val medications = mutableListOf<Medication>()
    private lateinit var adapter: MedicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyMedicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedinotiappDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        setupRecyclerView()
        loadDailyMedications()
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter(
            medications,
            onTakeClick = null,  // Deshabilitar acción "Tomar"
            onEditClick = null,  // Deshabilitar edición
            onItemClick = null,  // Deshabilitar clic en ítems
            onDeleteClick = null, // Deshabilitar eliminación
            isDailyView = true
        )

        binding.recyclerViewDailyMedications.apply {
            layoutManager = LinearLayoutManager(this@DailyMedicationsActivity)
            adapter = this@DailyMedicationsActivity.adapter
        }
    }

    private fun loadDailyMedications() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                medicationDatabase.medicationDao().getMedicationsByUser(userId).collect { allMedications ->
                    val todayMedications = filterMedicationsForToday(allMedications)
                    withContext(Dispatchers.Main) {
                        medications.clear()
                        medications.addAll(todayMedications)
                        adapter.notifyDataSetChanged()

                        if (medications.isEmpty()) {
                            showEmptyState()
                        } else {
                            hideEmptyState()
                        }
                    }
                }
            }
        }
    }

    private fun filterMedicationsForToday(medications: List<Medication>): List<Medication> {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)

        return medications.filter { medication ->
            when (medication.frecuencyOfTakeMedicine) {
                "Diaria" -> true
                "Semanal" -> isTodayInSelectedDays(medication, today)
                else -> false
            }
        }
    }

    private fun isTodayInSelectedDays(medication: Medication, today: Int): Boolean {
        val todayName = when (today) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> ""
        }

        return medication.frecuencyOfTakeMedicineExactDay.contains(todayName, ignoreCase = true)
    }

    private fun showEmptyState() {
        binding.recyclerViewDailyMedications.visibility = android.view.View.GONE
        binding.emptyState.visibility = android.view.View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.recyclerViewDailyMedications.visibility = android.view.View.VISIBLE
        binding.emptyState.visibility = android.view.View.GONE
    }
}
