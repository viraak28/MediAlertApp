package com.medialert.medinotiapp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.medialert.medinotiapp.databinding.ActivityMainBinding
import com.medialert.medinotiapp.ui.activities.MedicationsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
//Sobrescribir la funcion del padre
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//lazar el layout definido en activity_main
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//On funcinan con una accion
        binding.btnOpenMedications.setOnClickListener {
            val intent = Intent(this, MedicationsActivity::class.java)
            startActivity(intent)
        }

        binding.checkBox.setOnClickListener {
                val intent1 = Intent(/* packageContext = */ this, /* cls = */ MedicationsActivity::class.java)
                startActivity(intent1)
        }

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}


