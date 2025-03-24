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

        val newMedicationfrecuencyOfTakeMedicine = intent.getStringExtra("MEDICATION_frecuencyOfTakeMedicine") ?: ""
        val newMedicationfrecuencyOfTakeMedicineExactDay = intent.getStringExtra("MEDICATION_frecuencyOfTakeMedicineExactDay") ?: ""

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
        setupSpinners(newMedicationDosageQuantity.toString(),newMedicationAdministrationType.toString(),
            newMedicationfrecuencyOfTakeMedicine,
            newMedicationfrecuencyOfTakeMedicineExactDay)

        setupSaveButton()
    }
    private fun setupSpinners(dosage_op: String,admin_op:String,freq_op:String,freqexact_op:String) {
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
        // Config frecuencyoftakemedicine
        val frecuencyoftakemedicineAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.frecuencyoftakemedicine,
            android.R.layout.simple_spinner_item
        )
        frecuencyoftakemedicineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrecuencyoftakemedicine.adapter = frecuencyoftakemedicineAdapter
        val indice3 = frecuencyoftakemedicineAdapter.getPosition(freq_op)
        if (indice3 != -1) {
            binding.spinnerFrecuencyoftakemedicine.setSelection(indice3)
        } else {
            println("Valor no encontrado en el Spinner")
        }

        // Config frecuencyoftakemedicineexactday
        val frecuencyoftakemedicineexactdayadapter = ArrayAdapter.createFromResource(
            this,
            R.array.emptyArray,
            android.R.layout.simple_spinner_item
        )
        frecuencyoftakemedicineexactdayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrecuencyoftakemedicineexactday.adapter = frecuencyoftakemedicineexactdayadapter

        // Agregar listener para cambiar el contenido y visibilidad del segundo Spinner
        binding.spinnerFrecuencyoftakemedicine.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val textoSeleccionado = parent?.getItemAtPosition(position).toString()
                when (textoSeleccionado) {
                    "Bisemanal" -> {
                        val frecuencyoftakemedicineexactdayadapter = ArrayAdapter.createFromResource(
                            this@EditMedicationActivity,
                            R.array.frecuencyofweek,
                            android.R.layout.simple_spinner_item)
                        frecuencyoftakemedicineexactdayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerFrecuencyoftakemedicineexactday.adapter  = frecuencyoftakemedicineexactdayadapter
                        binding.spinnerFrecuencyoftakemedicineexactday.visibility = View.VISIBLE

                    }
                    "Semanal" -> {
                        val frecuencyoftakemedicineexactdayadapter = ArrayAdapter.createFromResource(
                            this@EditMedicationActivity,
                            R.array.frecuencyofweek,
                            android.R.layout.simple_spinner_item)
                        frecuencyoftakemedicineexactdayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerFrecuencyoftakemedicineexactday.adapter  = frecuencyoftakemedicineexactdayadapter
                        binding.spinnerFrecuencyoftakemedicineexactday.visibility = View.VISIBLE
                    }
                    "Mensual" -> {
                        val frecuencyoftakemedicineexactdayadapter = ArrayAdapter.createFromResource(
                            this@EditMedicationActivity,
                            R.array.frecuencyofmonth,
                            android.R.layout.simple_spinner_item)
                        binding.spinnerFrecuencyoftakemedicineexactday.adapter = frecuencyoftakemedicineexactdayadapter
                        binding.spinnerFrecuencyoftakemedicineexactday.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.spinnerFrecuencyoftakemedicineexactday.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se ha seleccionado nada
            }
            }

            // Seleccionar el valor exacto del día si existe
            val indiceExactDay = frecuencyoftakemedicineexactdayadapter.getPosition(freqexact_op)
            if (indiceExactDay != -1) {
                binding.spinnerFrecuencyoftakemedicineexactday.setSelection(indiceExactDay)
            } else {
                println("Valor no encontrado en el Spinner exacto del día")
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
        // Checkboxes
        val breakfast = binding.etMedicationBreakfast.isChecked
        val midMorning = binding.etMedicationMidMonning.isChecked
        val lunch = binding.etMedicationLunch.isChecked
        val snacking = binding.etMedicationSnacking.isChecked
        val dinner = binding.etMedicationDinner.isChecked

        val checkEmptyVal = name.isNotEmpty() && dosage.isNotEmpty() && frequency.isNotEmpty()
        val checkEmptySpinner = dosageQuantity.isNotEmpty() && administrationType.isNotEmpty()
                && frecuencyOfTakeMedicine.isNotEmpty()

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
                    medication.frecuencyOfTakeMedicine = frecuencyOfTakeMedicine
                    medication.frecuencyOfTakeMedicineExactDay = frecuencyOfTakeMedicineExactDay
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
                        putExtra("MEDICATION_dosageQuantity", dosageQuantity)
                        putExtra("MEDICATION_administrationType", administrationType)
//                        putExtra("MEDICATION_breakfast", breakfast)
//                        putExtra("MEDICATION_midMorning", midMorning)
//                        putExtra("MEDICATION_lunch", lunch)
//                        putExtra("MEDICATION_snacking", snacking)
//                        putExtra("MEDICATION_dinner", dinner)
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
        } else {
            showError("Por favor, complete todos los campos")
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
