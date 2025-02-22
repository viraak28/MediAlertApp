package com.medialert.medinotiapp.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.medialert.medinotiapp.databinding.ActivityMedicationDetailBinding

class MedicationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del intent
        val medicationId = intent.getIntExtra("MEDICATION_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: ""
        val medicationDosage = intent.getStringExtra("MEDICATION_DOSAGE") ?: ""

        // Mostrar datos en las vistas
        binding.tvMedicationName.text = medicationName
        binding.tvMedicationDosage.text = medicationDosage

        // Aquí puedes agregar más lógica para mostrar detalles adicionales del medicamento
    }
}
