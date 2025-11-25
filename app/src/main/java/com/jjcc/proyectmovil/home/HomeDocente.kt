package com.jjcc.proyectmovil.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemCurso
import com.jjcc.proyectmovil.messages.MainChatActivity
import com.jjcc.proyectmovil.profile.PerfilActivity
import com.jjcc.proyectmovil.roles.docente.CalificacionesActivity
import com.jjcc.proyectmovil.roles.docente.GestionAsistenciaActivity
import java.util.*
import kotlin.collections.ArrayList

class HomeDocente : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tvSaludoDocente: TextView
    private lateinit var rvProximasClases: RecyclerView
    private lateinit var tvSinClases: TextView
    private lateinit var tvNotificacionesEmpty: TextView

    private lateinit var cardAsistencia: CardView
    private lateinit var cardCalificaciones: CardView

    // Chart components
    private lateinit var container: FrameLayout
    private lateinit var spinnerFiltro: Spinner
    private var asistenciasFechas = ArrayList<Date>()
    private var asistenciasHoy = ArrayList<Date>()
    private val colorLavanda = "#C7B3FF".toColorInt()
    private val colorTexto = "#666666".toColorInt()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var adapterClases: ProximasClasesAdapter
    private val listaClases = mutableListOf<ItemCurso>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_docente)
        enableEdgeToEdge()

        tvSaludoDocente = findViewById(R.id.tvSaludoDocente)
        rvProximasClases = findViewById(R.id.rvProximasClases)
        tvSinClases = findViewById(R.id.tvSinClases)
        tvNotificacionesEmpty = findViewById(R.id.tvNotificacionesEmpty)
        bottomNav = findViewById(R.id.bottomNavigation)
        cardAsistencia = findViewById(R.id.cardAsistencia)
        cardCalificaciones = findViewById(R.id.cardCalificaciones)

        // Chart init
        container = findViewById(R.id.containerGrafica)
        spinnerFiltro = findViewById(R.id.spinnerFiltro)

        cardAsistencia.setOnClickListener {
            startActivity(Intent(this, GestionAsistenciaActivity::class.java))
        }

        cardCalificaciones.setOnClickListener {
            startActivity(Intent(this, CalificacionesActivity::class.java))
        }

        configurarRecyclerView()
        cargarNombreDesdeFirestore()
        cargarCursosDocente()
        cargarNotificaciones()

        // Chart logic
        inicializarSpinner()
        cargarAsistenciasDocente()

        configurarBottomNav()
    }

    private fun configurarRecyclerView() {
        adapterClases = ProximasClasesAdapter(listaClases)
        rvProximasClases.layoutManager = LinearLayoutManager(this)
        rvProximasClases.adapter = adapterClases
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
    // CARGAR CURSOS (PRÓXIMAS CLASES)
    // ============================================================
    private fun cargarCursosDocente() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("cursos")
            .whereEqualTo("docenteId", uid)
            .get()
            .addOnSuccessListener { result ->
                listaClases.clear()
                for (document in result) {
                    val curso = ItemCurso(
                        id = document.id,
                        nombre = document.getString("nombre"),
                        grado = document.getString("grado"),
                        seccion = document.getString("seccion"),
                        aula = document.getString("aula")
                    )
                    listaClases.add(curso)
                }

                if (listaClases.isEmpty()) {
                    rvProximasClases.visibility = View.GONE
                    tvSinClases.visibility = View.VISIBLE
                } else {
                    rvProximasClases.visibility = View.VISIBLE
                    tvSinClases.visibility = View.GONE
                    adapterClases.actualizarLista(listaClases)
                }
            }
            .addOnFailureListener {
                // Manejar error
            }
    }

    // ============================================================
    // CARGAR NOTIFICACIONES
    // ============================================================
    private fun cargarNotificaciones() {
        // Implementar lógica de notificaciones si existe la colección
        // Por ahora dejamos el estado por defecto "Sin notificaciones"
    }

    // ============================================================
    // CHART LOGIC
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

    private fun cargarAsistenciasDocente() {
        val uid = auth.currentUser?.uid ?: return

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

    private fun actualizarVistaContainer(filtro: String) {
        container.removeAllViews()

        val chart = BarChart(this)
        chart.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        container.addView(chart)

        when (filtro) {
            "Hoy" -> dibujarGraficaHoy(chart)
            "Semana" -> dibujarGraficaSemana(chart)
            "Mes" -> dibujarGraficaMes(chart)
        }
    }

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

    private fun dibujarGraficaHoy(chart: BarChart) {
        configurarChart(chart)
        val total = asistenciasHoy.size
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

    private fun obtenerInicioMesTimestamp(): Timestamp {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return Timestamp(cal.time)
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
                    val intent = Intent(this, MainChatActivity::class.java)
                    intent.putExtra("USER_ROLE", "DOCENTE")
                    startActivity(intent)
                    false
                }
                R.id.nav_calendar -> {
                    val intent = Intent(this, com.jjcc.proyectmovil.ui.CalendarActivity::class.java)
                    intent.putExtra("USER_ROLE", "DOCENTE")
                    startActivity(intent)
                    false
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("USER_ROLE", "DOCENTE")
                    startActivity(intent)
                    false
                }
                else -> false
            }
        }
    }
}
