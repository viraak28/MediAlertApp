package com.medialert.medinotiapp.ui.activities.users

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.MainActivity
import com.medialert.medinotiapp.data.UserDatabase
import com.medialert.medinotiapp.databinding.ActivityLoginBinding
import com.medialert.medinotiapp.models.User
import com.medialert.medinotiapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userDatabase: UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDatabase = UserDatabase.getDatabase(this)

        val correo = intent.getStringExtra("CORREO")
        if (correo != null) {
            binding.etCorreo.setText(correo)
        }

        binding.btnIniciarSesion.setOnClickListener {
            iniciarSesion()
        }

        binding.btnCambiarContrasena.setOnClickListener {
            cambiarContrasena()
        }

        binding.btnRegistrar.setOnClickListener {
            val intent = Intent(this, RegisterUserActivity::class.java)
            startActivity(intent)
        }
    }

    private fun iniciarSesion() {
        val correo = binding.etCorreo.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString().trim()

        if (correo.isNotEmpty() && contrasena.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val usuario = userDatabase.userDao().getUserByCorreo(correo)
                if (usuario != null && usuario.contrasena == contrasena) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        val sessionManager = SessionManager(this@LoginActivity)
                        sessionManager.saveSession(usuario.id)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cambiarContrasena() {
        val intent = Intent(this, CambiarContrasenaActivity::class.java)
        startActivity(intent)
    }
}
