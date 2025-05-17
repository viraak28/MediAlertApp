package com.medialert.medinotiapp.ui.activities.medications

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityAddMedicationBinding
import com.medialert.medinotiapp.models.Medication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.ArrayAdapter
import android.widget.Toast
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.utils.SessionManager
import kotlinx.coroutines.withContext

class AddMedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicationBinding
    private lateinit var medicationDatabase: MedinotiappDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedinotiappDatabase.getDatabase(this)
        sessionManager = SessionManager(this)


        // Configurar los Spinners
        setupSpinners()

        setupSaveButton()
    }

    private fun setupSpinners() {
        // Configurar el Spinner para la cantidad de dosis
        val dosageAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.dosage_options,
            R.layout.item_spinner
        )

        dosageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDosage.adapter = dosageAdapter

        // Configurar el Spinner para el tipo de administración
        val administrationAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.administration_options,
            R.layout.item_spinner
        )
        administrationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAdministration.adapter = administrationAdapter

        val frecuencyoftakemedicineAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.frecuencyoftakemedicine,
            R.layout.item_spinner
        )
        administrationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrecuencyoftakemedicine.adapter = frecuencyoftakemedicineAdapter
//vacio a la espera de la seleccion del spinner anterior
        val frecuencyoftakemedicineexactdayadapter = ArrayAdapter.createFromResource(
            this,
            R.array.emptyArray,
            R.layout.item_spinner)
        frecuencyoftakemedicineexactdayadapter .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrecuencyoftakemedicineexactday.adapter  = frecuencyoftakemedicineexactdayadapter

        // Agregar listener para cambiar el contenido y visibilidad del segundo Spinner
        binding.spinnerFrecuencyoftakemedicine.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val text_Chose= parent?.getItemAtPosition(position).toString()
                when (text_Chose) {
                    "Bisemanal" -> {
                        val frecuencyoftakemedicineexactdayadapter = ArrayAdapter.createFromResource(
                            this@AddMedicationActivity,
                            R.array.frecuencyofweek,
                            R.layout.item_spinner)
                        frecuencyoftakemedicineexactdayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerFrecuencyoftakemedicineexactday.adapter  = frecuencyoftakemedicineexactdayadapter
                        binding.spinnerFrecuencyoftakemedicineexactday.visibility = View.VISIBLE
                    }
                    "Semanal" -> {
                       val frecuencyoftakemedicineexactdayadapter = ArrayAdapter.createFromResource(
                           this@AddMedicationActivity,
                            R.array.frecuencyofweek,
                           R.layout.item_spinner)
                        frecuencyoftakemedicineexactdayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerFrecuencyoftakemedicineexactday.adapter  = frecuencyoftakemedicineexactdayadapter
                        binding.spinnerFrecuencyoftakemedicineexactday.visibility = View.VISIBLE
                    }
                    "Mensual" -> {
                        val frecuencyoftakemedicineexactdayadapter = ArrayAdapter.createFromResource(
                            this@AddMedicationActivity,
                            R.array.frecuencyofmonth,
                            R.layout.item_spinner)
                        frecuencyoftakemedicineexactdayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerFrecuencyoftakemedicineexactday.adapter  = frecuencyoftakemedicineexactdayadapter
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

        val frecuencyOfTakeMedicine = binding.spinnerFrecuencyoftakemedicine.selectedItem.toString()
        val frecuencyOfTakeMedicineExactDay = binding.spinnerFrecuencyoftakemedicineexactday.selectedItem.toString()

        // Checkboxes
        val breakfast = binding.etMedicationBreakfast.isChecked
        val midMorning = binding.etMedicationMidMonning.isChecked
        val lunch = binding.etMedicationLunch.isChecked
        val snacking = binding.etMedicationSnacking.isChecked
        val dinner = binding.etMedicationDinner.isChecked

        if (name.isNotEmpty() && dosage.isNotEmpty() && (breakfast || midMorning || lunch || snacking || dinner)) {
            lifecycleScope.launch(Dispatchers.IO) {
                val userId = sessionManager.getUserId()
                if (userId != -1) {
                    val newMedication = Medication(
                        name = name,
                        dosage = dosage,
                        dosageQuantity = dosageQuantity,
                        administrationType = administrationType,
                        frequency = frequency,
                        frecuencyOfTakeMedicine = frecuencyOfTakeMedicine,
                        frecuencyOfTakeMedicineExactDay = frecuencyOfTakeMedicineExactDay,
                        breakfast = breakfast,
                        midMorning = midMorning,
                        lunch = lunch,
                        snacking = snacking,
                        dinner = dinner,
                        userId = userId
                    )
                    medicationDatabase.medicationDao().insert(newMedication)

                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    // Maneja el caso en que no hay sesión activa
                    showError("No hay sesión activa")
                }
            }
        } else if (!(name.isNotEmpty() )){
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
