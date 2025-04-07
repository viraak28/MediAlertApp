package com.medialert.medinotiapp.ui.activities.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import com.medialert.medinotiapp.databinding.ActivityNotiConfigBinding
import com.medialert.medinotiapp.notifications.ReminderWorker
import com.medialert.medinotiapp.utils.SessionManager
import com.medialert.medinotiapp.data.MedinotiappDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class NotiConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotiConfigBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotiConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                binding.llRemindersConfig.visibility = View.VISIBLE
                crearRecordatorios()
            } else {
                Toast.makeText(this, "Es necesario el permiso para enviar notificaciones", Toast.LENGTH_SHORT).show()
            }
        }

        binding.NotiON.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                solicitarPermisoYMostrarConfiguracion()
            } else {
                binding.llRemindersConfig.visibility = View.GONE
            }
        }

        binding.btnCreateReminders.setOnClickListener {
            solicitarPermisoYCrearRecordatorios()
        }

        // Configura horas por defecto
        binding.etMedicationBreakfasthour.setText("07")
        binding.etMedicationMidMonningHour.setText("11")
        binding.etMedicationLunchHour.setText("14")
        binding.etMedicationSnakingHour.setText("18")
        binding.etMedicationDinnerHour.setText("21")

        binding.etMedicationBreakfastminutes.setText("00")
        binding.etMedicationMidMonningMinute.setText("00")
        binding.etMedicationLunchMinute.setText("00")
        binding.etMedicationSnackingMinute.setText("00")
        binding.etMedicationDinerMinute.setText("00")

        cargarEstado()
    }

    private fun solicitarPermisoYMostrarConfiguracion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                binding.llRemindersConfig.visibility = View.VISIBLE
            }
        } else {
            binding.llRemindersConfig.visibility = View.VISIBLE
        }
    }

    private fun solicitarPermisoYCrearRecordatorios() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                crearRecordatorios()
            }
        } else {
            crearRecordatorios()
        }
    }

    private fun crearRecordatorios() {
        val horaDesayuno = "${binding.etMedicationBreakfasthour.text}:${binding.etMedicationBreakfastminutes.text}"
        val horaMediaManana = "${binding.etMedicationMidMonningHour.text}:${binding.etMedicationMidMonningMinute.text}"
        val horaComida = "${binding.etMedicationLunchHour.text}:${binding.etMedicationLunchMinute.text}"
        val horaMerienda = "${binding.etMedicationSnakingHour.text}:${binding.etMedicationSnackingMinute.text}"
        val horaCena = "${binding.etMedicationDinnerHour.text}:${binding.etMedicationDinerMinute.text}"

        crearRecordatorio(horaDesayuno, "Desayuno")
        crearRecordatorio(horaMediaManana, "Media mañana")
        crearRecordatorio(horaComida, "Comida")
        crearRecordatorio(horaMerienda, "Merienda")
        crearRecordatorio(horaCena, "Cena")

        Toast.makeText(this, "Recordatorios creados", Toast.LENGTH_SHORT).show()
    }

    private fun crearRecordatorio(hora: String, tipoComida: String) {
        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val userName = obtenerNombreUsuario(userId)

            val calendar = Calendar.getInstance()
            val partesHora = hora.split(":")
            calendar.set(Calendar.HOUR_OF_DAY, partesHora[0].toInt())
            calendar.set(Calendar.MINUTE, partesHora[1].toInt())
            calendar.set(Calendar.SECOND, 0)

            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val retraso = calendar.timeInMillis - System.currentTimeMillis()

            val request = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
                .setInitialDelay(retraso, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString("tipoComida", tipoComida)
                        .putString("userName", userName)
                        .build()
                )
                .build()

            WorkManager.getInstance(applicationContext).enqueue(request)
        }
    }

    private suspend fun obtenerNombreUsuario(userId: Int): String {
        val userDatabase = MedinotiappDatabase.getDatabase(this)
        val usuario = userDatabase.userDao().getUserById(userId)
        return usuario?.nombre ?: ""
    }

    private fun cargarEstado() {
        val prefsName = "recordatorios_prefs_${sessionManager.getUserId()}"
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)

        binding.NotiON.isChecked = prefs.getBoolean("recordatoriosActivos", false)
        binding.etMedicationBreakfasthour.setText(prefs.getString("horaDesayunoHour", "07"))
        binding.etMedicationBreakfastminutes.setText(prefs.getString("horaDesayunoMinute", "00"))
        binding.etMedicationMidMonningHour.setText(prefs.getString("horaMediaMananaHour", "11"))
        binding.etMedicationMidMonningMinute.setText(prefs.getString("horaMediaMananaMinute", "00"))
        binding.etMedicationLunchHour.setText(prefs.getString("horaComidaHour", "14"))
        binding.etMedicationLunchMinute.setText(prefs.getString("horaComidaMinute", "00"))
        binding.etMedicationSnakingHour.setText(prefs.getString("horaMeriendaHour", "18"))
        binding.etMedicationSnackingMinute.setText(prefs.getString("horaMeriendaMinute", "00"))
        binding.etMedicationDinnerHour.setText(prefs.getString("horaCenaHour", "21"))
        binding.etMedicationDinerMinute.setText(prefs.getString("horaCenaMinute", "00"))

        if (binding.NotiON.isChecked) {
            binding.llRemindersConfig.visibility = View.VISIBLE
        } else {
            binding.llRemindersConfig.visibility = View.GONE
        }

        binding.NotiON.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                solicitarPermisoYMostrarConfiguracion()
            } else {
                binding.llRemindersConfig.visibility = View.GONE
            }
            guardarEstado()
        }

        binding.etMedicationBreakfasthour.addTextChangedListener {
            guardarEstado()
        }

        binding.etMedicationBreakfastminutes.addTextChangedListener {
            guardarEstado()
        }

        binding.etMedicationMidMonningHour.addTextChangedListener {
            guardarEstado()
        }

        binding.etMedicationMidMonningMinute.addTextChangedListener {
            guardarEstado()
        }

        binding.etMedicationLunchHour.addTextChangedListener {
            guardarEstado()
        }

        binding.etMedicationLunchMinute.addTextChangedListener {
            guardarEstado()
        }

        binding.etMedicationSnakingHour.addTextChangedListener {
            guardarEstado()
        }

        binding.etMedicationSnackingMinute.addTextChangedListener {
            guardarEstado()
        }

        binding.etMedicationDinnerHour.addTextChangedListener {
            guardarEstado()
        }

        binding.etMedicationDinerMinute.addTextChangedListener {
            guardarEstado()
        }
    }

    private fun guardarEstado() {
        val prefsName = "recordatorios_prefs_${sessionManager.getUserId()}"
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putBoolean("recordatoriosActivos", binding.NotiON.isChecked)
        editor.putString("horaDesayunoHour", binding.etMedicationBreakfasthour.text.toString())
        editor.putString("horaDesayunoMinute", binding.etMedicationBreakfastminutes.text.toString())
        editor.putString("horaMediaMananaHour", binding.etMedicationMidMonningHour.text.toString())
        editor.putString("horaMediaMananaMinute", binding.etMedicationMidMonningMinute.text.toString())
        editor.putString("horaComidaHour", binding.etMedicationLunchHour.text.toString())
        editor.putString("horaComidaMinute", binding.etMedicationLunchMinute.text.toString())
        editor.putString("horaMeriendaHour", binding.etMedicationSnakingHour.text.toString())
        editor.putString("horaMeriendaMinute", binding.etMedicationSnackingMinute.text.toString())
        editor.putString("horaCenaHour", binding.etMedicationDinnerHour.text.toString())
        editor.putString("horaCenaMinute", binding.etMedicationDinerMinute.text.toString())

        editor.apply()
    }
}
