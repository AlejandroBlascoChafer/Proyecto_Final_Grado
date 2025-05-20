package com.example.proyecto_final_grado.activities.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final_grado.databinding.ActivityLoginBinding
import com.example.proyecto_final_grado.utils.Constants
import com.example.proyecto_final_grado.session.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    private val clientId = Constants.CLIENT_ID
    private val redirectUri = Constants.REDIRECT_URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        binding.loginButton.setOnClickListener {
            val authUri = Uri.parse(Constants.AUTH_URL)
                .buildUpon()
                .appendQueryParameter("client_id", clientId.toString())
                .appendQueryParameter("redirect_uri", redirectUri)
                .appendQueryParameter("response_type", "code")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, authUri)
            startActivity(intent)

        }
    }
}
