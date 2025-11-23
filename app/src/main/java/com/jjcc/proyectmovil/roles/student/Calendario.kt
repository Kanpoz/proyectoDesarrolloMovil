package com.jjcc.proyectmovil.roles.student

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Calendario : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var layoutEventos: LinearLayout
    private lateinit var calendarView: CalendarView
    private lateinit var tvFechaSeleccionada: TextView
    val estudianteId: String? = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        db = FirebaseFirestore.getInstance()
        layoutEventos = findViewById(R.id.layoutEventos)
        calendarView = findViewById(R.id.calendarView)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)

        if (estudianteId != null) {
            db.collection("cursos")
                .whereArrayContains("estudiantesInscritos", estudianteId)
                .get()
                .addOnSuccessListener { documentos ->
                    for (doc in documentos) {
                        val nombreCurso = doc.getString("nombre")
                        Log.d("Cursos", "Clase: $nombreCurso")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Cursos", "Error al obtener clases", e)
                }
        }

        // Listener del calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val diaSemana = obtenerNombreDia(calendar.time)
            tvFechaSeleccionada.text = "Clases del $diaSemana"
            cargarClasesDelDia(diaSemana)
        }
    }

    private fun obtenerNombreDia(fecha: Date): String {
        val formato = SimpleDateFormat("EEEE", Locale("es", "ES"))
        return formato.format(fecha).replaceFirstChar { it.uppercase() }
    }

    private fun cargarClasesDelDia(dia: String) {
        layoutEventos.removeAllViews() // Limpiamos la vista anterior

        db.collection("cursos")
            .whereArrayContains("estudiantesInscritos", estudianteId!!)
            .get()
            .addOnSuccessListener { result ->
                var hayClases = false
                for (doc in result) {
                    val nombre = doc.getString("nombre") ?: ""
                    val aula = doc.getString("aula") ?: ""
                    val horarios = doc.get("horario") as? List<Map<String, Any>>

                    horarios?.forEach { horario ->
                        val diaCurso = horario["dia"]?.toString()?.lowercase()
                        val diaSeleccionado = dia.lowercase()

                        if (diaCurso == diaSeleccionado) {
                            hayClases = true
                            val horaInicio = horario["horaInicio"].toString()
                            val horaFin = horario["horaFin"].toString()
                            agregarClase(nombre, aula, horaInicio, horaFin)
                        }
                    }
                }

                if (!hayClases) {
                    mostrarMensajeSinClases()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar las clases", Toast.LENGTH_SHORT).show()
            }
    }
    private fun agregarClase(nombre: String, aula: String, horaInicio: String, horaFin: String) {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
            radius = 24f
            cardElevation = 6f
            setCardBackgroundColor(Color.WHITE)
        }

        val textView = TextView(this).apply {
            text = "$nombre\nAula: $aula\nHora: $horaInicio - $horaFin"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(40, 40, 40, 40)
        }

        card.addView(textView)
        layoutEventos.addView(card)
    }

    private fun mostrarMensajeSinClases() {
        val mensaje = TextView(this).apply {
            text = "No tienes clases este día"
            textSize = 18f
            setTextColor(Color.DKGRAY)
            setPadding(40, 40, 40, 40)
        }
        layoutEventos.addView(mensaje)
    }
}