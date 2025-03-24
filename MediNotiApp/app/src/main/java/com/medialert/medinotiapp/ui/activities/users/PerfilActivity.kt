package com.medialert.medinotiapp.ui.activities.users
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.data.UserDatabase
import com.medialert.medinotiapp.databinding.ActivityPerfilBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var userDatabase: UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDatabase = UserDatabase.getDatabase(this)

        val usuarioId = intent.getIntExtra("USUARIO_ID", -1)

        lifecycleScope.launch(Dispatchers.IO) {
            val usuario = userDatabase.userDao().getUserById(usuarioId)
            if (usuario != null) {
                runOnUiThread {
                    binding.tvNombre.text = usuario.nombre
                    binding.tvApellido.text = usuario.apellido
                    binding.tvCorreo.text = usuario.correo
                }
            }
        }
    }
}
