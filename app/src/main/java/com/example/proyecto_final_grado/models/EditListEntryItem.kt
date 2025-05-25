package com.example.proyecto_final_grado.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EditListEntryItem(
    val mediaListEntryId: Int,
    val mediaID: Int,
    val title: String?,
    val status: String,
    val score: Double?,
    val episode: Int?,
    val startDate: Long,
    val endDate: Long,
    val rewatches: Int?,
    val review: String?,
    val private: Boolean?,
    val hideFromList: Boolean?,
    val favourite: Boolean?,
    val type: String
) : Parcelable
