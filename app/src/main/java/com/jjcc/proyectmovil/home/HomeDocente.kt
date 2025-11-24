package com.jjcc.proyectmovil.home

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.messages.MainChatActivity
import com.jjcc.proyectmovil.profile.PerfilActivity
import com.jjcc.proyectmovil.roles.admin.GestionCursosActivity
import com.jjcc.proyectmovil.roles.admin.GestionUsuariosActivity
import com.jjcc.proyectmovil.roles.docente.CalificacionesActivity
import com.jjcc.proyectmovil.roles.docente.GestionAsistenciaActivity
import java.util.*
import kotlin.collections.ArrayList
import androidx.core.graphics.toColorInt

class HomeDocente : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tvSaludoDocente: TextView
    private lateinit var container: FrameLayout
    private lateinit var spinnerFiltro: Spinner

    private lateinit var btnAsistencia: LinearLayout

    private lateinit var btnCalificaciones: LinearLayout

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var asistenciasFechas = ArrayList<Date>()
    private var asistenciasHoy = ArrayList<Date>()

    // Colores para las gráficas
    private val colorLavanda = "#C7B3FF".toColorInt()
    private val colorTexto = "#666666".toColorInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_docente)
        enableEdgeToEdge()

        tvSaludoDocente = findViewById(R.id.tvSaludoDocente)
        container = findViewById(R.id.containerGrafica)
        spinnerFiltro = findViewById(R.id.spinnerFiltro)
        bottomNav = findViewById(R.id.bottomNavigation)
        btnAsistencia=findViewById(R.id.cardAsistencia)
        btnCalificaciones=findViewById(R.id.cardCalificaciones)

        btnAsistencia.setOnClickListener {
            startActivity(Intent(this, GestionAsistenciaActivity::class.java))
        }

        btnCalificaciones.setOnClickListener {
            startActivity(Intent(this, CalificacionesActivity::class.java))
        }

        inicializarSpinner()
        cargarNombreDesdeFirestore()
        cargarAsistenciasDocente()

        configurarBottomNav()
    }

    // ============================================================
    // SPINNER
    // ============================================================
    private fun inicializarSpinner() {
        val opciones = arrayOf("Hoy", "Semana", "Mes")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)
        spinnerFiltro.adapter = adapter
        spinnerFiltro.setSelection(0) // Default HOY

        spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                actualizarVistaContainer(spinnerFiltro.selectedItem.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ============================================================
    // NOMBRE DOCENTE
    // ============================================================
    private fun cargarNombreDesdeFirestore() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener {
                val nombre = it.getString("nombre") ?: "Docente"
                tvSaludoDocente.text = "¡Hola, $nombre!"
            }
    }

    // ============================================================
    // CARGAR ASISTENCIAS DEL DOCENTE
    // ============================================================
    private fun cargarAsistenciasDocente() {
        val uid = auth.currentUser?.uid ?: return

        // Obtener cursos donde docenteId == uid
        db.collection("cursos")
            .whereEqualTo("docenteId", uid)
            .get()
            .addOnSuccessListener { cursosSnap ->

                val cursosIds = cursosSnap.documents.map { it.id }

                if (cursosIds.isEmpty()) {
                    asistenciasFechas.clear()
                    asistenciasHoy.clear()
                    actualizarVistaContainer(spinnerFiltro.selectedItem.toString())
                    return@addOnSuccessListener
                }

                val inicioMes = obtenerInicioMesTimestamp()

                val chunks = cursosIds.chunked(10)
                asistenciasFechas.clear()
                asistenciasHoy.clear()

                var pendientes = chunks.size

                for (chunk in chunks) {
                    db.collection("asistencia")
                        .whereIn("cursoId", chunk)
                        .whereGreaterThanOrEqualTo("fecha", inicioMes)
                        .get()
                        .addOnSuccessListener { snap ->

                            for (doc in snap.documents) {
                                val fecha = doc.getTimestamp("fecha")?.toDate() ?: continue
                                asistenciasFechas.add(fecha)

                                // Guardar asistencias HOY
                                val cal = Calendar.getInstance()
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                if (fecha >= cal.time) {
                                    asistenciasHoy.add(fecha)
                                }
                            }

                            pendientes--
                            if (pendientes == 0) {
                                actualizarVistaContainer(spinnerFiltro.selectedItem.toString())
                            }
                        }
                        .addOnFailureListener {
                            pendientes--
                            if (pendientes == 0) {
                                actualizarVistaContainer(spinnerFiltro.selectedItem.toString())
                            }
                        }
                }
            }
    }

    // ============================================================
    // ACTUALIZAR VISTA DEL CONTAINER
    // ============================================================
    private fun actualizarVistaContainer(filtro: String) {

        container.removeAllViews()

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)

        // ---------------- TITULO ----------------
        val tvTitulo = TextView(this)
        tvTitulo.text = "Asistencia (Registros)"
        tvTitulo.textSize = 18f
        tvTitulo.setTypeface(null, Typeface.BOLD)
        tvTitulo.setTextColor(Color.BLACK)
        layout.addView(tvTitulo)

        // ---------------- GRÁFICA ----------------
        val chart = BarChart(this)
        chart.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            250
        )
        layout.addView(chart)

        when (filtro) {
            "Hoy" -> dibujarGraficaHoy(chart)
            "Semana" -> dibujarGraficaSemana(chart)
            "Mes" -> dibujarGraficaMes(chart)
        }

        // ---------------- TABLA / DETALLE ----------------
        val tabla = crearTablaSegunFiltro(filtro)
        layout.addView(tabla)

        container.addView(layout)
    }

    // ============================================================
    // CONFIGURACIÓN GENERAL DEL CHART
    // ============================================================
    private fun configurarChart(chart: BarChart) {
        chart.setDrawGridBackground(false)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.setTouchEnabled(false)
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = colorTexto
        xAxis.textSize = 10f
        xAxis.granularity = 1f

        val left = chart.axisLeft
        left.setDrawGridLines(false)
        left.setDrawLabels(false)
        left.axisMinimum = 0f
        left.axisLineColor = Color.TRANSPARENT

        val right = chart.axisRight
        right.setDrawGridLines(false)
        right.setDrawLabels(false)
        right.axisMinimum = 0f
        right.axisLineColor = Color.TRANSPARENT
    }

    // ============================================================
    // GRÁFICAS
    // ============================================================
    private fun dibujarGraficaHoy(chart: BarChart) {
        configurarChart(chart)

        val total = asistenciasHoy.size
        // para que no desaparezca cuando es 0
        val value = if (total == 0) 0.2f else total.toFloat()

        val entries = listOf(BarEntry(0f, value))
        val labels = listOf("Hoy")

        val dataSet = BarDataSet(entries, "")
        dataSet.color = colorLavanda
        dataSet.valueTextColor = Color.TRANSPARENT

        val data = BarData(dataSet)
        data.barWidth = 0.4f

        chart.data = data
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        chart.invalidate()
    }

    private fun dibujarGraficaSemana(chart: BarChart) {

        configurarChart(chart)

        val dias = listOf("L", "M", "M", "J", "V", "S", "D")
        val valores = contarSemanaPorDia()

        val entries = ArrayList<BarEntry>()
        for (i in valores.indices) {
            val v = if (valores[i] == 0) 0.2f else valores[i].toFloat()
            entries.add(BarEntry(i.toFloat(), v))
        }

        val dataSet = BarDataSet(entries, "")
        dataSet.color = colorLavanda
        dataSet.valueTextColor = Color.TRANSPARENT

        val data = BarData(dataSet)
        data.barWidth = 0.4f

        chart.data = data
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(dias)
        chart.invalidate()
    }

    private fun dibujarGraficaMes(chart: BarChart) {

        configurarChart(chart)

        val cal = Calendar.getInstance()
        val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val valores = contarMesPorDia(max)

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for (i in 1..max) {
            val v = if (valores[i - 1] == 0) 0.2f else valores[i - 1].toFloat()
            entries.add(BarEntry(i.toFloat(), v))
            labels.add(i.toString())
        }

        val dataSet = BarDataSet(entries, "")
        dataSet.color = colorLavanda
        dataSet.valueTextColor = Color.TRANSPARENT

        val data = BarData(dataSet)
        data.barWidth = 0.4f

        chart.data = data
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.invalidate()
    }

    // ============================================================
    // TABLA DETALLADA
    // ============================================================
    private fun crearTablaSegunFiltro(filtro: String): LinearLayout {

        val tabla = LinearLayout(this)
        tabla.orientation = LinearLayout.VERTICAL
        tabla.setPadding(4, 20, 4, 4)

        when (filtro) {

            "Hoy" -> {
                if (asistenciasHoy.isEmpty()) {
                    val tv = TextView(this)
                    tv.text = "No hay registros para este periodo."
                    tv.textSize = 14f
                    tv.setTextColor(colorTexto)
                    tv.setTypeface(null, Typeface.ITALIC)
                    tabla.addView(tv)
                } else {
                    asistenciasHoy.forEach {
                        val hora = android.text.format.DateFormat.format("hh:mm a", it)
                        tabla.addView(texto("- $hora"))
                    }
                }
            }

            "Semana" -> {
                val dias = arrayOf(
                    "Lunes", "Martes", "Miércoles",
                    "Jueves", "Viernes", "Sábado", "Domingo"
                )
                val cont = contarSemanaPorDia()

            }

            "Mes" -> {
                val cal = Calendar.getInstance()
                val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val cont = contarMesPorDia(max)
            }
        }

        return tabla
    }

    // ============================================================
    // CONTADORES
    // ============================================================
    private fun contarHoy(): Int = asistenciasHoy.size

    private fun obtenerAsistenciasHoy(): List<Date> = asistenciasHoy

    private fun contarSemana(): Int = contarSemanaPorDia().sum()

    private fun contarSemanaPorDia(): IntArray {
        val cont = IntArray(7)

        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val inicioSemana = cal.time

        asistenciasFechas.forEach {
            if (it >= inicioSemana) {
                val c = Calendar.getInstance()
                c.time = it
                val index = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
                cont[index]++
            }
        }

        return cont
    }

    private fun contarMesPorDia(max: Int): IntArray {
        val cont = IntArray(max)

        asistenciasFechas.forEach {
            val c = Calendar.getInstance()
            c.time = it
            val dia = c.get(Calendar.DAY_OF_MONTH)
            cont[dia - 1]++
        }

        return cont
    }

    // ============================================================
    // UTILS
    // ============================================================
    private fun obtenerInicioMesTimestamp(): Timestamp {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return Timestamp(cal.time)
    }

    private fun texto(t: String): TextView {
        val tv = TextView(this)
        tv.text = t
        tv.textSize = 14f
        tv.setTextColor(Color.BLACK)
        return tv
    }

    // ============================================================
    // NAVIGATION
    // ============================================================
    private fun configurarBottomNav() {
        bottomNav.itemRippleColor = null
        bottomNav.setOnItemReselectedListener {}
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_messages -> {
                    startActivity(Intent(this, MainChatActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
