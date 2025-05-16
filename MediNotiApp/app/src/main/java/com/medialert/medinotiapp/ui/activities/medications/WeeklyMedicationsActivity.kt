package com.medialert.medinotiapp.ui.activities.medications

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.adapters.MedicationAdapter
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityWeeklyMedicationsBinding
import com.medialert.medinotiapp.models.Medication
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
    private lateinit var adapter: MedicationAdapter
    private val medications = mutableListOf<Medication>()
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
        // Configurar semana actual
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
        val daysOfWeek = listOf(
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner,
            daysOfWeek
        )

        binding.spinnerDays.adapter = spinnerAdapter

        val todayIndex = getTodayIndex()

        binding.spinnerDays.setSelection(todayIndex)

        binding.spinnerDays.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSelectedDay = when (position) {
                    0 -> Calendar.MONDAY
                    1 -> Calendar.TUESDAY
                    2 -> Calendar.WEDNESDAY
                    3 -> Calendar.THURSDAY
                    4 -> Calendar.FRIDAY
                    5 -> Calendar.SATURDAY
                    6 -> Calendar.SUNDAY
                    else -> Calendar.MONDAY
                }
                loadMedications()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter(
            medications,
            onTakeClick = null,
            onEditClick = null,
            onItemClick = null,
            onDeleteClick = null
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
                    withContext(Dispatchers.Main) {
                        medications.clear()
                        medications.addAll(filtered)
                        adapter.notifyDataSetChanged()
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

    private fun isDayInWeeklySchedule(med: Medication): Boolean {
        val selectedDayName = getDayName(currentSelectedDay)
        return med.frecuencyOfTakeMedicineExactDay.contains(selectedDayName, ignoreCase = true) == true
    }

    private fun isDayInBiweeklySchedule(med: Medication): Boolean {
        val weekOfYear = currentWeekStart.get(Calendar.WEEK_OF_YEAR)
        val isEvenWeek = weekOfYear % 2 == 0
        val selectedDayName = getDayName(currentSelectedDay)

        return if (isEvenWeek) {
            med.frecuencyOfTakeMedicineExactDay.contains("$selectedDayName (par)", ignoreCase = true) == true
        } else {
            med.frecuencyOfTakeMedicineExactDay.contains("$selectedDayName (impar)", ignoreCase = true) == true
        }
    }

    private fun isDayInMonthlySchedule(med: Medication): Boolean {
        val dayOfMonth = currentWeekStart.get(Calendar.DAY_OF_MONTH)
        return med.frecuencyOfTakeMedicineExactDay.contains("Dia $dayOfMonth") == true
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
            Calendar.SUNDAY -> 6
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
}
