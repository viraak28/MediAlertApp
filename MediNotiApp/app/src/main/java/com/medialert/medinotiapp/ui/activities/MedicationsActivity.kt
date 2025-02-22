package com.medialert.medinotiapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.databinding.ActivityMedicationsBinding
import com.medialert.medinotiapp.adapters.MedicationAdapter
import com.medialert.medinotiapp.models.Medication

class MedicationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationsBinding
    private lateinit var recyclerViewMedications: RecyclerView
    private lateinit var fabAddMedication: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMedicationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar vistas
        recyclerViewMedications = findViewById(R.id.recyclerViewMedications)
        fabAddMedication = findViewById(R.id.fabAddMedication)

        // Configurar RecyclerView con un layout manager y adaptador
        val medications = listOf(
            Medication(1, "Paracetamol", "500 mg", "Cada 8 horas"),
            Medication(2, "Ibuprofeno", "200 mg", "Cada 6 horas"),
            Medication(3, "Amoxicilina", "1 g", "Cada 12 horas")
        )

        val adapter = MedicationAdapter(medications) { medication ->
            // Manejar clic en un medicamento (por ejemplo, mostrar detalles)
            showMedicationDetails(medication)
        }

        recyclerViewMedications.layoutManager = LinearLayoutManager(this)
        recyclerViewMedications.adapter = adapter

        // Configurar clic en el botón flotante para agregar medicamentos (aún no implementado)
        fabAddMedication.setOnClickListener {
            // Navegar a AddMedicationActivity (futura implementación)
            addNewMedication()
        }
    }

    private fun showMedicationDetails(medication: Medication) {
        // Crear un Intent para abrir MedicationDetailActivity
        val intent_m = Intent(this, MedicationDetailActivity::class.java).apply {
            // Pasar los datos del medicamento seleccionado a la nueva actividad
            putExtra("MEDICATION_ID", medication.id)
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_DOSAGE", medication.dosage)
        }
        // Iniciar la nueva actividad
        startActivity(intent_m)
    }

    private fun addNewMedication() {
        // Crear un Intent para abrir AddMedicationActivity
        val intent = Intent(this, AddMedicationActivity::class.java)
        // Iniciar la nueva actividad esperando un resultado
        startActivityForResult(intent, ADD_MEDICATION_REQUEST_CODE)
    }

    // Manejar el resultado de AddMedicationActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_MEDICATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Obtener los datos del nuevo medicamento
            val newMedicationName = data?.getStringExtra("NEW_MEDICATION_NAME")
            val newMedicationDosage = data?.getStringExtra("NEW_MEDICATION_DOSAGE")

            // Aquí deberías agregar el nuevo medicamento a tu lista de medicamentos
            // y actualizar el RecyclerView
            // Por ejemplo:
            // val newMedication = Medication(generateNewId(), newMedicationName, newMedicationDosage)
            // medications.add(newMedication)
            // adapter.notifyDataSetChanged()
        }
    }

    companion object {
        private const val ADD_MEDICATION_REQUEST_CODE = 1
    }
}


