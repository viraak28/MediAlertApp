package com.medialert.medinotiapp.ui.activities.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.databinding.ActivityNotiConfigBinding
import com.medialert.medinotiapp.notifications.ReminderWorker
import com.medialert.medinotiapp.utils.SessionManager
import com.medialert.medinotiapp.data.MedinotiappDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotiConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotiConfigBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var sessionManager: SessionManager
    private var cambiosPendientes = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotiConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupPermissionLauncher()
        cargarEstado()
        setupUI()
        verificarMedicamentosIniciales()
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                binding.llRemindersConfig.visibility = View.VISIBLE
                cambiosPendientes = true
            } else {
                cancelarTodosRecordatorios()
                Toast.makeText(this, "Permiso necesario para notificaciones", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
        binding.btnCreateReminders.text = "Actualizar Recordatorios"

        // Configurar listeners después de cargar el estado
        binding.NotiON.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) solicitarPermisoYMostrarConfiguracion()
            else {
                binding.llRemindersConfig.visibility = View.GONE
                cancelarTodosRecordatorios()
            }
            cambiosPendientes = true
            guardarEstado()
        }

        binding.btnCreateReminders.setOnClickListener {
            crearRecordatorios()
        }

        setupMealButtons()
    }

    private fun setupMealButtons() {
        listOf(
            binding.btnDeactivateBreakfastNoti to "Desayuno",
            binding.btnDeactivateMidMorningNoti to "Media Mañana",
            binding.btnDeactivateLunchNoti to "Comida",
            binding.btnDeactivateSnackingNoti to "Merienda",
            binding.btnDeactivateDinnerNoti to "Cena"
        ).forEach { (button, comida) ->
            button.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val tieneMedicamentos = tieneMedicamentosAsociados(comida)
                    withContext(Dispatchers.Main) {
                        if (tieneMedicamentos) {
                            toggleButtonState(button)
                            cambiosPendientes = true
                            guardarEstado() // Guardar inmediatamente
                        } else {
                            Toast.makeText(
                                this@NotiConfigActivity,
                                "No hay medicamentos para $comida",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun verificarMedicamentosIniciales() {
        lifecycleScope.launch(Dispatchers.IO) {
            listOf(
                "Desayuno" to binding.btnDeactivateBreakfastNoti,
                "Media Mañana" to binding.btnDeactivateMidMorningNoti,
                "Comida" to binding.btnDeactivateLunchNoti,
                "Merienda" to binding.btnDeactivateSnackingNoti,
                "Cena" to binding.btnDeactivateDinnerNoti
            ).forEach { (comida, button) ->
                if (!tieneMedicamentosAsociados(comida)) {
                    withContext(Dispatchers.Main) {
                        button.text = "ACTIVAR"
                        disableTimeInputsForMeal(comida)
                    }
                }
            }
        }
    }

    private fun disableTimeInputsForMeal(comida: String) {
        when (comida) {
            "Desayuno" -> setTimeInputsEnabled(false,
                binding.etMedicationBreakfastHour,
                binding.etMedicationBreakfastMinute)
            "Media Mañana" -> setTimeInputsEnabled(false,
                binding.etMedicationMidMorningHour,
                binding.etMedicationMidMorningMinute)
            "Comida" -> setTimeInputsEnabled(false,
                binding.etMedicationLunchHour,
                binding.etMedicationLunchMinute)
            "Merienda" -> setTimeInputsEnabled(false,
                binding.etMedicationSnackingHour,
                binding.etMedicationSnackingMinute)
            "Cena" -> setTimeInputsEnabled(false,
                binding.etMedicationDinnerHour,
                binding.etMedicationDinnerMinute)
        }
    }

    private fun setTimeInputsEnabled(enabled: Boolean, vararg editTexts: EditText) {
        editTexts.forEach {
            it.isEnabled = enabled
            it.alpha = if (enabled) 1f else 0.5f
        }
    }

    private fun toggleButtonState(button: Button) {
        val wasActivated = button.text == "DESACTIVAR"
        button.text = if (wasActivated) "ACTIVAR" else "DESACTIVAR"

        val comida = when (button.id) {
            R.id.btnDeactivateBreakfastNoti -> "Desayuno"
            R.id.btnDeactivateMidMorningNoti -> "Media Mañana"
            R.id.btnDeactivateLunchNoti -> "Comida"
            R.id.btnDeactivateSnackingNoti -> "Merienda"
            R.id.btnDeactivateDinnerNoti -> "Cena"
            else -> ""
        }

        if (wasActivated) {
            cancelarRecordatorio(comida)
        }

        setTimeInputsEnabled(!wasActivated, *getTimeInputsForMeal(comida))
    }

    private fun getTimeInputsForMeal(comida: String): Array<EditText> {
        return when (comida) {
            "Desayuno" -> arrayOf(binding.etMedicationBreakfastHour, binding.etMedicationBreakfastMinute)
            "Media Mañana" -> arrayOf(binding.etMedicationMidMorningHour, binding.etMedicationMidMorningMinute)
            "Comida" -> arrayOf(binding.etMedicationLunchHour, binding.etMedicationLunchMinute)
            "Merienda" -> arrayOf(binding.etMedicationSnackingHour, binding.etMedicationSnackingMinute)
            "Cena" -> arrayOf(binding.etMedicationDinnerHour, binding.etMedicationDinnerMinute)
            else -> emptyArray()
        }
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

    private fun crearRecordatorios() {
        val isNotiON = binding.NotiON.isChecked
        val comidasInfo = listOf(
            Cuarteto(
                binding.btnDeactivateBreakfastNoti.text.toString(),
                "Desayuno",
                binding.etMedicationBreakfastHour.text.toString(),
                binding.etMedicationBreakfastMinute.text.toString()
            ),
            Cuarteto(
                binding.btnDeactivateMidMorningNoti.text.toString(),
                "Media Mañana",
                binding.etMedicationMidMorningHour.text.toString(),
                binding.etMedicationMidMorningMinute.text.toString()
            ),
            Cuarteto(
                binding.btnDeactivateLunchNoti.text.toString(),
                "Comida",
                binding.etMedicationLunchHour.text.toString(),
                binding.etMedicationLunchMinute.text.toString()
            ),
            Cuarteto(
                binding.btnDeactivateSnackingNoti.text.toString(),
                "Merienda",
                binding.etMedicationSnackingHour.text.toString(),
                binding.etMedicationSnackingMinute.text.toString()
            ),
            Cuarteto(
                binding.btnDeactivateDinnerNoti.text.toString(),
                "Cena",
                binding.etMedicationDinnerHour.text.toString(),
                binding.etMedicationDinnerMinute.text.toString()
            )
        )

        lifecycleScope.launch(Dispatchers.IO) {
            if (!isNotiON) {
                cancelarTodosRecordatorios()
                withContext(Dispatchers.Main) {
                    guardarEstado()
                    Toast.makeText(
                        this@NotiConfigActivity,
                        "Recordatorios desactivados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            comidasInfo.forEach { (estado, comida, hora, minutos) ->
                if (estado == "DESACTIVAR" && tieneMedicamentosAsociados(comida)) {
                    crearRecordatorioIndividual("$hora:$minutos", comida)
                }
            }

            withContext(Dispatchers.Main) {
                guardarEstado()
                Toast.makeText(
                    this@NotiConfigActivity,
                    "Recordatorios actualizados",
                    Toast.LENGTH_SHORT
                ).show()
                cambiosPendientes = false
            }
        }
    }

    private data class Cuarteto(
        val estado: String,
        val comida: String,
        val hora: String,
        val minutos: String
    )

    private suspend fun obtenerNombreUsuario(userId: Int): String {
        return withContext(Dispatchers.IO) {
            val userDatabase = MedinotiappDatabase.getDatabase(this@NotiConfigActivity)
            val usuario = userDatabase.userDao().getUserById(userId)
            usuario?.nombre ?: ""
        }
    }

    private fun cancelarRecordatorio(tipoComida: String) {
        val userId = sessionManager.getUserId()
        WorkManager.getInstance(this).cancelUniqueWork("${tipoComida}_${userId}")
    }

    private fun cancelarTodosRecordatorios() {
        val userId = sessionManager.getUserId()
        WorkManager.getInstance(this).cancelAllWorkByTag("recordatorio_medicacion_$userId")
    }

    private fun crearRecordatorioIndividual(hora: String, tipoComida: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = sessionManager.getUserId()
            val userName = obtenerNombreUsuario(userId)

            val calendar = Calendar.getInstance().apply {
                val (hours, minutes) = hora.split(":").map { it.toIntOrNull() ?: 0 }
                set(Calendar.HOUR_OF_DAY, hours)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }

            val request = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
                .setInitialDelay(calendar.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString("tipoComida", tipoComida)
                        .putString("userName", userName)
                        .build()
                )
                .addTag("recordatorio_medicacion_$userId")
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "${tipoComida}_${userId}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    private suspend fun tieneMedicamentosAsociados(comida: String): Boolean {
        return withContext(Dispatchers.IO) {
            val database = MedinotiappDatabase.getDatabase(this@NotiConfigActivity)
            val medicamentos = database.medicationDao().getAll()

            medicamentos.any { medicamento ->
                when (comida) {
                    "Desayuno" -> medicamento.breakfast
                    "Media Mañana" -> medicamento.midMorning
                    "Comida" -> medicamento.lunch
                    "Merienda" -> medicamento.snacking
                    "Cena" -> medicamento.dinner
                    else -> false
                }
            }
        }
    }

    private fun cargarEstado() {
        val prefsName = "recordatorios_prefs_${sessionManager.getUserId()}"
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)

        // Cargar estado principal
        binding.NotiON.isChecked = prefs.getBoolean("recordatoriosActivos", false)

        // Cargar estado de cada comida
        listOf(
            "Desayuno" to binding.btnDeactivateBreakfastNoti,
            "Media Mañana" to binding.btnDeactivateMidMorningNoti,
            "Comida" to binding.btnDeactivateLunchNoti,
            "Merienda" to binding.btnDeactivateSnackingNoti,
            "Cena" to binding.btnDeactivateDinnerNoti
        ).forEach { (comida, button) ->
            button.text = prefs.getString("estado_$comida", "ACTIVAR")
        }

        // Cargar horas
        binding.etMedicationBreakfastHour.setText(prefs.getString("horaDesayunoHour", "07"))
        binding.etMedicationBreakfastMinute.setText(prefs.getString("horaDesayunoMinute", "00"))
        binding.etMedicationMidMorningHour.setText(prefs.getString("horaMediaMananaHour", "11"))
        binding.etMedicationMidMorningMinute.setText(prefs.getString("horaMediaMananaMinute", "00"))
        binding.etMedicationLunchHour.setText(prefs.getString("horaComidaHour", "14"))
        binding.etMedicationLunchMinute.setText(prefs.getString("horaComidaMinute", "00"))
        binding.etMedicationSnackingHour.setText(prefs.getString("horaMeriendaHour", "18"))
        binding.etMedicationSnackingMinute.setText(prefs.getString("horaMeriendaMinute", "00"))
        binding.etMedicationDinnerHour.setText(prefs.getString("horaCenaHour", "21"))
        binding.etMedicationDinnerMinute.setText(prefs.getString("horaCenaMinute", "00"))

        // Configurar visibilidad
        binding.llRemindersConfig.visibility = if (binding.NotiON.isChecked) View.VISIBLE else View.GONE

        setupTextWatchers()
    }

    private fun guardarEstado() {
        val prefsName = "recordatorios_prefs_${sessionManager.getUserId()}"
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val editor = prefs.edit()

        // Guardar estado principal
        editor.putBoolean("recordatoriosActivos", binding.NotiON.isChecked)

        // Guardar estado de cada comida
        listOf(
            "Desayuno" to binding.btnDeactivateBreakfastNoti.text.toString(),
            "Media Mañana" to binding.btnDeactivateMidMorningNoti.text.toString(),
            "Comida" to binding.btnDeactivateLunchNoti.text.toString(),
            "Merienda" to binding.btnDeactivateSnackingNoti.text.toString(),
            "Cena" to binding.btnDeactivateDinnerNoti.text.toString()
        ).forEach { (comida, estado) ->
            editor.putString("estado_$comida", estado)
        }

        // Guardar horas
        editor.putString("horaDesayunoHour", binding.etMedicationBreakfastHour.text.toString())
        editor.putString("horaDesayunoMinute", binding.etMedicationBreakfastMinute.text.toString())
        editor.putString("horaMediaMananaHour", binding.etMedicationMidMorningHour.text.toString())
        editor.putString("horaMediaMananaMinute", binding.etMedicationMidMorningMinute.text.toString())
        editor.putString("horaComidaHour", binding.etMedicationLunchHour.text.toString())
        editor.putString("horaComidaMinute", binding.etMedicationLunchMinute.text.toString())
        editor.putString("horaMeriendaHour", binding.etMedicationSnackingHour.text.toString())
        editor.putString("horaMeriendaMinute", binding.etMedicationSnackingMinute.text.toString())
        editor.putString("horaCenaHour", binding.etMedicationDinnerHour.text.toString())
        editor.putString("horaCenaMinute", binding.etMedicationDinnerMinute.text.toString())

        editor.apply()
    }

    private fun setupTextWatchers() {
        listOf(
            binding.etMedicationBreakfastHour,
            binding.etMedicationBreakfastMinute,
            binding.etMedicationMidMorningHour,
            binding.etMedicationMidMorningMinute,
            binding.etMedicationLunchHour,
            binding.etMedicationLunchMinute,
            binding.etMedicationSnackingHour,
            binding.etMedicationSnackingMinute,
            binding.etMedicationDinnerHour,
            binding.etMedicationDinnerMinute
        ).forEach { editText ->
            editText.addTextChangedListener {
                guardarEstado()
            }
        }
    }
}
