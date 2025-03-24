package com.medialert.medinotiapp.ui.activities.users
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityCambiarContrasenaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CambiarContrasenaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCambiarContrasenaBinding
    private lateinit var medinotiappDatabase: MedinotiappDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCambiarContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medinotiappDatabase = MedinotiappDatabase.getDatabase(this)

        binding.btnCambiarContrasena.setOnClickListener {
            cambiarContrasena()
        }
    }

    private fun cambiarContrasena() {
        val correo = binding.etCorreo.text.toString().trim()
        val contrasenaActual = binding.etContrasenaActual.text.toString().trim()
        val nuevaContrasena = binding.etNuevaContrasena.text.toString().trim()
        val confirmarNuevaContrasena = binding.etConfirmarNuevaContrasena.text.toString().trim()

        if (correo.isNotEmpty() && contrasenaActual.isNotEmpty() && nuevaContrasena.isNotEmpty() && confirmarNuevaContrasena.isNotEmpty()) {
            if (nuevaContrasena == confirmarNuevaContrasena) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val usuario = medinotiappDatabase.userDao().getUserByCorreo(correo)
                    if (usuario != null && usuario.contrasena == contrasenaActual) {
                        usuario.contrasena = nuevaContrasena
                        medinotiappDatabase.userDao().update(usuario)

                        runOnUiThread {
                            Toast.makeText(this@CambiarContrasenaActivity, "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@CambiarContrasenaActivity, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Las nuevas contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }
}
