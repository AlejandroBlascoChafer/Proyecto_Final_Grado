package com.example.proyecto_final_grado.models

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

data class WeeklyScheduleEntry(
    val id: Int,
    val dayOfWeek: DayOfWeek,
    val title: String,
    val episode: Int,
    val time: LocalTime,
    val imageUrl: String?  // Nueva propiedad para la imagen
)


sealed class WeeklyScheduleItem {
    data class Header(val day: DayOfWeek) : WeeklyScheduleItem()
    data class Episode(val entry: WeeklyScheduleEntry) : WeeklyScheduleItem()
}
