package com.medialert.medinotiapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medialert.medinotiapp.adapters.TakeAdapter
import com.medialert.medinotiapp.data.MedicationDatabase
import com.medialert.medinotiapp.databinding.ActivityMedicationDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationDetailBinding
    private lateinit var medicationDatabase: MedicationDatabase
    private var medicationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medicationDatabase = MedicationDatabase.getDatabase(this)

        // Apply window insets to handle status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // Obtener datos del intent
        medicationId = intent.getIntExtra("MEDICATION_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: ""
        val medicationDosage = intent.getStringExtra("MEDICATION_DOSAGE") ?: ""
        val medicationFrequency = intent.getStringExtra("MEDICATION_FREQUENCY") ?: ""
        val medicationAdministrationType  = intent.getStringExtra( "MEDICATION_AdministrationType") ?: ""
        val medicationDosageQuantity  = intent.getStringExtra( "MEDICATION_dosageQuantity") ?: ""
        // Mostrar datos en las vistas
        binding.tvMedicationName.text = "Nombre: $medicationName"
        binding.tvMedicationDosage.text = "Dosis: $medicationDosage"
        binding.tvMedicationFrequency.text = "Frecuencia: $medicationFrequency"
        binding.tvMedicationDosageQuantity.text = "Cantidad : $medicationDosageQuantity $medicationAdministrationType"
        // Configurar RecyclerView y mostrar las tomas
        setupRecyclerView()
        loadTakes()
    }

    private fun setupRecyclerView() {
        binding.rvTakes.layoutManager = LinearLayoutManager(this)
    }

    private fun loadTakes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val takes = medicationDatabase.medicationDao().getTakesForMedication(medicationId).collect{
                withContext(Dispatchers.Main){
                    binding.rvTakes.adapter = TakeAdapter(it)
                }
            }
        }
    }
}
