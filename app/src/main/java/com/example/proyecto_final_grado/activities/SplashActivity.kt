package com.example.proyecto_final_grado.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.activities.login.LoginActivity
import com.example.proyecto_final_grado.session.SessionManager
import com.example.proyecto_final_grado.viewmodels.SharedViewModel

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)


        sharedViewModel.loadInitialData()

        sharedViewModel.loading.observe(this) { isLoading ->
            if (isLoading == false) {
                if (sessionManager.isLoggedIn()) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

//        Handler(Looper.getMainLooper()).postDelayed({
//            if (sessionManager.isLoggedIn()) {
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            } else {
//                startActivity(Intent(this, LoginActivity::class.java))
//                finish()
//            }
//        }, 2000)



    }
}
