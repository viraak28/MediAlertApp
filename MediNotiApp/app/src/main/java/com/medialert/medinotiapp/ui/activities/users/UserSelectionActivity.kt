package com.medialert.medinotiapp.ui.activities.users

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.medialert.medinotiapp.R
import com.medialert.medinotiapp.data.MedinotiappDatabase
import com.medialert.medinotiapp.adapters.UserAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var medinotiappDatabase: MedinotiappDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)

        recyclerView = findViewById(R.id.rvUsers)
        medinotiappDatabase = MedinotiappDatabase.getDatabase(this)

        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            medinotiappDatabase.userDao().getAll().collectLatest { users ->
                val adapter = UserAdapter(users) { user ->
                    val intent = Intent(this@UserSelectionActivity, LoginActivity::class.java)
                    intent.putExtra("CORREO", user.correo)
                    startActivity(intent)
                }
                recyclerView.adapter = adapter
            }
        }

        findViewById<View>(R.id.btnRegisterNewUser).setOnClickListener {
            val intent = Intent(this, RegisterUserActivity::class.java)
            startActivity(intent)
        }
    }
}

