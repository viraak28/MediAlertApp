package com.medialert.medinotiapp.ui.activities.medications

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.adapters.MedicationAdapter
import com.medialert.medinotiapp.data.MedicationDatabase
import com.medialert.medinotiapp.databinding.ActivityMedicationsBinding
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.models.Take
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationsBinding
    private lateinit var adapter: MedicationAdapter
    private lateinit var medicationDatabase: MedicationDatabase

    private val medications = mutableListOf<Medication>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedicationDatabase.getDatabase(this)

        setupRecyclerView()
        setupFab()

        loadMedications()
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter(
            medications,
            onTakeClick = { medication ->
                showMedicationTaken(medication)
            },
            onEditClick = { medication ->
                editMedication(medication)
            },
            onItemClick = { medication ->
                showMedicationDetails(medication)
            },
            onDeleteClick = { medication ->
                deleteMedication(medication)
            }
        )
        binding.recyclerViewMedications.apply {
            layoutManager = LinearLayoutManager(this@MedicationsActivity)
            adapter = this@MedicationsActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddMedication.setOnClickListener {
            addNewMedication()
        }
    }

    private fun showMedicationDetails(medication: Medication) {
        val intent = Intent(this, MedicationDetailActivity::class.java).apply {
            putExtra("MEDICATION_ID", medication.id)
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_DOSAGE", medication.dosage)
            putExtra("MEDICATION_FREQUENCY", medication.frequency)
            putExtra("MEDICATION_AdministrationType", medication.administrationType)
            putExtra("MEDICATION_dosageQuantity", medication.dosageQuantity)
            putExtra("MEDICATION_frecuencyOfTakeMedicine", medication.frecuencyOfTakeMedicine)
            putExtra("MEDICATION_frecuencyOfTakeMedicineExactDay", medication.frecuencyOfTakeMedicineExactDay)
        }
        startActivity(intent)
    }

    private fun addNewMedication() {
        val intent = Intent(this, AddMedicationActivity::class.java)
        startActivityForResult(intent, ADD_MEDICATION_REQUEST_CODE)
    }

    private fun editMedication(medication: Medication) {
        val intent = Intent(this, EditMedicationActivity::class.java).apply {
            putExtra("MEDICATION_ID", medication.id)
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_DOSAGE", medication.dosage)
            putExtra("MEDICATION_FREQUENCY", medication.frequency)
            putExtra("MEDICATION_dosageQuantity", medication.dosageQuantity)
            putExtra("MEDICATION_breakfast", medication.breakfast)
            putExtra("MEDICATION_midMorning", medication.midMorning)
            putExtra("MEDICATION_lunch", medication.lunch)
            putExtra("MEDICATION_snacking", medication.snacking)
            putExtra("MEDICATION_dinner", medication.dinner)
            putExtra("MEDICATION_administrationType", medication.administrationType)
            putExtra("MEDICATION_frecuencyOfTakeMedicine", medication.frecuencyOfTakeMedicine)
            putExtra("MEDICATION_frecuencyOfTakeMedicineExactDay", medication.frecuencyOfTakeMedicineExactDay)

        }
        startActivityForResult(intent, EDIT_MEDICATION_REQUEST_CODE)
    }

    private fun showMedicationTaken(medication: Medication) {
        lifecycleScope.launch {
            medicationDatabase.medicationDao().insertTake(Take(medicationId = medication.id))
            Snackbar.make(binding.root, "Medicamento ${medication.name} tomado", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun deleteMedication(medication: Medication) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de continuar?")
        builder.setPositiveButton("Sí") { _, _ ->
            // Acción si se confirma
            //medicationDatabase = MedicationDatabase.getDatabase(this)
            lifecycleScope.launch(Dispatchers.IO) {
                medicationDatabase.medicationDao().delete(medication)
            }
            println("Se ha confirmado")
        }
        builder.setNegativeButton("No") { _, _ ->
            // Acción si se cancela
            println("Se ha cancelado")
        }
        builder.show()
    }


    private fun loadMedications() {
        lifecycleScope.launch {
            medicationDatabase.medicationDao().getAll().collect { medicationsList ->
                withContext(Dispatchers.Main) {
                    medications.clear()
                    medications.addAll(medicationsList)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_MEDICATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val newMedicationName = data?.getStringExtra("NEW_MEDICATION_NAME")
            val newMedicationDosage = data?.getStringExtra("NEW_MEDICATION_DOSAGE")
            val newMedicationFrequency = data?.getStringExtra("NEW_MEDICATION_FREQUENCY")
            val newMedicationDosageQuantity = data?.getStringExtra("NEW_MEDICATION_dosageQuantity")
            val newMedicationAdministrationType = data?.getStringExtra("NEW_MEDICATION_administrationType")
            val newMedicationBreakfast = data?.getBooleanExtra("NEW_MEDICATION_breakfast", false)
            val newMedicationMidMorning = data?.getBooleanExtra("NEW_MEDICATION_midMorning", false)
            val newMedicationLunch = data?.getBooleanExtra("NEW_MEDICATION_lunch", false)
            val newMedicationSnacking = data?.getBooleanExtra("NEW_MEDICATION_snacking", false)
            val newMedicationDinner = data?.getBooleanExtra("NEW_MEDICATION_dinner", false)

            val newMedicationfrecuencyOfTakeMedicine = data?.getStringExtra("NEW_MEDICATION_frecuencyOfTakeMedicine")
            val newMedicationfrecuencyOfTakeMedicineExactDay = data?.getStringExtra("NEW_MEDICATION_frecuencyOfTakeMedicineExactDay")


            if (newMedicationName != null && newMedicationFrequency != null &&
                newMedicationDosageQuantity != null && newMedicationAdministrationType != null &&
                newMedicationDosage != null && newMedicationfrecuencyOfTakeMedicine!= null
                && newMedicationfrecuencyOfTakeMedicineExactDay!= null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val newMedication = Medication(
                        name = newMedicationName,
                        dosage = newMedicationDosage,
                        frequency = newMedicationFrequency,
                        dosageQuantity = newMedicationDosageQuantity,
                        administrationType = newMedicationAdministrationType,
                        breakfast = newMedicationBreakfast == true,
                        midMorning = newMedicationMidMorning  == true,
                        lunch = newMedicationLunch == true,
                        snacking = newMedicationSnacking == true,
                        dinner = newMedicationDinner == true,
                        frecuencyOfTakeMedicine = newMedicationfrecuencyOfTakeMedicine,
                        frecuencyOfTakeMedicineExactDay = newMedicationfrecuencyOfTakeMedicineExactDay
                    )
                    medicationDatabase.medicationDao().insert(newMedication)
                }
            } else {
                // Maneja el caso en que los datos no están completos
                showError("No se han recibido todos los datos necesarios")
            }
        } else if (requestCode == EDIT_MEDICATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val medicationId = data?.getIntExtra("MEDICATION_ID", -1) ?: -1
            val editedMedicationName = data?.getStringExtra("MEDICATION_NAME")
            val editedMedicationDosage = data?.getStringExtra("MEDICATION_DOSAGE")
            val editedMedicationFrequency = data?.getStringExtra("MEDICATION_FREQUENCY")
            val editedMedicationDosageQuantity = data?.getStringExtra("EDITED_MEDICATION_dosageQuantity")
            val editedMedicationAdministrationType = data?.getStringExtra("EDITED_MEDICATION_administrationType")
            val editedMedicationBreakfast = data?.getBooleanExtra("EDITED_MEDICATION_breakfast", false)
            val editedMedicationMidMorning = data?.getBooleanExtra("EDITED_MEDICATION_midMorning", false)
            val editedMedicationLunch = data?.getBooleanExtra("EDITED_MEDICATION_lunch", false)
            val editedMedicationSnacking = data?.getBooleanExtra("EDITED_MEDICATION_snacking", false)
            val editedMedicationDinner = data?.getBooleanExtra("EDITED_MEDICATION_dinner", false)

            val editedMedicationfrecuencyOfTakeMedicine = data?.getStringExtra("NEW_MEDICATION_frecuencyOfTakeMedicine")
            val editedMedicationfrecuencyOfTakeMedicineExactDay = data?.getStringExtra("NEW_MEDICATION_frecuencyOfTakeMedicineExactDay")


            if (medicationId != -1 && editedMedicationName != null && editedMedicationFrequency != null &&
                editedMedicationDosageQuantity != null && editedMedicationAdministrationType != null &&
                editedMedicationDosage != null && editedMedicationfrecuencyOfTakeMedicine!= null
                && editedMedicationfrecuencyOfTakeMedicineExactDay!= null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val updatedMedication = Medication(
                        id = medicationId,
                        name = editedMedicationName,
                        dosage = editedMedicationDosage,
                        frequency = editedMedicationFrequency,
                        dosageQuantity = editedMedicationDosageQuantity,
                        administrationType = editedMedicationAdministrationType,
                        breakfast = editedMedicationBreakfast == true,
                        midMorning = editedMedicationMidMorning == true,
                        lunch = editedMedicationLunch == true,
                        snacking = editedMedicationSnacking == true,
                        dinner = editedMedicationDinner == true,
                        frecuencyOfTakeMedicine = editedMedicationfrecuencyOfTakeMedicine,
                        frecuencyOfTakeMedicineExactDay = editedMedicationfrecuencyOfTakeMedicineExactDay
                    )

                    medicationDatabase.medicationDao().update(updatedMedication)
                }
            }
            else if (medicationId != -1 && editedMedicationName != null && editedMedicationFrequency != null &&
                editedMedicationDosageQuantity != null && editedMedicationAdministrationType != null &&
                editedMedicationDosage != null && editedMedicationfrecuencyOfTakeMedicine!= null
               ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val updatedMedication = Medication(
                        id = medicationId,
                        name = editedMedicationName,
                        dosage = editedMedicationDosage,
                        frequency = editedMedicationFrequency,
                        dosageQuantity = editedMedicationDosageQuantity,
                        administrationType = editedMedicationAdministrationType,
                        breakfast = editedMedicationBreakfast == true,
                        midMorning = editedMedicationMidMorning == true,
                        lunch = editedMedicationLunch == true,
                        snacking = editedMedicationSnacking == true,
                        dinner = editedMedicationDinner == true,
                        frecuencyOfTakeMedicine = editedMedicationfrecuencyOfTakeMedicine,
                        frecuencyOfTakeMedicineExactDay = ""
                    )

                    medicationDatabase.medicationDao().update(updatedMedication)
                }
            }
            else {
                // Maneja el caso en que los datos no están completos
                showError("No se han recibido todos los datos necesarios para editar")
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private const val ADD_MEDICATION_REQUEST_CODE = 1
        private const val EDIT_MEDICATION_REQUEST_CODE = 2
    }
}
