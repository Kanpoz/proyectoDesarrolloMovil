package com.jjcc.proyectmovil.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.model.EventoCalendario
import java.time.LocalDate

/**
 * Adapter for the calendar grid. Displays each day of the month and indicates if there are events.
 */
class DayAdapter(
    private val onDayClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    private var days: List<DayItem> = emptyList()
    private var selectedDate: LocalDate? = null

    fun submitList(newDays: List<DayItem>, selected: LocalDate?) {
        days = newDays
        selectedDate = selected
        notifyDataSetChanged()
    }

    fun updateEventDays(eventDates: List<LocalDate>) {
        days = days.map { dayItem ->
            if (dayItem.date != null && eventDates.contains(dayItem.date)) {
                dayItem.copy(hasEvent = true)
            } else {
                dayItem.copy(hasEvent = false)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = days[position]
        holder.bind(item, selectedDate)
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayNumber: TextView = itemView.findViewById(R.id.day_number)
        private val eventIndicator: View = itemView.findViewById(R.id.event_indicator)

        fun bind(item: DayItem, selected: LocalDate?) {
            if (item.date == null) {
                dayNumber.text = ""
                itemView.isClickable = false
                eventIndicator.visibility = View.GONE
                dayNumber.backgroundTintList = null
            } else {
                dayNumber.text = item.date.dayOfMonth.toString()
                itemView.isClickable = true
                eventIndicator.visibility = if (item.hasEvent) View.VISIBLE else View.GONE

                val isSelected = item.date == selected
                if (isSelected) {
                    dayNumber.setTextColor(itemView.context.getColor(R.color.white))
                    dayNumber.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        itemView.context.getColor(R.color.brand_primary)
                    )
                } else {
                    dayNumber.setTextColor(itemView.context.getColor(R.color.text_primary))
                    dayNumber.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        itemView.context.getColor(android.R.color.transparent)
                    )
                }

                itemView.setOnClickListener {
                    onDayClick(item.date)
                }
            }
        }
    }
}
