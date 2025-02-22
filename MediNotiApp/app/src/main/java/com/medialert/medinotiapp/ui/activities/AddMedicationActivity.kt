package com.medialert.medinotiapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.medialert.medinotiapp.databinding.ActivityAddMedicationBinding

class AddMedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets to handle status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        setupSaveButton()
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

        if (name.isNotEmpty() && dosage.isNotEmpty() && frequency.isNotEmpty()) {
            val resultIntent = Intent().apply {
                putExtra("NEW_MEDICATION_NAME", name)
                putExtra("NEW_MEDICATION_DOSAGE", dosage)
                putExtra("NEW_MEDICATION_FREQUENCY", frequency)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            showError("Por favor, complete todos los campos")
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
