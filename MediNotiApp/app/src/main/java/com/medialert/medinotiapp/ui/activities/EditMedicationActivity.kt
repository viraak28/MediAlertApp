package com.medialert.medinotiapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.R
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
        val newMedicationDosageQuantity = intent.getStringExtra("MEDICATION_dosageQuantity")
        val newMedicationAdministrationType = intent.getStringExtra("MEDICATION_administrationType")
        val newMedicationBreakfast = intent.getBooleanExtra("MEDICATION_breakfast", false)
        val newMedicationMidMorning = intent.getBooleanExtra("MEDICATION_midMorning", false)
        val newMedicationLunch = intent.getBooleanExtra("MEDICATION_lunch", false)
        val newMedicationSnacking = intent.getBooleanExtra("MEDICATION_snacking", false)
        val newMedicationDinner = intent.getBooleanExtra("MEDICATION_dinner", false)


        // Establecer los valores en los campos de edición
        binding.etMedicationName.setText(medicationName)
        binding.etMedicationDosage.setText(medicationDosage)
        binding.etMedicationFrequency.setText(medicationFrequency)

        binding.etMedicationBreakfast.isChecked = newMedicationBreakfast
        binding.etMedicationMidMonning.isChecked =newMedicationMidMorning
        binding.etMedicationLunch.isChecked = newMedicationLunch
        binding.etMedicationSnacking.isChecked = newMedicationSnacking
        binding.etMedicationDinner.isChecked = newMedicationDinner
        //  Spinners
        setupSpinners(newMedicationDosageQuantity.toString(),newMedicationAdministrationType.toString())

        setupSaveButton()
    }
    private fun setupSpinners(dosage_op: String,admin_op:String) {
        // Configuracion  Spinner dosage_options

        val dosageAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.dosage_options,
            android.R.layout.simple_spinner_item
        )
        dosageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDosage.adapter = dosageAdapter
        val indice = dosageAdapter.getPosition(dosage_op)
        if (indice != -1) {
            binding.spinnerDosage.setSelection(indice)
        } else {
            println("Valor no encontrado en el Spinner")
        }
        // Config administration_options
        val administrationAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.administration_options,
            android.R.layout.simple_spinner_item
        )
        administrationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAdministration.adapter = administrationAdapter
        val indice2 = administrationAdapter.getPosition(admin_op)
        if (indice2 != -1) {
            binding.spinnerAdministration.setSelection(indice2)
        } else {
            println("Valor no encontrado en el Spinner")
        }
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
        val dosageQuantity = binding.spinnerDosage.selectedItem.toString()
        val administrationType = binding.spinnerAdministration.selectedItem.toString()
        // Checkboxes
        val breakfast = binding.etMedicationBreakfast.isChecked
        val midMorning = binding.etMedicationMidMonning.isChecked
        val lunch = binding.etMedicationLunch.isChecked
        val snacking = binding.etMedicationSnacking.isChecked
        val dinner = binding.etMedicationDinner.isChecked

        val checkEmptyVal = name.isNotEmpty() && dosage.isNotEmpty() && frequency.isNotEmpty()
        val checkEmptySpinner = dosageQuantity.isNotEmpty() && administrationType.isNotEmpty()

        if (checkEmptyVal  && checkEmptySpinner) {
            lifecycleScope.launch(Dispatchers.IO) {
                // Actualizar el medicamento en la base de datos
                val medicationDao = medicationDatabase.medicationDao()
                val medication = medicationDao.getMedicationById(medicationId)
                if (medication != null) {
                    medication.name = name
                    medication.dosage = dosage
                    medication.administrationType = administrationType
                    medication.frequency = frequency
                    medication.dosageQuantity =dosageQuantity
                    medication.breakfast = breakfast
                    medication.midMorning = midMorning
                    medication.lunch = lunch
                    medication.dinner = dinner
                    medication.snacking = snacking
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
