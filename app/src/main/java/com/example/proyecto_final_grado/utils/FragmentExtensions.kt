package com.example.proyecto_final_grado.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.proyecto_final_grado.activities.MainActivity


fun Fragment.openMediaDetailFragment(mediaID: Int, fragmentProvider: () -> Fragment) {
    val fragment = fragmentProvider().apply {
        arguments = Bundle().apply {
            putInt("MEDIA_ID", mediaID)
        }
    }
    (activity as? MainActivity)?.openDetailFragment(fragment)
}
