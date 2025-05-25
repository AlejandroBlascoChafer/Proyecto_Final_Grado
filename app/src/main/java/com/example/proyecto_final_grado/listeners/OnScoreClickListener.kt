package com.example.proyecto_final_grado.listeners

import android.widget.TextView

interface OnScoreClickListener {
    fun onScoreClick(score: Double, mediaId: Int, status: String, scoreText: TextView)
}