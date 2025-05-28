package com.example.proyecto_final_grado.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Optional
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.DialogScoreBinding
import com.example.proyecto_final_grado.databinding.FragmentEditListEntryBinding
import com.example.proyecto_final_grado.listeners.OnScoreClickListener
import com.example.proyecto_final_grado.models.EditListEntryItem
import com.example.proyecto_final_grado.viewmodels.SharedViewModel
import graphql.GetUserProfileInfoQuery
import graphql.UpdateEntryListMutation
import graphql.UpdateFavouriteMutation
import graphql.type.FuzzyDateInput
import graphql.type.MediaListStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

class EditListEntryFragment : Fragment(), OnScoreClickListener {
    private var _binding: FragmentEditListEntryBinding? = null
    private val binding get() = _binding!!

    private lateinit var apolloClient: ApolloClient

    private var mediaEntry: EditListEntryItem? = null

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apolloClient = ApolloClientProvider.getApolloClient(requireContext())
        mediaEntry = arguments?.getParcelable("entry")
        mediaEntry?.let { loadInitialData(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditListEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun loadInitialData(entry: EditListEntryItem) {

        binding.mediaTitle.text = entry.title

        val statusOptions = listOf("CURRENT", "COMPLETED", "DROPPED", "PAUSED", "PLANNING")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter
        binding.spinnerStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position) as String
                if (selected == "COMPLETED") {
                    val today = org.threeten.bp.LocalDate.now()
                    binding.buttonEndDate.text = today.toString()
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        val statusIndex = statusOptions.indexOf(entry.status)
        if (statusIndex != -1) binding.spinnerStatus.setSelection(statusIndex)

        binding.textViewScore.text = entry.score.toString()
        binding.textViewScore.setOnClickListener{
            entry.score?.let { it1 -> onScoreClick(it1, entry.mediaID, entry.status, binding.textViewScore) }
        }

        binding.editTextReview.setText(entry.review)

        binding.switchFavorite.isChecked = entry.favourite == true

        val startLocalDate = Instant.ofEpochMilli(entry.startDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        binding.buttonStartDate.text = startLocalDate.toString()
        binding.buttonStartDate.setOnClickListener {
            showDatePickerDialog(binding.buttonStartDate)
        }

        val endLocalDate = Instant.ofEpochMilli(entry.endDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        if (endLocalDate.year == 0){
            binding.buttonEndDate.text = "Not established"
        } else {
            binding.buttonEndDate.text = endLocalDate.toString()
        }
        binding.buttonEndDate.setOnClickListener {
            showDatePickerDialog(binding.buttonEndDate)
        }


        binding.numberPickerRewatches.maxValue = 99
        binding.numberPickerRewatches.minValue = 0
        binding.numberPickerRewatches.value = entry.rewatches!!

        binding.switchPrivate.isChecked = entry.private == true
        binding.switchHidden.isChecked = entry.hideFromList == true


        binding.buttonSave.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val selectedStatus = when (binding.spinnerStatus.selectedItem as String) {
                        "CURRENT" -> MediaListStatus.CURRENT
                        "COMPLETED" -> MediaListStatus.COMPLETED
                        "DROPPED" -> MediaListStatus.DROPPED
                        "PAUSED" -> MediaListStatus.PAUSED
                        "PLANNING" -> MediaListStatus.PLANNING
                        else -> MediaListStatus.CURRENT
                    }

                    // Función auxiliar para parsear fecha de botón a FuzzyDateInput
                    fun parseDateToFuzzyDateInput(dateString: String): FuzzyDateInput {
                        if (dateString == "Not established") return FuzzyDateInput(Optional.absent(),Optional.absent(),Optional.absent())
                        val parts = dateString.split("-")
                        return FuzzyDateInput(
                            year = Optional.present(parts.getOrNull(0)?.toIntOrNull()),
                            month = Optional.present(parts.getOrNull(1)?.toIntOrNull()),
                            day = Optional.present(parts.getOrNull(2)?.toIntOrNull())
                        )
                    }

                    val startedAtInput = parseDateToFuzzyDateInput(binding.buttonStartDate.text.toString())
                    val completedAtInput = parseDateToFuzzyDateInput(binding.buttonEndDate.text.toString())

                    val scoreValue = binding.textViewScore.text.toString().toDoubleOrNull() ?: 0.0

                    val response = apolloClient.mutation(UpdateEntryListMutation(
                        saveMediaListEntryId = entry.mediaListEntryId,
                        mediaId = entry.mediaID,
                        status = selectedStatus,
                        score = scoreValue,
                        repeat = binding.numberPickerRewatches.value,
                        private = binding.switchPrivate.isChecked,
                        hiddenFromStatusLists = binding.switchHidden.isChecked,
                        startedAt = startedAtInput,
                        completedAt = completedAtInput,
                        notes = binding.editTextReview.text.toString()
                    )).execute()

                    if (entry.type == "ANIME"){
                        apolloClient.mutation(UpdateFavouriteMutation(Optional.present(entry.mediaID), Optional.absent(), Optional.absent(), Optional.absent(), Optional.absent())).execute()
                    } else {
                        apolloClient.mutation(UpdateFavouriteMutation(Optional.absent(), Optional.present(entry.mediaID), Optional.absent(), Optional.absent(), Optional.absent())).execute()
                    }




                    if (selectedStatus == MediaListStatus.COMPLETED && binding.buttonEndDate.text == "Not established") {
                        val today = org.threeten.bp.LocalDate.now()
                        binding.buttonEndDate.text = today.toString()
                    }


                    if (!response.hasErrors()) {

                        sharedViewModel.loadInitialData()

                        Toast.makeText(requireContext(), "Entry saved", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception){
                    Log.e("ResponseError", e.toString())
                }
            }
        }


    }

    private fun showDatePickerDialog(button: Button) {
        val currentDateText = button.text.toString()
        val initialDate = try {
            org.threeten.bp.LocalDate.parse(currentDateText)
        } catch (e: Exception) {
            org.threeten.bp.LocalDate.now()
        }

        val datePicker = android.app.DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = org.threeten.bp.LocalDate.of(year, month + 1, dayOfMonth)
                button.text = selectedDate.toString()
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        )
        datePicker.show()
    }


    @SuppressLint("SetTextI18n")
    override fun onScoreClick(score: Double, mediaId: Int, status: String, scoreText: TextView) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_score, null)
        val binding = DialogScoreBinding.bind(dialogView)
        var scoreFormat = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: ApolloResponse<GetUserProfileInfoQuery.Data> = apolloClient.query(
                    GetUserProfileInfoQuery()
                ).execute()
                scoreFormat = response.data?.Viewer?.mediaListOptions?.scoreFormat.toString()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Error", "${e.message}")
                }
            }

            withContext(Dispatchers.Main){
                val (max, _) = when (scoreFormat) {
                    "POINT_100" -> 100 to 1
                    "POINT_10_DECIMAL" -> 100 to 1
                    "POINT_10" -> 10 to 1
                    "POINT_5" -> 5 to 1
                    "POINT_3" -> 3 to 1
                    else -> 100 to 1
                }

                binding.seekBarScore.max = max

                binding.seekBarScore.progress = when (scoreFormat) {
                    "POINT_10_DECIMAL" -> (score * 10).toInt()
                    else -> score.toInt()
                }


                fun formatScore(progress: Int): String = when (scoreFormat) {
                    "POINT_10_DECIMAL" -> "%.1f".format(progress / 10f)
                    else -> progress.toString()
                }

                binding.tvCurrentScore.text = "Score: ${formatScore(binding.seekBarScore.progress)}"

                binding.seekBarScore.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        binding.tvCurrentScore.text = "Score: ${formatScore(progress)}"
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                AlertDialog.Builder(context)
                    .setTitle("Change Score")
                    .setView(binding.root)
                    .setPositiveButton("OK") { _, _ ->
                        val value = binding.seekBarScore.progress
                        val finalScore = when (scoreFormat) {
                            "POINT_10_DECIMAL" -> value / 10f
                            else -> value.toFloat()
                        }
                        scoreText.text = finalScore.toString()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }


    }

}