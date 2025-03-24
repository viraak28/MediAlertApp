package com.medialert.medinotiapp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medialert.medinotiapp.MainActivity
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.data.UserDatabase
import com.medialert.medinotiapp.ui.activities.users.LoginActivity
import com.medialert.medinotiapp.ui.activities.users.RegisterUserActivity
import com.medialert.medinotiapp.ui.activities.users.UserSelectionActivity
import com.medialert.medinotiapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var userDatabase: UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        sessionManager = SessionManager(this)
        userDatabase = UserDatabase.getDatabase(this)

        if (sessionManager.isUserLoggedIn()) {
            // Acceder directamente a los datos de la sesión
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                userDatabase.userDao().getAll().collect { users ->
                    if (users.isEmpty()) {
                        // La lista está vacía
                        runOnUiThread {
                            // Mostrar pantalla de registro
                            val intent = Intent(this@SplashScreenActivity, RegisterUserActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        // La lista no está vacía
                        runOnUiThread {
                            if (users.size == 1) {
                                // Mostrar pantalla de inicio de sesión para un solo usuario
                                val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
                                intent.putExtra("CORREO", users[0].correo)
                                startActivity(intent)
                                finish()
                            } else {
                                // Mostrar lista de usuarios para seleccionar
                                val intent = Intent(this@SplashScreenActivity, UserSelectionActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}
