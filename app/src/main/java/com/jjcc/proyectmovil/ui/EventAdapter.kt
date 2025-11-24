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
        private val time: TextView = itemView.findViewById(R.id.event_time)
        private val location: TextView = itemView.findViewById(R.id.event_location)

        fun bind(event: EventoCalendario) {
            title.text = event.titulo
            time.text = "${event.horaInicio} - ${event.horaFin}"
            location.text = event.ubicacion
        }
    }
}
