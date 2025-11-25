package com.jjcc.proyectmovil.roles.student

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.adapter.DashboardAsistenciaAdapter
import com.jjcc.proyectmovil.core.model.ItemAsistencia

class MisAsistencias : AppCompatActivity() {

    private lateinit var recyclerAsistencias: RecyclerView
    private lateinit var tvGeneralAttendance: TextView
    private lateinit var tvRiskStatus: TextView
    private lateinit var tvJustifiedCount: TextView
    private lateinit var tvUnjustifiedCount: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val asistenciasList = mutableListOf<ItemAsistencia>()
    private lateinit var adapter: DashboardAsistenciaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_asistencias)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Initialize Views
        tvGeneralAttendance = findViewById(R.id.tvGeneralAttendance)
        tvRiskStatus = findViewById(R.id.tvRiskStatus)
        tvJustifiedCount = findViewById(R.id.tvJustifiedCount)
        tvUnjustifiedCount = findViewById(R.id.tvUnjustifiedCount)
        recyclerAsistencias = findViewById(R.id.recyclerAsistencias)

        // Setup RecyclerView
        recyclerAsistencias.layoutManager = LinearLayoutManager(this)
        adapter = DashboardAsistenciaAdapter(asistenciasList)
        recyclerAsistencias.adapter = adapter

        cargarAsistencias()
    }

    private fun cargarAsistencias() {
        val estudianteId = auth.currentUser?.uid ?: return

        db.collection("asistencia")
            .whereEqualTo("estudianteId", estudianteId)
            .get()
            .addOnSuccessListener { result ->
                asistenciasList.clear()

                var totalClases = 0
                var asistenciasValidas = 0  // P + T
                var justificadas = 0
                var injustificadas = 0

                // Contador para procesar todas las asistencias con sus clases
                var procesados = 0
                val totalDocs = result.size()

                if (totalDocs == 0) {
                    updateDashboardStats(0, 0, 0, 0)
                    return@addOnSuccessListener
                }

                for (doc in result) {
                    val asistencia = doc.toObject(ItemAsistencia::class.java)
                    val claseId = asistencia.claseId

                    // Cruzar con la colección clases para obtener la fecha real
                    if (!claseId.isNullOrBlank()) {
                        db.collection("clases").document(claseId).get()
                            .addOnSuccessListener { claseDoc ->
                                // Obtener la fecha real de la clase
                                val fechaClase = claseDoc.getTimestamp("fecha")

                                // Crear una copia del objeto con la fecha correcta
                                val asistenciaConFecha = ItemAsistencia(
                                    id = asistencia.id,
                                    claseId = asistencia.claseId,
                                    cursoId = asistencia.cursoId,
                                    estudianteId = asistencia.estudianteId,
                                    estado = asistencia.estado,
                                    fecha = fechaClase ?: asistencia.fecha,
                                    modificaciones = asistencia.modificaciones
                                )

                                asistenciasList.add(asistenciaConFecha)

                                totalClases++

                                // Clasificar según el estado (P, T, A)
                                when (asistencia.estado?.uppercase()) {
                                    "P" -> asistenciasValidas++  // Presente
                                    "T" -> asistenciasValidas++  // Tardanza (cuenta como asistencia)
                                    "A" -> {
                                        // Ausente - verificar si está justificado
                                        val tieneJustificacion = asistencia.modificaciones?.any { mod ->
                                            mod["tipo"] == "justificacion" ||
                                            mod["justificado"] == true ||
                                            mod["observacion"] != null
                                        } ?: false

                                        if (tieneJustificacion) {
                                            justificadas++
                                        } else {
                                            injustificadas++
                                        }
                                    }
                                }

                                procesados++
                                if (procesados == totalDocs) {
                                    // Ordenar por fecha descendente (más reciente primero)
                                    asistenciasList.sortByDescending { it.fecha }
                                    adapter.notifyDataSetChanged()
                                    updateDashboardStats(totalClases, asistenciasValidas, justificadas, injustificadas)
                                }
                            }
                            .addOnFailureListener {
                                // Si falla la consulta de clase, usar la fecha del documento de asistencia
                                asistenciasList.add(asistencia)
                                totalClases++

                                when (asistencia.estado?.uppercase()) {
                                    "P" -> asistenciasValidas++
                                    "T" -> asistenciasValidas++
                                    "A" -> {
                                        val tieneJustificacion = asistencia.modificaciones?.any { mod ->
                                            mod["tipo"] == "justificacion" ||
                                            mod["justificado"] == true ||
                                            mod["observacion"] != null
                                        } ?: false

                                        if (tieneJustificacion) {
                                            justificadas++
                                        } else {
                                            injustificadas++
                                        }
                                    }
                                }

                                procesados++
                                if (procesados == totalDocs) {
                                    asistenciasList.sortByDescending { it.fecha }
                                    adapter.notifyDataSetChanged()
                                    updateDashboardStats(totalClases, asistenciasValidas, justificadas, injustificadas)
                                }
                            }
                    } else {
                        // Si no hay claseId, agregar directamente
                        asistenciasList.add(asistencia)
                        totalClases++

                        when (asistencia.estado?.uppercase()) {
                            "P" -> asistenciasValidas++
                            "T" -> asistenciasValidas++
                            "A" -> {
                                val tieneJustificacion = asistencia.modificaciones?.any { mod ->
                                    mod["tipo"] == "justificacion" ||
                                    mod["justificado"] == true ||
                                    mod["observacion"] != null
                                } ?: false

                                if (tieneJustificacion) {
                                    justificadas++
                                } else {
                                    injustificadas++
                                }
                            }
                        }

                        procesados++
                        if (procesados == totalDocs) {
                            asistenciasList.sortByDescending { it.fecha }
                            adapter.notifyDataSetChanged()
                            updateDashboardStats(totalClases, asistenciasValidas, justificadas, injustificadas)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar asistencias", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDashboardStats(total: Int, asistenciasValidas: Int, justificadas: Int, injustificadas: Int) {
        if (total == 0) {
            tvGeneralAttendance.text = "100%"
            tvRiskStatus.text = "Sin registros de asistencia."
            tvJustifiedCount.text = "0"
            tvUnjustifiedCount.text = "0"
            return
        }

        // Calculate Percentage: (Presentes + Tardanzas) / Total
        // Las justificadas NO cuentan para el porcentaje, solo para el conteo
        val percentage = (asistenciasValidas.toFloat() / total.toFloat()) * 100

        tvGeneralAttendance.text = "${percentage.toInt()}%"
        tvJustifiedCount.text = justificadas.toString()
        tvUnjustifiedCount.text = injustificadas.toString()

        // Risk Status Logic (Example: Risk if < 80%)
        val riskThreshold = 80
        if (percentage < riskThreshold) {
            tvRiskStatus.text = "Estás en riesgo de reprobar por inasistencias."
        } else {
            tvRiskStatus.text = "Tu asistencia es buena. ¡Sigue así!"
        }
    }
}
