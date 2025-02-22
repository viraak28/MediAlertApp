package com.medialert.medinotiapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.adapters.MedicationAdapter
import com.medialert.medinotiapp.databinding.ActivityMedicationsBinding
import com.medialert.medinotiapp.models.Medication

class MedicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationsBinding
    private lateinit var adapter: MedicationAdapter
    private val medications = mutableListOf<Medication>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = MedicationAdapter(
            medications,
            onTakeClick = { medication ->
                // Acción al hacer clic en "Tomar"
                showMedicationTaken(medication)
            },
            onEditClick = { medication ->
                // Acción al hacer clic en "Editar"
                editMedication(medication)
            },
            onItemClick = { medication ->
                // Acción al hacer clic en el item
                showMedicationDetails(medication)
            }
        )
        binding.recyclerViewMedications.apply {
            layoutManager = LinearLayoutManager(this@MedicationsActivity)
            adapter = this@MedicationsActivity.adapter
        }

        // Cargar medicamentos iniciales (esto podría venir de una base de datos en el futuro)
        medications.addAll(
            listOf(
                Medication(1, "Paracetamol", "500 mg", "Cada 8 horas"),
                Medication(2, "Ibuprofeno", "200 mg", "Cada 6 horas"),
                Medication(3, "Amoxicilina", "1 g", "Cada 12 horas")
            )
        )
        adapter.notifyDataSetChanged()
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
        Snackbar.make(binding.root, "Medicamento ${medication.name} tomado", Snackbar.LENGTH_SHORT).show()
        // Aquí implementarías la lógica para registrar la toma del medicamento,
        // por ejemplo, guardar la información en una base de datos local.
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_MEDICATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val newMedicationName = data?.getStringExtra("NEW_MEDICATION_NAME")
            val newMedicationDosage = data?.getStringExtra("NEW_MEDICATION_DOSAGE")
            val newMedicationFrequency = data?.getStringExtra("NEW_MEDICATION_FREQUENCY")

            if (newMedicationName != null && newMedicationDosage != null && newMedicationFrequency != null) {
                val newId = medications.maxByOrNull { it.id }?.id?.plus(1) ?: 1
                val newMedication = Medication(newId, newMedicationName, newMedicationDosage, newMedicationFrequency)
                medications.add(newMedication)
                adapter.notifyItemInserted(medications.size - 1)
            }
        } else if (requestCode == EDIT_MEDICATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val medicationId = data?.getIntExtra("MEDICATION_ID", -1) ?: -1
            val editedMedicationName = data?.getStringExtra("MEDICATION_NAME")
            val editedMedicationDosage = data?.getStringExtra("MEDICATION_DOSAGE")
            val editedMedicationFrequency = data?.getStringExtra("MEDICATION_FREQUENCY")

            if (medicationId != -1 && editedMedicationName != null && editedMedicationDosage != null && editedMedicationFrequency != null) {
                val index = medications.indexOfFirst { it.id == medicationId }
                if (index != -1) {
                    val updatedMedication = Medication(medicationId, editedMedicationName, editedMedicationDosage, editedMedicationFrequency)
                    medications[index] = updatedMedication
                    adapter.notifyItemChanged(index)
                }
            }
        }
    }

    companion object {
        private const val ADD_MEDICATION_REQUEST_CODE = 1
        private const val EDIT_MEDICATION_REQUEST_CODE = 2
    }
}
