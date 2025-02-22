package com.medialert.medinotiapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.data.MedicationDatabase
import com.medialert.medinotiapp.databinding.ActivityAddMedicationBinding
import com.medialert.medinotiapp.models.Medication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddMedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicationBinding
    private lateinit var medicationDatabase: MedicationDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedicationDatabase.getDatabase(this)

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
                val newMedication = Medication(
                    name = name,
                    dosage = dosage,
                    frequency = frequency
                )
                medicationDatabase.medicationDao().insert(newMedication)

                setResult(Activity.RESULT_OK)
                finish()
            }
        } else {
            showError("Por favor, complete todos los campos")
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
