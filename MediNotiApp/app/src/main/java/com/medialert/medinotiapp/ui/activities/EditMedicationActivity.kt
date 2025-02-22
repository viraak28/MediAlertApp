package com.medialert.medinotiapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.medialert.medinotiapp.databinding.ActivityAddMedicationBinding // Reutilizamos el layout de AddMedication
import com.medialert.medinotiapp.models.Medication

class EditMedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicationBinding
    private var medicationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener los datos del medicamento a editar
        medicationId = intent.getIntExtra("MEDICATION_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: ""
        val medicationDosage = intent.getStringExtra("MEDICATION_DOSAGE") ?: ""
        val medicationFrequency = intent.getStringExtra("MEDICATION_FREQUENCY") ?: ""

        // Establecer los valores en los campos de edición
        binding.etMedicationName.setText(medicationName)
        binding.etMedicationDosage.setText(medicationDosage)
        binding.etMedicationFrequency.setText(medicationFrequency)

        setupSaveButton()
    }

    private fun setupSaveButton() {
        binding.btnSaveMedication.setOnClickListener {
            saveMedication()
        }
    }

    private fun saveMedication() {
        val name = binding.etMedicationName.text.toString().trim()
        val dosage = binding.etMedicationDosage.text.toString().trim()
        val frequency = binding.etMedicationFrequency.text.toString().trim()

        if (name.isNotEmpty() && dosage.isNotEmpty() && frequency.isNotEmpty()) {
            val resultIntent = Intent().apply {
                putExtra("MEDICATION_ID", medicationId)
                putExtra("MEDICATION_NAME", name)
                putExtra("MEDICATION_DOSAGE", dosage)
                putExtra("MEDICATION_FREQUENCY", frequency)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            // Mostrar un mensaje de error si los campos están vacíos
            // Por ejemplo, usando un Snackbar:
            // Snackbar.make(binding.root, "Please fill all fields", Snackbar.LENGTH_SHORT).show()
        }
    }
}
