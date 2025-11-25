package com.jjcc.proyectmovil.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.model.EventoCalendario

/**
 * Adapter for the list of events of a selected day.
 */
class EventAdapter : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events: List<EventoCalendario> = emptyList()

    fun submitList(newEvents: List<EventoCalendario>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.event_title)
        private val subtitle: TextView = itemView.findViewById(R.id.event_subtitle)
        private val time: TextView = itemView.findViewById(R.id.event_time)
        private val strip: View = itemView.findViewById(R.id.color_strip)
        private val icon: android.widget.ImageView = itemView.findViewById(R.id.event_icon)

        fun bind(event: EventoCalendario) {
            title.text = event.titulo
            subtitle.text = event.ubicacion
            time.text = "${event.horaInicio} - ${event.horaFin}"

            // Dynamic styling
            val colors = listOf(
                R.color.brand_primary,
                R.color.accent_orange,
                R.color.success
            )
            val colorRes = colors[adapterPosition % colors.size]
            val color = itemView.context.getColor(colorRes)

            strip.setBackgroundColor(color)
            icon.setColorFilter(color)

            val iconRes = when {
                event.titulo.contains("Clase", true) -> R.drawable.ic_school
                event.titulo.contains("Reunión", true) -> R.drawable.ic_calendar
                event.titulo.contains("Entrega", true) -> R.drawable.ic_tareas
                else -> R.drawable.ic_calendar
            }
            icon.setImageResource(iconRes)
        }
    }
}
