package com.medialert.medinotiapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.medialert.medinotiapp.databinding.ActivityMainBinding
import com.medialert.medinotiapp.ui.activities.SplashScreenActivity
import com.medialert.medinotiapp.ui.activities.medications.DailyMedicationsActivity
import com.medialert.medinotiapp.ui.activities.medications.MedicationsActivity
import com.medialert.medinotiapp.ui.activities.medications.WeeklyMedicationsActivity
import com.medialert.medinotiapp.ui.activities.reminders.NotiConfigActivity
import com.medialert.medinotiapp.ui.activities.notebooks.NotebooksActivity
import com.medialert.medinotiapp.ui.activities.users.PerfilActivity
import com.medialert.medinotiapp.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupNavigationButtons()
        setupWeekButton()
        setupNotebookButton()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        sessionManager.clearSession()
        val intent = Intent(this, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupNavigationButtons() {
        binding.btnPerfil.setOnClickListener {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                val intent = Intent(this, PerfilActivity::class.java)
                intent.putExtra("USUARIO_ID", userId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMedications.setOnClickListener {
            val intent = Intent(this, MedicationsActivity::class.java)
            startActivity(intent)
        }

        binding.btnDay.setOnClickListener {
            val intent = Intent(this, DailyMedicationsActivity::class.java)
            startActivity(intent)
        }

        binding.btnReminders.setOnClickListener {
            val intent = Intent(this, NotiConfigActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, SplashScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupWeekButton() {
        val weekRange = getWeekRange()
        binding.txtWeekRange.text = weekRange

        binding.btnWeek.setOnClickListener {
            val intent = Intent(this, WeeklyMedicationsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupNotebookButton() {
        binding.btnNotebook.setOnClickListener {
            val intent = Intent(this, NotebooksActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getWeekRange(): String {
        val calendar = Calendar.getInstance()

        // Establecer el primer día de la semana (lunes)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val startOfWeek = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6) // Sumar 6 días para obtener el último día de la semana
        val endOfWeek = calendar.time

        // Formato de fecha: por ejemplo "07 Abr - 13 Abr"
        val dateFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))
        return "${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}"
    }
}
