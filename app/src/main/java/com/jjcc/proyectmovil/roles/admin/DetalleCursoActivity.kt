package com.jjcc.proyectmovil.roles.admin

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R

class DetalleCursoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tvIdCurso: TextView
    private lateinit var tvNombreCurso: TextView
    private lateinit var tvDocenteAsignado: TextView
    private lateinit var tvGradoSeccion: TextView
    private lateinit var tvAsignatura: TextView
    private lateinit var tvAula: TextView
    private lateinit var tvPeriodo: TextView
    private lateinit var tvHorarioResumen: TextView
    private lateinit var tvListaEstudiantes: TextView
    private lateinit var tvTituloEstudiantes: TextView
    private lateinit var tvTituloEstudiantesCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_curso)

        db = FirebaseFirestore.getInstance()

        tvIdCurso = findViewById(R.id.tvIdCurso)
        tvNombreCurso = findViewById(R.id.tvNombreCurso)
        tvDocenteAsignado = findViewById(R.id.tvDocenteAsignado)
        tvGradoSeccion = findViewById(R.id.tvGradoSeccion)
        tvAsignatura = findViewById(R.id.tvAsignatura)
        tvAula = findViewById(R.id.tvAula)
        tvPeriodo = findViewById(R.id.tvPeriodo)
        tvHorarioResumen = findViewById(R.id.tvHorarioResumen)

        tvListaEstudiantes = findViewById(R.id.tvListaEstudiantes)
        tvTituloEstudiantes = findViewById(R.id.tvTituloEstudiantes)
        tvTituloEstudiantesCount = findViewById(R.id.tvTituloEstudiantesCount)

        val cursoId = intent.getStringExtra("cursoId")
        if (cursoId.isNullOrBlank()) {
            Toast.makeText(this, "Curso no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarDetallesCurso(cursoId)
    }

    private fun cargarDetallesCurso(cursoId: String) {
        db.collection("cursos")
            .document(cursoId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "No se encontró el curso", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val id = doc.getString("id") ?: doc.id
                val nombre = doc.getString("nombre") ?: ""
                val grado = doc.getString("grado") ?: ""
                val seccion = doc.getString("seccion") ?: ""
                val asignatura = doc.getString("asignatura") ?: ""
                val aula = doc.getString("aula") ?: "-"
                val periodo = doc.getString("periodo") ?: "-"
                val docenteId = doc.getString("docenteId") ?: ""

                val horario = doc.get("horario") as? List<Map<String, Any>>

                // Grado/Sección tipo "11A"
                val gradoSeccion = if (grado.isNotBlank() && seccion.isNotBlank()) {
                    "$grado$seccion"
                } else {
                    "-"
                }

                // Horario (Resumen): ej. "Lunes 12:00-02:00 (+2 sesiones)"
                val resumenHorario = construirResumenHorario(horario)

                tvIdCurso.text = id
                tvNombreCurso.text = nombre
                tvGradoSeccion.text = gradoSeccion
                tvAsignatura.text = asignatura
                tvAula.text = aula
                tvPeriodo.text = periodo
                tvHorarioResumen.text = resumenHorario

                // Cargar nombre del docente
                if (docenteId.isNotBlank()) {
                    db.collection("users")
                        .document(docenteId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val nombreDocente = userDoc.getString("nombre") ?: "-"
                            tvDocenteAsignado.text = nombreDocente
                        }
                        .addOnFailureListener {
                            tvDocenteAsignado.text = "-"
                        }
                } else {
                    tvDocenteAsignado.text = "-"
                }

                // Cargar estudiantes
                val estudiantesIds = doc.get("estudiantesInscritos") as? List<String> ?: emptyList()
                cargarEstudiantes(estudiantesIds)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando curso: ${it.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun construirResumenHorario(horario: List<Map<String, Any>>?): String {
        if (horario == null || horario.isEmpty()) return "-"

        val primera = horario.first()
        val dia = primera["dia"]?.toString() ?: ""
        val horaInicio = primera["horaInicio"]?.toString() ?: ""
        val horaFin = primera["horaFin"]?.toString() ?: ""

        val totalSesiones = horario.size
        val extra = if (totalSesiones > 1) " (+${totalSesiones - 1} sesiones)" else ""

        return "$dia $horaInicio-$horaFin$extra"
    }

    private fun cargarEstudiantes(ids: List<String>) {
        if (ids.isEmpty()) {
            tvTituloEstudiantesCount.text = "(0)"
            tvListaEstudiantes.text = "Sin estudiantes inscritos"
            return
        }

        tvTituloEstudiantesCount.text = "(${ids.size})"
        tvListaEstudiantes.text = "Cargando estudiantes..."

        val listaLineas = mutableListOf<String>()
        var pendientes = ids.size

        for (id in ids) {
            db.collection("users")
                .document(id)
                .get()
                .addOnSuccessListener { userDoc ->
                    val nombre = userDoc.getString("nombre") ?: "Sin nombre"
                    listaLineas.add("$nombre (ID: $id)")
                }
                .addOnFailureListener {
                    listaLineas.add("ID: $id")
                }
                .addOnCompleteListener {
                    pendientes--
                    if (pendientes == 0) {
                        // Todos los estudiantes procesados
                        listaLineas.sort()
                        tvListaEstudiantes.text = listaLineas.joinToString(separator = "\n")
                    }
                }
        }
    }
}