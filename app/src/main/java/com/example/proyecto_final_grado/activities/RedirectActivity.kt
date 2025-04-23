package com.example.proyecto_final_grado.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_final_grado.utils.Constants
import com.example.proyecto_final_grado.utils.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RedirectActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        val code = intent?.data?.getQueryParameter("code")
        if (code != null) {
            exchangeCodeForToken(code)
        } else {
            Toast.makeText(this, "No se pudo obtener el código de autorización", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun exchangeCodeForToken(code: String) {
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", Constants.CLIENT_ID.toString())
            .add("client_secret", Constants.CLIENT_SECRET)
            .add("redirect_uri", Constants.REDIRECT_URI)
            .add("code", code)
            .build()

        val request = Request.Builder()
            .url(Constants.TOKEN_URL) // debería ser https://anilist.co/api/v2/oauth/token
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RedirectActivity, "Error de red", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string() ?: ""
                Log.d("RedirectActivity", "Response: $responseString")

                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(responseString)
                        if (json.has("access_token")) {
                            val accessToken = json.getString("access_token")
                            sessionManager.saveAccessToken(accessToken)

                            startActivity(Intent(this@RedirectActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Log.e("RedirectActivity", "No access_token found in response")
                            runOnUiThread {
                                Toast.makeText(this@RedirectActivity, "Error: access_token no encontrado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("RedirectActivity", "Error parsing response: ${e.message}")
                        runOnUiThread {
                            Toast.makeText(this@RedirectActivity, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("RedirectActivity", "Error ${response.code}")
                    Log.e("RedirectActivity", "Cuerpo: $responseString")
                    runOnUiThread {
                        Toast.makeText(this@RedirectActivity, "Error autenticando", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
