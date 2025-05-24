package com.example.proyecto_final_grado.adapters.homeAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.listeners.OnAnimeClickListener
import com.example.proyecto_final_grado.models.WeeklyScheduleItem
import com.squareup.picasso.Picasso
import org.threeten.bp.format.TextStyle
import java.util.Locale
import org.threeten.bp.format.DateTimeFormatter


class WeeklyScheduleAdapter(private val listenerAnime: OnAnimeClickListener) : ListAdapter<WeeklyScheduleItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_EPISODE = 1
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is WeeklyScheduleItem.Header -> TYPE_HEADER
            is WeeklyScheduleItem.Episode -> TYPE_EPISODE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_weekly_schedule_episode, parent, false)
            EpisodeViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is WeeklyScheduleItem.Header -> (holder as HeaderViewHolder).bind(item)
            is WeeklyScheduleItem.Episode -> (holder as EpisodeViewHolder).bind(item)
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: WeeklyScheduleItem.Header) {
            val dayName = item.day.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val capitalized = dayName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            (itemView as TextView).text = capitalized
        }
    }

    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageEpisode)
        private val titleText = itemView.findViewById<TextView>(R.id.textTitle)
        private val episodeTimeText = itemView.findViewById<TextView>(R.id.textEpisodeTime)

        fun bind(item: WeeklyScheduleItem.Episode) {
            val entry = item.entry
            titleText.text = entry.title
            episodeTimeText.text = "Episode ${entry.episode} at ${entry.time.format(timeFormatter)}"

            if (!entry.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(entry.imageUrl)
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.holo_red_light)
                    .into(imageView)
            } else {
                imageView.setImageResource(android.R.color.darker_gray)
            }
            itemView.setOnClickListener {
                listenerAnime.onAnimeClick(item.entry.id)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WeeklyScheduleItem>() {
        override fun areItemsTheSame(old: WeeklyScheduleItem, new: WeeklyScheduleItem): Boolean {
            return if (old is WeeklyScheduleItem.Header && new is WeeklyScheduleItem.Header) {
                old.day == new.day
            } else if (old is WeeklyScheduleItem.Episode && new is WeeklyScheduleItem.Episode) {
                old.entry.dayOfWeek == new.entry.dayOfWeek &&
                        old.entry.title == new.entry.title &&
                        old.entry.episode == new.entry.episode
            } else false
        }

        override fun areContentsTheSame(old: WeeklyScheduleItem, new: WeeklyScheduleItem): Boolean {
            return old == new
        }
    }
}

