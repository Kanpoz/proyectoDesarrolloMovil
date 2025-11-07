package com.jjcc.proyectmovil

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R

class AsistenciasActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistencias)

        barChart = findViewById(R.id.barChart)
        loadAttendanceData()
    }

    private fun loadAttendanceData() {
        val cursoId = "curso_1A" // temporal, cambiar según usuario
        db.collection("asistencia_semanal").document(cursoId).collection("semanas")
            .get().addOnSuccessListener { snap ->
                val entries = ArrayList<BarEntry>()
                val labels = ArrayList<String>()
                var i = 0
                for (doc in snap.documents) {
                    val porcentaje = doc.getDouble("porcentaje")?.toFloat() ?: 0f
                    entries.add(BarEntry(i.toFloat(), porcentaje))
                    labels.add(doc.id)
                    i++
                }
                val dataSet = BarDataSet(entries, "Asistencia Semanal")
                dataSet.colors = entries.map {
                    if (it.y >= 85f) Color.parseColor("#4CAF50") else Color.parseColor("#FBC02D")
                }
                val barData = BarData(dataSet)
                barChart.data = barData
                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                barChart.invalidate()
            }
    }
}
