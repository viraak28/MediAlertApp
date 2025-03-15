package com.medialert.medinotiapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.medialert.medinotiapp.databinding.ActivityMainBinding
import com.medialert.medinotiapp.ui.activities.MedicationsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    //Sobrescribir la funcion del padre
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //lazar el layout definido en activity_main
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMedicationButtons()
        setupNavigationButtons()
    }

    private fun setupMedicationButtons() {
        //On funcinan con una accion
        binding.btnBreakfastMeds.setOnClickListener {
            // TODO: Implement logic for showing breakfast medications
        }

        binding.btnLunchMeds.setOnClickListener {
            // TODO: Implement logic for showing lunch medications
        }

        binding.btnDinnerMeds.setOnClickListener {
            // TODO: Implement logic for showing dinner medications
        }

    }

    private fun setupNavigationButtons() {
        binding.btnPastillero.setOnClickListener {
            // TODO: Implement pill organizer functionality
        }

        binding.btnMedications.setOnClickListener {
            val intent = Intent(this, MedicationsActivity::class.java)
            startActivity(intent)
        }

        binding.btnReminders.setOnClickListener {
            // TODO: Implement reminders functionality
        }
    }
}
