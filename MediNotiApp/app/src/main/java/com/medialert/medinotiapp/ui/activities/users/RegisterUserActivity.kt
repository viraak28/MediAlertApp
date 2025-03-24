package com.medialert.medinotiapp.ui.activities.users

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityUserRegisterBinding
import com.medialert.medinotiapp.models.User
import com.medialert.medinotiapp.ui.activities.SplashScreenActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserRegisterBinding
    private lateinit var medinotiappDatabase: MedinotiappDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medinotiappDatabase = MedinotiappDatabase.getDatabase(this)

        binding.btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.etNombre.text.toString().trim()
        val apellido = binding.etApellido.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString().trim()
        val confirmarContrasena = binding.etConfirmarContrasena.text.toString().trim()

        if (nombre.isNotEmpty() && apellido.isNotEmpty() && correo.isNotEmpty() && contrasena.isNotEmpty() && confirmarContrasena.isNotEmpty()) {
            if (contrasena == confirmarContrasena) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val usuario = User(nombre = nombre, apellido = apellido, correo = correo, contrasena = contrasena)
                    medinotiappDatabase.userDao().insert(usuario)

                    runOnUiThread {
                        Toast.makeText(this@RegisterUserActivity, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterUserActivity, SplashScreenActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }
}
