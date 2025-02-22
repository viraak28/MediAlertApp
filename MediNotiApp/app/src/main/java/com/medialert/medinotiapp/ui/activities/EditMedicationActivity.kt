package com.medialert.medinotiapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.data.MedicationDatabase
import com.medialert.medinotiapp.databinding.ActivityAddMedicationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditMedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicationBinding
    private var medicationId: Int = -1
    private lateinit var medicationDatabase: MedicationDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedicationDatabase.getDatabase(this)

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
            lifecycleScope.launch(Dispatchers.IO) {
                // Actualizar el medicamento en la base de datos
                val medicationDao = medicationDatabase.medicationDao()
                val medication = medicationDao.getMedicationById(medicationId)
                if (medication != null) {
                    medication.name = name
                    medication.dosage = dosage
                    medication.frequency = frequency
                    medicationDao.update(medication)

                    // Enviar los datos actualizados a MedicationsActivity
                    val resultIntent = Intent().apply {
                        putExtra("MEDICATION_ID", medicationId)
                        putExtra("MEDICATION_NAME", name)
                        putExtra("MEDICATION_DOSAGE", dosage)
                        putExtra("MEDICATION_FREQUENCY", frequency)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    showError("Medicamento no encontrado")
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        } else {
            showError("Por favor, complete todos los campos")
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
