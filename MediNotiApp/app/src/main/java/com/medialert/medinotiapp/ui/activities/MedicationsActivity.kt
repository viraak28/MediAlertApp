package com.medialert.medinotiapp.ui.activities

import android.app.Activity
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
import kotlinx.coroutines.flow.collect
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
        }
        startActivityForResult(intent, EDIT_MEDICATION_REQUEST_CODE)
    }

    private fun showMedicationTaken(medication: Medication) {
        lifecycleScope.launch {
            medicationDatabase.medicationDao().insertTake(Take(medicationId = medication.id))
            Snackbar.make(binding.root, "Medicamento ${medication.name} tomado", Snackbar.LENGTH_SHORT).show()
        }
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

            if (newMedicationName != null && newMedicationDosage != null && newMedicationFrequency != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val newMedication = Medication(
                        name = newMedicationName,
                        dosage = newMedicationDosage,
                        frequency = newMedicationFrequency
                    )
                    medicationDatabase.medicationDao().insert(newMedication)
                }
            }
        } else if (requestCode == EDIT_MEDICATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val medicationId = data?.getIntExtra("MEDICATION_ID", -1) ?: -1
            val editedMedicationName = data?.getStringExtra("MEDICATION_NAME")
            val editedMedicationDosage = data?.getStringExtra("MEDICATION_DOSAGE")
            val editedMedicationFrequency = data?.getStringExtra("MEDICATION_FREQUENCY")

            if (medicationId != -1 && editedMedicationName != null && editedMedicationDosage != null && editedMedicationFrequency != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val updatedMedication = Medication(
                        id = medicationId,
                        name = editedMedicationName,
                        dosage = editedMedicationDosage,
                        frequency = editedMedicationFrequency
                    )
                    medicationDatabase.medicationDao().update(updatedMedication)
                }
            }
        }
    }

    companion object {
        private const val ADD_MEDICATION_REQUEST_CODE = 1
        private const val EDIT_MEDICATION_REQUEST_CODE = 2
    }
}
