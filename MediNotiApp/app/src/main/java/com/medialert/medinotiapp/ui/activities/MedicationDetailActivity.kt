package com.medialert.medinotiapp.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.medialert.medinotiapp.adapters.TakeAdapter
import com.medialert.medinotiapp.data.MedicationDatabase
import com.medialert.medinotiapp.databinding.ActivityMedicationDetailBinding
import com.medialert.medinotiapp.notifications.ReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MedicationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationDetailBinding
    private lateinit var medicationDatabase: MedicationDatabase
    private var medicationId: Int = -1
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedicationDatabase.getDatabase(this)

        // Apply window insets to handle status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // Obtener datos del intent
        medicationId = intent.getIntExtra("MEDICATION_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: ""
        val medicationDosage = intent.getStringExtra("MEDICATION_DOSAGE") ?: ""
        val medicationFrequency = intent.getStringExtra("MEDICATION_FREQUENCY") ?: ""
        val medicationAdministrationType = intent.getStringExtra("MEDICATION_AdministrationType") ?: ""
        val medicationDosageQuantity = intent.getStringExtra("MEDICATION_dosageQuantity") ?: ""
        // Mostrar datos en las vistas
        binding.tvMedicationName.text = "Nombre: $medicationName"
        binding.tvMedicationDosage.text = "Dosis: $medicationDosage"
        binding.tvMedicationFrequency.text = "Frecuencia: $medicationFrequency"
        binding.tvMedicationDosageQuantity.text = "Cantidad : $medicationDosageQuantity $medicationAdministrationType"
        // Configurar RecyclerView y mostrar las tomas
        setupRecyclerView()
        loadTakes()

        // Configura el botón para programar el recordatorio
        binding.btnProgramarRecordatorio.setOnClickListener {
            val horaRecordatorio = binding.etRecordatorioHora.text.toString()
            solicitarPermisoYProgramarRecordatorio(horaRecordatorio)
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                programarRecordatorio(horaRecordatorio)
            } else {
                Toast.makeText(this, "Es necesario el permiso para enviar notificaciones", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvTakes.layoutManager = LinearLayoutManager(this)
    }

    private fun loadTakes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val takes = medicationDatabase.medicationDao().getTakesForMedication(medicationId).collect {
                withContext(Dispatchers.Main) {
                    binding.rvTakes.adapter = TakeAdapter(it)
                }
            }
        }
    }

    private fun solicitarPermisoYProgramarRecordatorio(hora: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                horaRecordatorio = hora
            } else {
                programarRecordatorio(hora)
            }
        } else {
            programarRecordatorio(hora)
        }
    }

    private lateinit var horaRecordatorio: String

    private fun programarRecordatorio(hora: String) {
        // Calcula el tiempo hasta la hora del recordatorio
        val calendar = Calendar.getInstance()
        val partesHora = hora.split(":")
        calendar.set(Calendar.HOUR_OF_DAY, partesHora[0].toInt())
        calendar.set(Calendar.MINUTE, partesHora[1].toInt())
        calendar.set(Calendar.SECOND, 0)

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            // Si la hora ya pasó, programa para el día siguiente
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val retraso = calendar.timeInMillis - System.currentTimeMillis()

        val workManager = WorkManager.getInstance(applicationContext)

        val request = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInitialDelay(retraso, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueue(request)
    }
}
