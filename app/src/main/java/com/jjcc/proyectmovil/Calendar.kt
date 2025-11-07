package com.jjcc.proyectmovil

import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Calendar : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val tvFechaSeleccionada = findViewById<TextView>(R.id.tvFechaSeleccionada)

        // Detecta cuando el usuario hace clic en un día del calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Ojo: en Android el mes empieza en 0 (Enero=0, Febrero=1...)
            val fecha = "$dayOfMonth/${month + 1}/$year"

            // Muestra la fecha seleccionada
            tvFechaSeleccionada.text = "Seleccionaste: $fecha"

            // Aquí podrías guardar la fecha en Firebase si quieres
            // val db = FirebaseDatabase.getInstance().getReference("calendario")
            // val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            // db.child(uid).child("ultimaFecha").setValue(fecha)
        }
    }
}
