package com.jjcc.proyectmovil.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var dayAdapter: DayAdapter
    private lateinit var eventAdapter: EventAdapter
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrev: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var tvEventsHeader: TextView
    private lateinit var bottomNav: com.google.android.material.bottomnavigation.BottomNavigationView

    // These would normally be provided by authentication / intent extras
    private val userId: String = "USER_ID_PLACEHOLDER" // TODO: Get actual user ID
    private val userRole: String = "DOCENTE" // TODO: Get actual role

    private val firestore = FirebaseFirestore.getInstance()

    private var currentYearMonth: YearMonth = YearMonth.now()
    private var selectedDate: LocalDate = LocalDate.now()
    private var allEvents: List<EventoCalendario> = emptyList()
    private var currentMonthDays: List<DayItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarRecyclerView = findViewById(R.id.recycler_calendar)
        eventsRecyclerView = findViewById(R.id.recycler_events)
        tvMonthYear = findViewById(R.id.tv_month_year)
        btnPrev = findViewById(R.id.btn_prev_month)
        btnNext = findViewById(R.id.btn_next_month)
        tvEventsHeader = findViewById(R.id.tv_events_header)
        bottomNav = findViewById(R.id.bottomNavigation)

        dayAdapter = DayAdapter { date -> onDaySelected(date) }
        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)
        calendarRecyclerView.adapter = dayAdapter

        eventAdapter = EventAdapter()
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventsRecyclerView.adapter = eventAdapter

        btnPrev.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            loadCalendar()
        }

        btnNext.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            loadCalendar()
        }

        setupBottomNavigation()
        loadCalendar()
    }

    private fun setupBottomNavigation() {
        bottomNav.selectedItemId = R.id.nav_calendar
        bottomNav.itemRippleColor = null
        bottomNav.setOnItemReselectedListener {}

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate back to Home based on role
                    finish() // Since we came from Home, finishing returns to it
                    true
                }
                R.id.nav_messages -> {
                    startActivity(android.content.Intent(this, com.jjcc.proyectmovil.messages.MainChatActivity::class.java))
                    false
                }
                R.id.nav_calendar -> true
                R.id.nav_profile -> {
                    startActivity(android.content.Intent(this, com.jjcc.proyectmovil.profile.PerfilActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    /** Load days of the current month and fetch events from Firestore */
    private fun loadCalendar() {
        // Update Header
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("es", "ES"))
        tvMonthYear.text = currentYearMonth.format(formatter).replaceFirstChar { it.uppercase() }

        // Build list of DayItem for the month
        val days = mutableListOf<DayItem>()
        val firstOfMonth = currentYearMonth.atDay(1)
        val dayOfWeekOffset = firstOfMonth.dayOfWeek.value % 7 // Monday=1 ... Sunday=7, we want 0..6 offset (Sunday=0)
        // Note: Java Time DayOfWeek: Mon=1, Sun=7.
        // If our grid starts at Sunday (0), and Mon is 1.
        // If 1st is Mon (1), offset should be 1.
        // If 1st is Sun (7), offset should be 0.
        // So offset = dayOfWeek.value % 7. Correct.

        // Fill preceding empty cells
        for (i in 0 until dayOfWeekOffset) {
            days.add(DayItem(null, false))
        }
        val daysInMonth = currentYearMonth.lengthOfMonth()
        for (day in 1..daysInMonth) {
            val date = currentYearMonth.atDay(day)
            days.add(DayItem(date, false)) // event flag will be updated after fetching
        }
        currentMonthDays = days
        dayAdapter.submitList(currentMonthDays, selectedDate)
        fetchEventsForMonth()
    }
    /** Query Firestore for both collections, merge them, and update UI */
    private fun fetchEventsForMonth() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) return

        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        findViewById<View>(R.id.empty_state).visibility = View.GONE

        val start = currentYearMonth.atDay(1).atStartOfDay()
        val end = currentYearMonth.atEndOfMonth().atTime(23, 59, 59)

        // Convert to Firestore Timestamp
        val startTs = com.google.firebase.Timestamp(java.util.Date.from(start.atZone(java.time.ZoneId.systemDefault()).toInstant()))
        val endTs = com.google.firebase.Timestamp(java.util.Date.from(end.atZone(java.time.ZoneId.systemDefault()).toInstant()))

        val role = intent.getStringExtra("USER_ROLE") ?: "DOCENTE"

        // Query clases collection based on role
        val clasesQuery = when (role) {
            "DOCENTE" -> firestore.collection("clases")
                .whereEqualTo("docenteId", uid)
                .whereGreaterThanOrEqualTo("fecha", startTs)
                .whereLessThanOrEqualTo("fecha", endTs)
            "ESTUDIANTE" -> firestore.collection("clases")
                .whereArrayContains("estudiantesInscritos", uid)
                .whereGreaterThanOrEqualTo("fecha", startTs)
                .whereLessThanOrEqualTo("fecha", endTs)
            else -> firestore.collection("clases")
                .whereEqualTo("docenteId", uid) // Fallback
                .whereGreaterThanOrEqualTo("fecha", startTs)
                .whereLessThanOrEqualTo("fecha", endTs)
        }

        // Query eventos collection (global events)
        val eventosQuery = firestore.collection("eventos")
            .whereEqualTo("activo", true)
            .whereGreaterThanOrEqualTo("fecha", startTs)
            .whereLessThanOrEqualTo("fecha", endTs)

        // Execute both queries in parallel and combine results
        clasesQuery.get().addOnSuccessListener { clasesSnap ->
            eventosQuery.get().addOnSuccessListener { eventosSnap ->
                progressBar.visibility = View.GONE
                val merged = mergeSnapshots(clasesSnap, eventosSnap)
                allEvents = merged.sortedWith(compareBy({ it.fecha }, { it.horaInicio }))

                // Update day cells that have events
                dayAdapter.updateEventDays(allEvents.map { it.fecha }.distinct())

                // Refresh view for selected date
                showEventsForDate(selectedDate)
            }.addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                e.printStackTrace()
                android.util.Log.e("CalendarActivity", "Error fetching events", e)
                val msg = if (e.message?.contains("FAILED_PRECONDITION") == true) {
                    "Falta índice en Firestore. Revisa el Logcat para el link de creación."
                } else {
                    "Error cargando eventos: ${e.message}"
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { e ->
            progressBar.visibility = View.GONE
            e.printStackTrace()
            android.util.Log.e("CalendarActivity", "Error fetching classes", e)
            val msg = if (e.message?.contains("FAILED_PRECONDITION") == true) {
                "Falta índice en Firestore. Revisa el Logcat para el link de creación."
            } else {
                "Error cargando clases: ${e.message}"
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
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
                val tema = data["tema"] as? String ?: "Clase"
                val materia = data["nombre"] as? String ?: "Asignatura" // Fallback if tema is missing or use materia name
                val horaInicio = data["horaInicio"] as? String ?: ""
                val horaFin = data["horaFin"] as? String ?: "" // Or calculate from duracion
                val aula = data["aula"] as? String ?: ""

                // Use tema as title, materia/aula as subtitle
                val titulo = if (tema != "Clase") tema else materia
                val ubicacion = if (aula.isNotEmpty()) "$materia - $aula" else materia

                list.add(EventoCalendario(titulo, localDate, horaInicio, horaFin, ubicacion))
            }
        }

        // Map eventos documents
        for (doc in eventosSnap) {
            val data = doc.data
            val fecha = (data["fecha"] as? com.google.firebase.Timestamp)?.toDate()
            if (fecha != null) {
                val localDate = fecha.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                val titulo = data["titulo"] as? String ?: "Evento Escolar"
                val horaInicio = data["horaInicio"] as? String ?: ""
                val horaFin = data["horaFin"] as? String ?: ""
                val lugar = data["lugar"] as? String ?: "Campus"

                list.add(EventoCalendario(titulo, localDate, horaInicio, horaFin, lugar))
            }
        }
        return list
    }

    private fun onDaySelected(date: LocalDate) {
        selectedDate = date
        dayAdapter.submitList(currentMonthDays, selectedDate)
        showEventsForDate(date)
    }

    private fun showEventsForDate(date: LocalDate) {
        val eventsForDay = allEvents.filter { it.fecha == date }
        eventAdapter.submitList(eventsForDay)

        val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM", java.util.Locale("es", "ES"))
        tvEventsHeader.text = "Eventos del ${date.format(formatter)}"

        findViewById<View>(R.id.empty_state).visibility = if (eventsForDay.isEmpty()) View.VISIBLE else View.GONE
    }
}

// Helper data class for the day grid items
data class DayItem(val date: LocalDate?, var hasEvent: Boolean)
