package com.medialert.medinotiapp.ui.activities.medications

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.adapters.WeekAdapter
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityWeeklyMedicationsBinding
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.models.Take
import com.medialert.medinotiapp.utils.DailyItem
import com.medialert.medinotiapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeeklyMedicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyMedicationsBinding
    private lateinit var medicationDatabase: MedinotiappDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: WeekAdapter
    private var currentWeekStart: Calendar = Calendar.getInstance()
    private var currentSelectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyMedicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedinotiappDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        setupWeekNavigation()
        setupSpinner()
        setupRecyclerView()
        loadMedications()
    }

    private fun setupWeekNavigation() {
        currentWeekStart = getStartOfWeek(Calendar.getInstance())
        updateWeekDisplay()

        binding.btnPreviousWeek.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekDisplay()
            loadMedications()
        }

        binding.btnNextWeek.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekDisplay()
            loadMedications()
        }
    }

    private fun setupSpinner() {
        val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val spinnerAdapter = ArrayAdapter(this, R.layout.item_spinner, daysOfWeek)

        binding.spinnerDays.apply {
            adapter = spinnerAdapter
            setSelection(getTodayIndex())
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    currentSelectedDay = when (position) {
                        0 -> Calendar.MONDAY
                        1 -> Calendar.TUESDAY
                        2 -> Calendar.WEDNESDAY
                        3 -> Calendar.THURSDAY
                        4 -> Calendar.FRIDAY
                        5 -> Calendar.SATURDAY
                        6 -> Calendar.SUNDAY // Corregido: Domingo como posición 6
                        else -> Calendar.MONDAY
                    }
                    loadMedications()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = WeekAdapter(
            emptyList(),
            onItemClick = { medication -> showMedicationDetails(medication) }
        )
        binding.recyclerViewWeeklyMedications.apply {
            layoutManager = LinearLayoutManager(this@WeeklyMedicationsActivity)
            adapter = this@WeeklyMedicationsActivity.adapter
        }
    }

    private fun loadMedications() {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                medicationDatabase.medicationDao().getMedicationsByUser(userId).collect { allMedications ->
                    val filtered = filterMedicationsForWeek(allMedications)
                    val groupedItems = groupMedicationsByMeal(filtered)

                    withContext(Dispatchers.Main) {
                        adapter.updateItems(groupedItems)
                        if (groupedItems.isEmpty()) showEmptyState() else hideEmptyState()
                    }
                }
            }
        }
    }

    private fun filterMedicationsForWeek(medications: List<Medication>): List<Medication> {
        return medications.filter { med ->
            when (med.frecuencyOfTakeMedicine) {
                "Diaria" -> true
                "Semanal" -> isDayInWeeklySchedule(med)
                "Bisemanal" -> isDayInBiweeklySchedule(med)
                "Mensual" -> isDayInMonthlySchedule(med)
                else -> false
            }
        }
    }

    // CORRECCIÓN CLAVE: Cálculo preciso del día del mes para domingos
    private fun isDayInMonthlySchedule(med: Medication): Boolean {
        val calendar = currentWeekStart.clone() as Calendar
        // Ajuste para días diferentes al lunes inicial
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val offset = (currentSelectedDay - currentDayOfWeek + 7) % 7
        calendar.add(Calendar.DAY_OF_MONTH, offset)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        return med.frecuencyOfTakeMedicineExactDay.contains("Dia $dayOfMonth", ignoreCase = true)
    }

    private fun isDayInWeeklySchedule(med: Medication): Boolean {
        val selectedDayName = getDayName(currentSelectedDay)
        return med.frecuencyOfTakeMedicineExactDay.contains(selectedDayName, ignoreCase = true)
    }

    private fun isDayInBiweeklySchedule(med: Medication): Boolean {
        val weekOfYear = currentWeekStart.get(Calendar.WEEK_OF_YEAR)
        val isEvenWeek = weekOfYear % 2 == 0
        val selectedDayName = getDayName(currentSelectedDay)

        return if (isEvenWeek) {
            med.frecuencyOfTakeMedicineExactDay.contains("$selectedDayName (par)", ignoreCase = true)
        } else {
            med.frecuencyOfTakeMedicineExactDay.contains("$selectedDayName (impar)", ignoreCase = true)
        }
    }

    private fun groupMedicationsByMeal(medications: List<Medication>): List<DailyItem> {
        val result = mutableListOf<DailyItem>()
        val mealMap = mapOf(
            "Desayuno" to { m: Medication -> m.breakfast },
            "Media mañana" to { m: Medication -> m.midMorning },
            "Comida" to { m: Medication -> m.lunch },
            "Merienda" to { m: Medication -> m.snacking },
            "Cena" to { m: Medication -> m.dinner }
        )

        mealMap.forEach { (meal, predicate) ->
            medications.filter(predicate).takeIf { it.isNotEmpty() }?.let {
                result.add(DailyItem.Header(meal))
                result.addAll(it.map { med -> DailyItem.Med(med) })
            }
        }
        return result
    }

    private fun updateWeekDisplay() {
        val dateFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))
        val endOfWeek = currentWeekStart.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_WEEK, 6)
        binding.txtWeekRange.text = "${dateFormat.format(currentWeekStart.time)} - ${dateFormat.format(endOfWeek.time)}"
    }

    private fun getStartOfWeek(calendar: Calendar): Calendar {
        val newCalendar = calendar.clone() as Calendar
        newCalendar.firstDayOfWeek = Calendar.MONDAY
        newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return newCalendar
    }

    private fun getTodayIndex(): Int {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6 // Corregido: Domingo en posición 6 del spinner
            else -> 0
        }
    }

    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> ""
        }
    }

    private fun showEmptyState() {
        binding.recyclerViewWeeklyMedications.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.recyclerViewWeeklyMedications.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    private fun showMedicationDetails(medication: Medication) {
        Intent(this, MedicationDetailActivity::class.java).apply {
            putExtra("MEDICATION_ID", medication.id)
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_DOSAGE", medication.dosage)
            putExtra("MEDICATION_FREQUENCY", medication.frequency)
            putExtra("MEDICATION_AdministrationType", medication.administrationType)
            putExtra("MEDICATION_dosageQuantity", medication.dosageQuantity)
            putExtra("MEDICATION_frecuencyOfTakeMedicine", medication.frecuencyOfTakeMedicine)
            putExtra("MEDICATION_frecuencyOfTakeMedicineExactDay", medication.frecuencyOfTakeMedicineExactDay)
        }.also { startActivity(it) }
    }
}
