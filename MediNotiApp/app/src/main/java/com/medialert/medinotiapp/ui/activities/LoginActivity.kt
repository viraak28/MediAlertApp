package com.medialert.medinotiapp.ui.activities
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.data.UserDatabase
import com.medialert.medinotiapp.databinding.ActivityLoginBinding
import com.medialert.medinotiapp.models.User
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

        binding.btnIniciarSesion.setOnClickListener {
            iniciarSesion()
        }

        binding.btnCambiarContrasena.setOnClickListener {
            cambiarContrasena()
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
                        // Iniciar sesión y mostrar el perfil del usuario
                        mostrarPerfilUsuario(usuario)
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

    private fun mostrarPerfilUsuario(usuario: User) {
        val intent = Intent(this, PerfilActivity::class.java)
        intent.putExtra("USUARIO_ID", usuario.id)
        startActivity(intent)
    }
}
