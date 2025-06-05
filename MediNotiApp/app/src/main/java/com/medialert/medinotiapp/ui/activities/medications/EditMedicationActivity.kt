package com.medialert.medinotiapp.ui.activities.medications

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityAddMedicationBinding
import com.medialert.medinotiapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditMedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicationBinding
    private var medicationId: Int = -1
    private lateinit var medicationDatabase: MedinotiappDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var freqexactOp: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedinotiappDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        // Obtener datos del medicamento
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
        val newMedicationfrecuencyOfTakeMedicine = intent.getStringExtra("MEDICATION_frecuencyOfTakeMedicine") ?: ""
        val newMedicationfrecuencyOfTakeMedicineExactDay = intent.getStringExtra("MEDICATION_frecuencyOfTakeMedicineExactDay") ?: ""
        freqexactOp = newMedicationfrecuencyOfTakeMedicineExactDay

        // Establecer los valores en los campos de edición
        binding.etMedicationName.setText(medicationName)
        binding.etMedicationDosage.setText(medicationDosage)
        binding.etMedicationFrequency.setText(medicationFrequency)

        // ¡Cargar correctamente los switches!
        binding.etMedicationBreakfast.isChecked = newMedicationBreakfast
        binding.etMedicationMidMonning.isChecked = newMedicationMidMorning
        binding.etMedicationLunch.isChecked = newMedicationLunch
        binding.etMedicationSnacking.isChecked = newMedicationSnacking
        binding.etMedicationDinner.isChecked = newMedicationDinner

        // Configurar Spinners
        setupSpinners(
            newMedicationDosageQuantity.orEmpty(),
            newMedicationAdministrationType.orEmpty(),
            newMedicationfrecuencyOfTakeMedicine,
            newMedicationfrecuencyOfTakeMedicineExactDay
        )

        // Forzar actualización inicial del Spinner secundario
        binding.spinnerFrecuencyoftakemedicine.post {
            binding.spinnerFrecuencyoftakemedicine.setSelection(
                binding.spinnerFrecuencyoftakemedicine.selectedItemPosition
            )
        }

        setupSaveButton()
    }

    private fun setupSpinners(
        dosageOp: String,
        adminOp: String,
        freqOp: String,
        freqexactOp: String
    ) {
        // Configuración inicial de los Spinners
        setupDosageSpinner(dosageOp)
        setupAdministrationSpinner(adminOp)
        setupFrequencySpinners(freqOp, freqexactOp)
    }

    private fun setupDosageSpinner(dosageOp: String) {
        ArrayAdapter.createFromResource(
            this,
            R.array.dosage_options,
            R.layout.item_spinner
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDosage.adapter = this
            setSelectionSafe(dosageOp, binding.spinnerDosage)
        }
    }

    private fun setupAdministrationSpinner(adminOp: String) {
        ArrayAdapter.createFromResource(
            this,
            R.array.administration_options,
            R.layout.item_spinner
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerAdministration.adapter = this
            setSelectionSafe(adminOp, binding.spinnerAdministration)
        }
    }

    private fun setupFrequencySpinners(freqOp: String, freqexactOp: String) {
        // Spinner principal de frecuencia
        ArrayAdapter.createFromResource(
            this,
            R.array.frecuencyoftakemedicine,
            R.layout.item_spinner
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerFrecuencyoftakemedicine.adapter = this
            setSelectionSafe(freqOp, binding.spinnerFrecuencyoftakemedicine)
        }

        // Listener para actualizar Spinner secundario
        binding.spinnerFrecuencyoftakemedicine.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateExactDaySpinner(parent?.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateExactDaySpinner(selectedFrequency: String) {
        val arrayRes = when (selectedFrequency) {
            "Bisemanal", "Semanal" -> R.array.frecuencyofweek
            "Mensual" -> R.array.frecuencyofmonth
            else -> R.array.emptyArray
        }

        ArrayAdapter.createFromResource(
            this,
            arrayRes,
            R.layout.item_spinner
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerFrecuencyoftakemedicineexactday.adapter = this
            setSelectionSafe(freqexactOp, binding.spinnerFrecuencyoftakemedicineexactday)

            binding.spinnerFrecuencyoftakemedicineexactday.visibility =
                if (arrayRes != R.array.emptyArray) View.VISIBLE else View.GONE
        }
    }

    private fun setSelectionSafe(value: String, spinner: android.widget.Spinner) {
        (spinner.adapter as? ArrayAdapter<String>)?.let { adapter ->
            val position = adapter.getPosition(value)
            if (position != -1) spinner.setSelection(position)
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
        val frecuencyOfTakeMedicine = binding.spinnerFrecuencyoftakemedicine.selectedItem.toString()
        val frecuencyOfTakeMedicineExactDay = binding.spinnerFrecuencyoftakemedicineexactday.selectedItem.toString()
        // Switches
        val breakfast = binding.etMedicationBreakfast.isChecked
        val midMorning = binding.etMedicationMidMonning.isChecked
        val lunch = binding.etMedicationLunch.isChecked
        val snacking = binding.etMedicationSnacking.isChecked
        val dinner = binding.etMedicationDinner.isChecked

        val checkEmptyVal = name.isNotEmpty() && dosage.isNotEmpty()
        val checkEmptySpinner = dosageQuantity.isNotEmpty() && administrationType.isNotEmpty()
                && frecuencyOfTakeMedicine.isNotEmpty()

        if (checkEmptyVal  && checkEmptySpinner && (breakfast || midMorning || lunch || snacking || dinner)) {
            lifecycleScope.launch(Dispatchers.IO) {
                val medicationDao = medicationDatabase.medicationDao()
                val medication = medicationDao.getMedicationById(medicationId)
                if (medication != null && medication.userId == sessionManager.getUserId()) {
                    medication.name = name
                    medication.dosage = dosage
                    medication.administrationType = administrationType
                    medication.frequency = frequency
                    medication.frecuencyOfTakeMedicine = frecuencyOfTakeMedicine
                    medication.frecuencyOfTakeMedicineExactDay = frecuencyOfTakeMedicineExactDay
                    medication.dosageQuantity = dosageQuantity
                    medication.breakfast = breakfast
                    medication.midMorning = midMorning
                    medication.lunch = lunch
                    medication.dinner = dinner
                    medication.snacking = snacking
                    medicationDao.update(medication)

                    val resultIntent = Intent().apply {
                        putExtra("MEDICATION_ID", medicationId)
                        putExtra("MEDICATION_NAME", name)
                        putExtra("MEDICATION_DOSAGE", dosage)
                        putExtra("MEDICATION_FREQUENCY", frequency)
                        putExtra("MEDICATION_dosageQuantity", dosageQuantity)
                        putExtra("MEDICATION_administrationType", administrationType)
                        putExtra("MEDICATION_breakfast", breakfast)
                        putExtra("MEDICATION_midMorning", midMorning)
                        putExtra("MEDICATION_lunch", lunch)
                        putExtra("MEDICATION_snacking", snacking)
                        putExtra("MEDICATION_dinner", dinner)
                        putExtra("MEDICATION_frecuencyOfTakeMedicine", frecuencyOfTakeMedicine)
                        putExtra("MEDICATION_frecuencyOfTakeMedicineExactDay", frecuencyOfTakeMedicineExactDay)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    showError("Medicamento no encontrado")
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }  else if (!(name.isNotEmpty() )){
            showError("Por favor, complete el nombre")
        }
        else if (!( dosage.isNotEmpty())){
            showError("Por favor, complete la dosis")
        }
        else if (!(breakfast || midMorning || lunch || snacking || dinner)){
            showError("Por favor, seleccione una franja horaria")
        }
        else {
            showError("Por favor, complete todos los campos")
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
