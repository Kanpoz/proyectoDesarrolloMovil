package com.jjcc.proyectmovil.roles.docente

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jjcc.proyectmovil.R
import java.text.SimpleDateFormat
import java.util.Locale

class GestionAsistenciaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // UI
    private lateinit var tvCursoNombre: TextView
    private lateinit var tvClaseRegistrada: TextView
    private lateinit var rvEstudiantes: RecyclerView
    private lateinit var btnMarcarPresentes: Button
    private lateinit var btnLimpiar: Button
    private lateinit var btnGuardar: Button
    private lateinit var layoutCursoSpinner: androidx.cardview.widget.CardView

    // Data
    private var cursoIdActual = ""
    private var claseIdActual = ""
    private var fechaClaseActual: Timestamp? = null
    private var listaCursos = ArrayList<HashMap<String, Any>>()
    private var listaClases = ArrayList<HashMap<String, Any>>()
    private var studentsList = ArrayList<StudentAttendance>()
    private lateinit var adapter: GestionAsistenciaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_asistencia)

        // Init Views
        tvCursoNombre = findViewById(R.id.tvCursoNombre)
        tvClaseRegistrada = findViewById(R.id.tvClaseRegistrada)
        rvEstudiantes = findViewById(R.id.rvEstudiantes)
        btnMarcarPresentes = findViewById(R.id.btnMarcarPresentes)
        btnLimpiar = findViewById(R.id.btnLimpiar)
        btnGuardar = findViewById(R.id.btnGuardar)
        layoutCursoSpinner = findViewById(R.id.layoutCursoSpinner)

        // Setup RecyclerView
        rvEstudiantes.layoutManager = LinearLayoutManager(this)
        adapter = GestionAsistenciaAdapter(studentsList) { _, _ ->
            // Callback if needed
        }
        rvEstudiantes.adapter = adapter

        // Listeners
        layoutCursoSpinner.setOnClickListener { mostrarSelectorCursos() }

        findViewById<android.view.View>(R.id.layoutClaseSpinner).setOnClickListener { mostrarSelectorClases() }

        btnMarcarPresentes.setOnClickListener { marcarTodos("P") }
        btnLimpiar.setOnClickListener { marcarTodos(null) }
        btnGuardar.setOnClickListener { guardarAsistencia() }

        cargarCursosDelDocente()
    }

    private fun cargarCursosDelDocente() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("cursos")
            .whereEqualTo("docenteId", uid)
            .get()
            .addOnSuccessListener { snap ->
                listaCursos.clear()
                for (curso in snap.documents) {
                    val map = hashMapOf<String, Any>(
                        "id" to curso.id,
                        "nombre" to (curso.getString("nombre") ?: "Curso"),
                        "estudiantes" to (curso.get("estudiantesInscritos") ?: emptyList<String>())
                    )
                    listaCursos.add(map)
                }

                if (listaCursos.isNotEmpty()) {
                    seleccionarCurso(listaCursos[0])
                }
            }
    }

    private fun seleccionarCurso(curso: HashMap<String, Any>) {
        cursoIdActual = curso["id"] as String
        tvCursoNombre.text = curso["nombre"] as String
        cargarClasesParaCurso(cursoIdActual)
    }

    private fun cargarClasesParaCurso(cursoId: String) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("clases")
            .whereEqualTo("docenteId", uid)
            .orderBy("fecha")
            .get()
            .addOnSuccessListener { snap ->
                listaClases.clear()
                for (clase in snap.documents) {
                    if (clase.getString("cursoId") != cursoId) continue

                    val fechaTimestamp = clase.getTimestamp("fecha")
                    val fecha = fechaTimestamp?.toDate()
                    val tema = clase.getString("tema") ?: "Clase"

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                    val map = hashMapOf<String, Any>(
                        "id" to clase.id,
                        "tema" to tema,
                        "fechaObj" to (fecha ?: java.util.Date()),
                        "fechaTimestamp" to (fechaTimestamp ?: Timestamp.now()),
                        "display" to "${dateFormat.format(fecha ?: java.util.Date())} - $tema"
                    )
                    listaClases.add(map)
                }

                if (listaClases.isNotEmpty()) {
                    seleccionarClase(listaClases.last())
                } else {
                    tvClaseRegistrada.text = "Sin clases registradas"
                    studentsList.clear()
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                 Toast.makeText(this, "Error cargando clases: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun seleccionarClase(clase: HashMap<String, Any>) {
        claseIdActual = clase["id"] as String
        fechaClaseActual = clase["fechaTimestamp"] as? Timestamp
        tvClaseRegistrada.text = clase["display"] as String
        cargarEstudiantesYAsistencia()
    }

    private fun cargarEstudiantesYAsistencia() {
        val curso = listaCursos.find { it["id"] == cursoIdActual } ?: return
        val studentIds = curso["estudiantes"] as? List<String> ?: emptyList()

        if (studentIds.isEmpty()) {
            studentsList.clear()
            adapter.notifyDataSetChanged()
            return
        }

        studentsList.clear()
        val tempStudents = ArrayList<StudentAttendance>()
        var loadedCount = 0

        studentIds.forEach { id ->
            db.collection("users").document(id).get().addOnSuccessListener { userDoc ->
                val name = userDoc.getString("nombre") ?: "Estudiante"
                tempStudents.add(StudentAttendance(id, name))

                loadedCount++
                if (loadedCount == studentIds.size) {
                    studentsList.addAll(tempStudents.sortedBy { it.name })
                    cargarAsistenciaExistente()
                }
            }
        }
    }

    private fun cargarAsistenciaExistente() {
        db.collection("asistencia")
            .whereEqualTo("claseId", claseIdActual)
            .get()
            .addOnSuccessListener { snap ->
                val attendanceMap = HashMap<String, Pair<String, String?>>()
                for (doc in snap.documents) {
                    val studentId = doc.getString("estudianteId")
                    val status = doc.getString("estado")
                    val observaciones = doc.getString("observaciones")
                    if (studentId != null && status != null) {
                        attendanceMap[studentId] = Pair(status, observaciones)
                    }
                }

                for (student in studentsList) {
                    val attendance = attendanceMap[student.id]
                    if (attendance != null) {
                        student.status = attendance.first
                        student.observaciones = attendance.second
                        // Guardar estado original para detectar cambios
                        student.originalStatus = attendance.first
                        student.originalObservaciones = attendance.second
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun marcarTodos(status: String?) {
        for (student in studentsList) {
            student.status = status
            // Limpiar observaciones cuando se marca como presente o se limpia
            if (status != "J") {
                student.observaciones = null
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun guardarAsistencia() {
        if (studentsList.isEmpty()) {
            Toast.makeText(this, "No hay estudiantes para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        val batch = db.batch()
        var changeCount = 0

        for (student in studentsList) {
            // Solo guardar si hay cambios
            if (student.hasChanges() && student.status != null) {
                val docId = "att_${claseIdActual}_${student.id}"
                val docRef = db.collection("asistencia").document(docId)

                val data = hashMapOf(
                    "cursoId" to cursoIdActual,
                    "claseId" to claseIdActual,
                    "estudianteId" to student.id,
                    "estado" to student.status!!,
                    "fecha" to (fechaClaseActual ?: Timestamp.now())
                )

                // Agregar observaciones si existen
                if (!student.observaciones.isNullOrBlank()) {
                    data["observaciones"] = student.observaciones!!
                }

                batch.set(docRef, data, SetOptions.merge())
                changeCount++
            }
        }

        if (changeCount > 0) {
            batch.commit().addOnSuccessListener {
                Toast.makeText(this, "Asistencia guardada correctamente ($changeCount cambios)", Toast.LENGTH_SHORT).show()
                // Actualizar estado original después de guardar
                for (student in studentsList) {
                    student.originalStatus = student.status
                    student.originalObservaciones = student.observaciones
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
             Toast.makeText(this, "No hay cambios para guardar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarSelectorCursos() {
        val nombres = listaCursos.map { it["nombre"] as String }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Seleccionar curso")
            .setItems(nombres) { _, i ->
                seleccionarCurso(listaCursos[i])
            }
            .show()
    }

    private fun mostrarSelectorClases() {
        val nombres = listaClases.map { it["display"] as String }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Seleccionar clase")
            .setItems(nombres) { _, i ->
                seleccionarClase(listaClases[i])
            }
            .show()
    }
}
