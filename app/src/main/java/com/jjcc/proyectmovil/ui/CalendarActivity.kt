package com.jjcc.proyectmovil.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.model.EventoCalendario
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var dayAdapter: DayAdapter
    private lateinit var eventAdapter: EventAdapter

    // These would normally be provided by authentication / intent extras
    private val userId: String = "<USER_ID>"
    private val userRole: String = "DOCENTE" // or "ESTUDIANTE"

    private val firestore = FirebaseFirestore.getInstance()

    private var currentYearMonth: YearMonth = YearMonth.now()
    private var selectedDate: LocalDate = LocalDate.now()
    private var allEvents: List<EventoCalendario> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarRecyclerView = findViewById(R.id.recycler_calendar)
        eventsRecyclerView = findViewById(R.id.recycler_events)

        dayAdapter = DayAdapter { date -> onDaySelected(date) }
        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)
        calendarRecyclerView.adapter = dayAdapter

        eventAdapter = EventAdapter()
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventsRecyclerView.adapter = eventAdapter

        loadCalendar()
    }

    /** Load days of the current month and fetch events from Firestore */
    private fun loadCalendar() {
        // Build list of DayItem for the month
        val days = mutableListOf<DayItem>()
        val firstOfMonth = currentYearMonth.atDay(1)
        val dayOfWeekOffset = firstOfMonth.dayOfWeek.value % 7 // Monday=1 ... Sunday=7, we want 0..6 offset
        // Fill preceding empty cells
        for (i in 0 until dayOfWeekOffset) {
            days.add(DayItem(null, false))
        }
        val daysInMonth = currentYearMonth.lengthOfMonth()
        for (day in 1..daysInMonth) {
            val date = currentYearMonth.atDay(day)
            days.add(DayItem(date, false)) // event flag will be updated after fetching
        }
        dayAdapter.submitList(days)
        fetchEventsForMonth()
    }

    /** Query Firestore for both collections, merge them, and update UI */
    private fun fetchEventsForMonth() {
        val start = currentYearMonth.atDay(1).atStartOfDay()
        val end = currentYearMonth.atEndOfMonth().atTime(23, 59, 59)

        // Helper to convert Timestamp to LocalDate
        val toLocalDate = { ts: com.google.firebase.Timestamp -> ts.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() }

        // Query clases collection based on role
        val clasesQuery = when (userRole) {
            "DOCENTE" -> firestore.collection("clases")
                .whereEqualTo("docenteId", userId)
                .whereGreaterThanOrEqualTo("fecha", start)
                .whereLessThanOrEqualTo("fecha", end)
            "ESTUDIANTE" -> firestore.collection("clases")
                .whereArrayContains("estudiantes", userId)
                .whereGreaterThanOrEqualTo("fecha", start)
                .whereLessThanOrEqualTo("fecha", end)
            else -> firestore.collection("clases")
        }

        // Query eventosEscolares collection (global events)
        val eventosQuery = firestore.collection("eventosEscolares")
            .whereGreaterThanOrEqualTo("fecha", start)
            .whereLessThanOrEqualTo("fecha", end)

        // Execute both queries in parallel and combine results
        clasesQuery.get().addOnSuccessListener { clasesSnap ->
            eventosQuery.get().addOnSuccessListener { eventosSnap ->
                val merged = mergeSnapshots(clasesSnap, eventosSnap)
                allEvents = merged
                // Update day cells that have events
                dayAdapter.updateEventDays(merged.map { it.fecha })
                // Show events for currently selected date
                showEventsForDate(selectedDate)
            }.addOnFailureListener { e ->
                // Handle error
                e.printStackTrace()
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

    /** Convert Firestore snapshots into a unified list of EventoCalendario */
    private fun mergeSnapshots(clasesSnap: QuerySnapshot, eventosSnap: QuerySnapshot): List<EventoCalendario> {
        val list = mutableListOf<EventoCalendario>()
        // Map clase documents
        for (doc in clasesSnap) {
            val data = doc.data
            val fecha = (data["fecha"] as? com.google.firebase.Timestamp)?.toDate()
            if (fecha != null) {
                val localDate = fecha.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                val titulo = data["nombre"] as? String ?: "Clase"
                val horaInicio = data["horaInicio"] as? String ?: ""
                val horaFin = data["horaFin"] as? String ?: ""
                val ubicacion = data["aula"] as? String ?: ""
                list.add(EventoCalendario(titulo, localDate, horaInicio, horaFin, ubicacion))
            }
        }
        // Map eventosEscolares documents
        for (doc in eventosSnap) {
            val data = doc.data
            val fecha = (data["fecha"] as? com.google.firebase.Timestamp)?.toDate()
            if (fecha != null) {
                val localDate = fecha.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                val titulo = data["titulo"] as? String ?: "Evento"
                val horaInicio = data["horaInicio"] as? String ?: ""
                val horaFin = data["horaFin"] as? String ?: ""
                val ubicacion = data["lugar"] as? String ?: ""
                list.add(EventoCalendario(titulo, localDate, horaInicio, horaFin, ubicacion))
            }
        }
        return list
    }

    private fun onDaySelected(date: LocalDate) {
        selectedDate = date
        showEventsForDate(date)
    }

    private fun showEventsForDate(date: LocalDate) {
        val eventsForDay = allEvents.filter { it.fecha == date }
        eventAdapter.submitList(eventsForDay)
        // Show/hide empty view if needed
        findViewById<View>(R.id.empty_state).visibility = if (eventsForDay.isEmpty()) View.VISIBLE else View.GONE
    }
}

// Helper data class for the day grid items
data class DayItem(val date: LocalDate?, var hasEvent: Boolean)
