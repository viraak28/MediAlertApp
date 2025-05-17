package com.medialert.medinotiapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.databinding.ActivityMainBinding
import com.medialert.medinotiapp.ui.activities.SplashScreenActivity
import com.medialert.medinotiapp.ui.activities.medications.DailyMedicationsActivity
import com.medialert.medinotiapp.ui.activities.medications.MedicationsActivity
import com.medialert.medinotiapp.ui.activities.medications.WeeklyMedicationsActivity
import com.medialert.medinotiapp.ui.activities.reminders.NotiConfigActivity
import com.medialert.medinotiapp.ui.activities.notebooks.NotebooksActivity
import com.medialert.medinotiapp.ui.activities.users.PerfilActivity
import com.medialert.medinotiapp.utils.SessionManager
import com.medialert.medinotiapp.utils.TooltipHelper.showTooltipDialog
//import com.medialert.medinotiapp.utils.TooltipHelper.showCustomTooltip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var medinotiappDatabase: MedinotiappDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        medinotiappDatabase = MedinotiappDatabase.getDatabase(this)

        displayUsername()

        setupNavigationButtons()
        setupWeekButton()
        setupNotebookButton()
    }

    private fun displayUsername() {
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            lifecycleScope.launch(Dispatchers.IO) {
                val usuario = medinotiappDatabase.userDao().getUserById(userId)

                withContext(Dispatchers.Main) {
                    if (usuario != null) {
                        binding.tvUserName.text = "${usuario.nombre} ${usuario.apellido}"
                    } else {
                        binding.tvUserName.text = "USUARIO NO IDENTIFICADO"
                    }
                }
            }
        } else {
            binding.tvUserName.text = "USUARIO NO IDENTIFICADO"
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        sessionManager.clearSession()
        val intent = Intent(this, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupNavigationButtons() {
        binding.btnPerfil.setOnClickListener {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                val intent = Intent(this, PerfilActivity::class.java)
                intent.putExtra("USUARIO_ID", userId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnPerfil.setOnLongClickListener {
            showTooltipDialog(
                context = it.context,
                anchorView = it,
                text = "Datos Perfil"
            )
            true
        }


        binding.btnMedications.setOnClickListener {
            val intent = Intent(this, MedicationsActivity::class.java)
            startActivity(intent)
        }
        binding.btnMedications.setOnLongClickListener {
            showTooltipDialog(
                context = it.context,
                anchorView = it,
                text = "Registro de la medicacion por usuario"
            )
            true
        }

        binding.btnDay.setOnClickListener {
            val intent = Intent(this, DailyMedicationsActivity::class.java)
            startActivity(intent)
        }

        binding.btnDay.setOnLongClickListener {
//            showCustomTooltip(
//                context = this,
//                anchorView = it,
//                text = "Medicación que se debe tomar hoy",
//                textSize = 20f,
//                //backgroundColor = Color.parseColor("#4CAF50")
//                backgroundColor = R.color.white,
//                textColor = R.color.tooltip
//            )
//            true
            showTooltipDialog(
                context = it.context,
                anchorView = it,
                text = "Medicación que se debe tomar hoy"
            )
            true
        }

        binding.btnReminders.setOnClickListener {
            val intent = Intent(this, NotiConfigActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
//            sessionManager.clearSession()
//            val intent = Intent(this, SplashScreenActivity::class.java)
//            startActivity(intent)
//            finish()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Cerrar sesión")
            builder.setMessage("¿Estás seguro de continuar?")
            builder.setPositiveButton("Sí") { _, _ ->
                // Acción si se confirma
                sessionManager.clearSession()
                val intent = Intent(this, SplashScreenActivity::class.java)
                startActivity(intent)
                finish()
                println("Se ha confirmado")
            }
            builder.setNegativeButton("No") { _, _ ->
                // Acción si se cancela
                println("Se ha cancelado")
            }
            builder.show()
        }
    }

    private fun setupWeekButton() {
        val weekRange = getWeekRange()
        binding.txtWeekRange.text = "MEDICACION:"+ weekRange

        binding.btnWeek.setOnClickListener {
            val intent = Intent(this, WeeklyMedicationsActivity::class.java)
            startActivity(intent)
        }
        binding.btnWeek.setOnLongClickListener {
            showTooltipDialog(
                context = it.context,
                anchorView = it,
                text = "Aqui se puede ver la medicacion por dia de la semana actual.\n" +
                        "Tambien puede seleccionar la semana que desee ver. "

            )
            true
        }
    }

    private fun setupNotebookButton() {
        binding.btnNotebook.setOnClickListener {
            val intent = Intent(this, NotebooksActivity::class.java)
            startActivity(intent)
        }
        binding.btnNotebook.setOnLongClickListener {
            showTooltipDialog(
                context = it.context,
                anchorView = it,
                text = "Aqui se anotar lo que necesites realizar seguimiento.\n" +
                        "Puedes crear varias para tenerlo organizado. "

            )
            true
        }
    }

    private fun getWeekRange(): String {
        val calendar = Calendar.getInstance()

        // Establecer el primer día de la semana (lunes)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val startOfWeek = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6) // Sumar 6 días para obtener el último día de la semana
        val endOfWeek = calendar.time

        // Formato de fecha: por ejemplo "07 Abr - 13 Abr"
        val dateFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))
        return "${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}"
    }
}
