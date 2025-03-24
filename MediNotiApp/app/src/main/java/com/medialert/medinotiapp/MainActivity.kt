package com.medialert.medinotiapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.medialert.medinotiapp.databinding.ActivityMainBinding
import com.medialert.medinotiapp.ui.activities.SplashScreenActivity
import com.medialert.medinotiapp.ui.activities.medications.MedicationsActivity
import com.medialert.medinotiapp.ui.activities.users.PerfilActivity
import com.medialert.medinotiapp.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        binding.btnPerfil.setOnClickListener {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                val intent = Intent(this, PerfilActivity::class.java)
                intent.putExtra("USUARIO_ID", userId)
                startActivity(intent)
            } else {
                // Maneja el caso en que no hay sesión activa
                Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMedications.setOnClickListener {
            val intent = Intent(this, MedicationsActivity::class.java)
            startActivity(intent)
        }

        binding.btnReminders.setOnClickListener {
            // TODO: Implement reminders functionality
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, SplashScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
