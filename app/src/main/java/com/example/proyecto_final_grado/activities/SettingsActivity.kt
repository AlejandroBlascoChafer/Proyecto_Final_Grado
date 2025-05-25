package com.example.proyecto_final_grado.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.apolloStore
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.exception.ApolloHttpException
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.ActivitySettingsBinding
import com.example.proyecto_final_grado.viewmodels.SharedViewModel
import graphql.GetUserOptionsQuery
import graphql.UpdateUserInfoMutation
import graphql.type.ScoreFormat
import graphql.type.UserStaffNameLanguage
import graphql.type.UserTitleLanguage
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var apolloClient: ApolloClient
    private lateinit var sharedViewModel: SharedViewModel


    private val titleLanguageOptions = listOf("ROMAJI", "ENGLISH", "NATIVE")
    private val scoreFormatOptions = listOf("POINT_10", "POINT_100", "POINT_10_DECIMAL", "POINT_5", "POINT_3")
    private val staffNameLanguageOptions = listOf("NATIVE", "ROMAJI", "ROMAJI_WESTERN")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]


        apolloClient = ApolloClientProvider.getApolloClient(this)

        binding.spinnerTitleLanguage.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titleLanguageOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerScoreFormat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, scoreFormatOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerStaffNameLanguage.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, staffNameLanguageOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.buttonSaveSettings.setOnClickListener { saveSettings() }

        lifecycleScope.launch {
            try {
                val response = apolloClient.query(GetUserOptionsQuery()).execute()
                val userOptions = response.data?.Viewer

                if (userOptions != null) {
                    loadCurrentSettings(userOptions)
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error loading settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCurrentSettings(viewer: GetUserOptionsQuery.Viewer) {
        val titleLanguage = viewer.options?.titleLanguage
        Log.d("SettingsActivity", "Loading settings: titleLanguage=$titleLanguage")

        val titleLangIndex = when (titleLanguage) {
            UserTitleLanguage.ROMAJI -> 0
            UserTitleLanguage.ENGLISH -> 1
            UserTitleLanguage.NATIVE -> 2
            else -> 0
        }
        // Forzar selección con animación (true)
        binding.spinnerTitleLanguage.setSelection(titleLangIndex, true)

        binding.switchAdultContent.isChecked = viewer.options?.displayAdultContent == true

        val scoreFormatIndex = when (viewer.mediaListOptions?.scoreFormat) {
            ScoreFormat.POINT_10 -> 0
            ScoreFormat.POINT_100 -> 1
            ScoreFormat.POINT_10_DECIMAL -> 2
            ScoreFormat.POINT_5 -> 3
            ScoreFormat.POINT_3 -> 4
            else -> 0
        }
        binding.spinnerScoreFormat.setSelection(scoreFormatIndex)

        val staffLangIndex = when (viewer.options?.staffNameLanguage) {
            UserStaffNameLanguage.NATIVE -> 0
            UserStaffNameLanguage.ROMAJI -> 1
            UserStaffNameLanguage.ROMAJI_WESTERN -> 2
            else -> 0
        }
        binding.spinnerStaffNameLanguage.setSelection(staffLangIndex)

        binding.switchAiringNotifications.isChecked = viewer.options?.airingNotifications == true
    }

    private fun saveSettings() {
        val titleLanguageEnum = UserTitleLanguage.safeValueOf(binding.spinnerTitleLanguage.selectedItem.toString())
        val scoreFormatEnum = ScoreFormat.safeValueOf(binding.spinnerScoreFormat.selectedItem.toString())
        val staffNameEnum = UserStaffNameLanguage.safeValueOf(binding.spinnerStaffNameLanguage.selectedItem.toString())
        val displayAdult = binding.switchAdultContent.isChecked
        val airingNotifications = binding.switchAiringNotifications.isChecked

        lifecycleScope.launch {
            try {

                Log.d("SettingsActivity", "Sending mutation with titleLanguage=$titleLanguageEnum, displayAdultContent=$displayAdult, scoreFormat=$scoreFormatEnum, staffNameLanguage=$staffNameEnum, airingNotifications=$airingNotifications")
                val response = apolloClient.mutation(
                    UpdateUserInfoMutation(
                        titleLanguage = titleLanguageEnum,
                        displayAdultContent = displayAdult,
                        scoreFormat = scoreFormatEnum,
                        staffNameLanguage = staffNameEnum,
                        airingNotifications = airingNotifications
                    )
                ).fetchPolicy(FetchPolicy.NetworkFirst).execute()

                Log.d("SettingsActivity", "Mutation response: $response")
                if (!response.hasErrors()) {
                    Log.d("SettingsActivity", "Settings updated successfully.")
                    Toast.makeText(this@SettingsActivity, "Settings updated", Toast.LENGTH_SHORT).show()


                    val intent = Intent(this@SettingsActivity, SplashActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    Log.e("SettingsActivity", "Mutation errors: ${response.errors}")
                }
            } catch (e: Exception) {
                Log.e("SettingsActivity", "Mutation exception", e)
            }
        }
    }


}
