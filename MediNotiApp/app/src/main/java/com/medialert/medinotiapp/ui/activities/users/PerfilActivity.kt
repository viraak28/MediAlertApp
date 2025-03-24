package com.medialert.medinotiapp.ui.activities.users
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityPerfilBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var medinotiappDatabase: MedinotiappDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        medinotiappDatabase = MedinotiappDatabase.getDatabase(this)

        val usuarioId = intent.getIntExtra("USUARIO_ID", -1)

        lifecycleScope.launch(Dispatchers.IO) {
            val usuario = medinotiappDatabase.userDao().getUserById(usuarioId)
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
