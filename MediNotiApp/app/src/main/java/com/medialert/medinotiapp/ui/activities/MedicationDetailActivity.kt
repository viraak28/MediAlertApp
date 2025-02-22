package com.medialert.medinotiapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.medialert.medinotiapp.databinding.ActivityMedicationDetailBinding

class MedicationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets to handle status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // Obtener datos del intent
        val medicationId = intent.getIntExtra("MEDICATION_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: ""
        val medicationDosage = intent.getStringExtra("MEDICATION_DOSAGE") ?: ""
        val medicationFrequency = intent.getStringExtra("MEDICATION_FREQUENCY") ?: ""

        // Mostrar datos en las vistas
        binding.tvMedicationName.text = "Nombre: $medicationName"
        binding.tvMedicationDosage.text = "Dosis: $medicationDosage"
        binding.tvMedicationFrequency.text = "Frecuencia: $medicationFrequency"
    }
}
