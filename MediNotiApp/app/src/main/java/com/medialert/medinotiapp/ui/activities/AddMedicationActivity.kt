package com.medialert.medinotiapp.ui.activities

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.data.MedicationDatabase
import com.medialert.medinotiapp.databinding.ActivityAddMedicationBinding
import com.medialert.medinotiapp.models.Medication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.ArrayAdapter
import com.medialert.medinotiapp.R

class AddMedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicationBinding
    private lateinit var medicationDatabase: MedicationDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedicationDatabase.getDatabase(this)

        // Configurar los Spinners
        setupSpinners()

        setupSaveButton()
    }

    private fun setupSpinners() {
        // Configurar el Spinner para la cantidad de dosis
        val dosageAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.dosage_options,
            android.R.layout.simple_spinner_item
        )
        dosageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDosage.adapter = dosageAdapter

        // Configurar el Spinner para el tipo de administración
        val administrationAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.administration_options,
            android.R.layout.simple_spinner_item
        )
        administrationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAdministration.adapter = administrationAdapter
    }

    private fun setupSaveButton() {
        binding.btnSaveMedication.setOnClickListener {
            saveMedication()
        }
    }

    private fun saveMedication() {
        val name = binding.etMedicationName.text.toString().trim()
        val dosage = binding.etMedicationDosage.text.toString().trim()
        val dosageQuantity = binding.spinnerDosage.selectedItem.toString()
        val administrationType = binding.spinnerAdministration.selectedItem.toString()
        val frequency = binding.etMedicationFrequency.text.toString().trim()

        // Checkboxes
        val breakfast = binding.etMedicationBreakfast.isChecked
        val midMorning = binding.etMedicationMidMonning.isChecked
        val lunch = binding.etMedicationLunch.isChecked
        val snacking = binding.etMedicationSnacking.isChecked
        val dinner = binding.etMedicationDinner.isChecked

        if (name.isNotEmpty() && dosage.isNotEmpty() && frequency.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val newMedication = Medication(
                    name = name,
                    dosage = dosage,
                    dosageQuantity = dosageQuantity,
                    administrationType = administrationType,
                    frequency = frequency,
                    breakfast = breakfast,
                    midMorning = midMorning,
                    lunch = lunch,
                    snacking = snacking,
                    dinner = dinner
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
