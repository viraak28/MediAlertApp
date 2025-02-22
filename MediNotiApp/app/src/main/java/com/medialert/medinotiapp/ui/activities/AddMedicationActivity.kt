package com.medialert.medinotiapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.medialert.medinotiapp.databinding.ActivityAddMedicationBinding

class AddMedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveMedication.setOnClickListener {
            saveMedication()
        }
    }

    private fun saveMedication() {
        val name = binding.etMedicationName.text.toString()
        val dosage = binding.etMedicationDosage.text.toString()

        if (name.isNotEmpty() && dosage.isNotEmpty()) {
            val resultIntent = Intent().apply {
                putExtra("NEW_MEDICATION_NAME", name)
                putExtra("NEW_MEDICATION_DOSAGE", dosage)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            // Mostrar un mensaje de error si los campos están vacíos
            // Por ejemplo, usando un Snackbar:
            // Snackbar.make(binding.root, "Please fill all fields", Snackbar.LENGTH_SHORT).show()
        }
    }
}
