package com.medialert.medinotiapp.ui.activities.medications

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.adapters.DailyAdapter
import com.medialert.medinotiapp.adapters.MedicationAdapter
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityDailyMedicationsBinding
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.models.Take
import com.medialert.medinotiapp.utils.DailyItem
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
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        setupRecyclerView()
        loadDailyMedications()
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter(
            medications,
            onTakeClick = null,
            onEditClick = null,
            onItemClick = null,
            onDeleteClick = null,
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
                    val groupedItems = groupMedicationsByMeal(todayMedications)
                    withContext(Dispatchers.Main) {
                        val adapter = DailyAdapter(
                            groupedItems,
                            onTakeClick = { medication -> showMedicationTaken(medication) },
                            onItemClick = { medication -> showMedicationDetails(medication) }
                        )
                        binding.recyclerViewDailyMedications.adapter = adapter
                        adapter.notifyDataSetChanged()

                        if (groupedItems.isEmpty()) showEmptyState() else hideEmptyState()
                    }
                }
            }
        }
    }

    private fun filterMedicationsForToday(medications: List<Medication>): List<Medication> {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)

        return medications.filter { medication ->
            when (medication.frecuencyOfTakeMedicine) {
                "Diaria" -> true
                "Semanal" -> isTodayInSelectedDays(medication, today)
                "Bisemanal" -> isTodayInBiweeklySchedule(medication, today, weekOfYear)
                "Mensual" -> isDayInMonthlySchedule(medication, dayOfMonth)
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

    private fun isTodayInBiweeklySchedule(medication: Medication, today: Int, weekOfYear: Int): Boolean {
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

        val isEvenWeek = weekOfYear % 2 == 0
        val dayPattern = if (isEvenWeek) "$todayName (par)" else "$todayName (impar)"

        return medication.frecuencyOfTakeMedicineExactDay.contains(dayPattern, ignoreCase = true)
    }

    private fun isDayInMonthlySchedule(medication: Medication, dayOfMonth: Int): Boolean {
        return medication.frecuencyOfTakeMedicineExactDay.contains("Dia $dayOfMonth", ignoreCase = true)
    }

    private fun showEmptyState() {
        binding.recyclerViewDailyMedications.visibility = android.view.View.GONE
        binding.emptyState.visibility = android.view.View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.recyclerViewDailyMedications.visibility = android.view.View.VISIBLE
        binding.emptyState.visibility = android.view.View.GONE
    }

    private fun groupMedicationsByMeal(medications: List<Medication>): List<DailyItem> {
        val result = mutableListOf<DailyItem>()
        val mealMap = mapOf(
            "DESAYUNO" to { m: Medication -> m.breakfast },
            "MEDIO DÍA" to { m: Medication -> m.midMorning },
            "COMIDA" to { m: Medication -> m.lunch },
            "MERIENDA" to { m: Medication -> m.snacking },
            "CENA" to { m: Medication -> m.dinner }
        )

        mealMap.forEach { (meal, predicate) ->
            medications.filter(predicate).takeIf { it.isNotEmpty() }?.let {
                result.add(DailyItem.Header(meal))
                result.addAll(it.map { med -> DailyItem.Med(med) })
            }
        }
        return result
    }

    private fun showMedicationTaken(medication: Medication) {
        lifecycleScope.launch {
            medicationDatabase.medicationDao().insertTake(Take(medicationId = medication.id))
            Snackbar.make(binding.root, "Medicamento ${medication.name} tomado", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showMedicationDetails(medication: Medication) {
        Intent(this, MedicationDetailActivity::class.java).apply {
            putExtra("MEDICATION_ID", medication.id)
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_DOSAGE", medication.dosage)
            putExtra("MEDICATION_FREQUENCY", medication.frequency)
            putExtra("MEDICATION_AdministrationType", medication.administrationType)
            putExtra("MEDICATION_dosageQuantity", medication.dosageQuantity)
            putExtra("MEDICATION_breakfast", medication.breakfast)
            putExtra("MEDICATION_midMorning", medication.midMorning)
            putExtra("MEDICATION_lunch", medication.lunch)
            putExtra("MEDICATION_snacking", medication.snacking)
            putExtra("MEDICATION_dinner", medication.dinner)
            putExtra("MEDICATION_frecuencyOfTakeMedicine", medication.frecuencyOfTakeMedicine)
            putExtra("MEDICATION_frecuencyOfTakeMedicineExactDay", medication.frecuencyOfTakeMedicineExactDay)
        }.also { startActivity(it) }
    }
}
