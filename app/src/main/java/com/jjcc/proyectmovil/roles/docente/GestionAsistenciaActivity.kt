package com.jjcc.proyectmovil.roles.docente

import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R

class GestionAsistenciaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // UI
    private lateinit var tvCursoNombre: TextView
    private lateinit var tvClaseRegistrada: TextView
    private lateinit var containerEstudiantes: FrameLayout
    private lateinit var btnMarcarPresentes: Button
    private lateinit var btnLimpiar: Button
    private lateinit var btnGuardar: Button
    private lateinit var layoutCursoSpinner: LinearLayout
    private lateinit var layoutClaseSpinner: LinearLayout

    // Data
    private var cursoIdActual = ""
    private var claseIdActual = ""

    private val estados = HashMap<String, String>() // estudianteId -> P/A/T

    private var listaCursos = ArrayList<HashMap<String, Any>>()   // Cursos del docente
    private var listaClases = ArrayList<HashMap<String, Any>>()   // Clases del curso seleccionado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_asistencia)

        // ------------ Referencias ----------
        tvCursoNombre = findViewById(R.id.tvCursoNombre)
        tvClaseRegistrada = findViewById(R.id.tvClaseRegistrada)
        containerEstudiantes = findViewById(R.id.containerEstudiantes)
        btnMarcarPresentes = findViewById(R.id.btnMarcarPresentes)
        btnLimpiar = findViewById(R.id.btnLimpiar)
        btnGuardar = findViewById(R.id.btnGuardar)
        layoutCursoSpinner = findViewById(R.id.layoutCursoSpinner)
        layoutClaseSpinner = findViewById(R.id.layoutClaseSpinner)

        layoutCursoSpinner.isClickable = true
        layoutCursoSpinner.isFocusable = true
        layoutClaseSpinner.isClickable = true
        layoutClaseSpinner.isFocusable = true

        layoutCursoSpinner.setOnClickListener { mostrarSelectorCursos() }
        layoutClaseSpinner.setOnClickListener { mostrarSelectorClases() }

        cargarCursosDelDocente()
        configurarBotones()
    }

    // =========================================================
    // CARGAR CURSOS DEL DOCENTE
    // =========================================================
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
                    val curso = listaCursos[0]
                    cursoIdActual = curso["id"] as String
                    tvCursoNombre.text = curso["nombre"] as String

                    cargarClasesParaCurso(cursoIdActual)
                    cargarEstudiantesDelCurso(cursoIdActual)
                }
            }
    }

    // =========================================================
    // MOSTRAR SELECTOR DE CURSOS
    // =========================================================
    private fun mostrarSelectorCursos() {
        if (listaCursos.isEmpty()) return

        val nombres = listaCursos.map { it["nombre"] as String }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Seleccionar curso")
            .setItems(nombres) { _, i ->
                val curso = listaCursos[i]

                cursoIdActual = curso["id"] as String
                tvCursoNombre.text = curso["nombre"] as String

                cargarClasesParaCurso(cursoIdActual)
                cargarEstudiantesDelCurso(cursoIdActual)
            }
            .show()
    }

    // =========================================================
    // CARGAR CLASES
    // =========================================================
    private fun cargarClasesParaCurso(cursoId: String) {

        db.collection("clases")
            .whereEqualTo("cursoId", cursoId)
            .orderBy("fecha")
            .get()
            .addOnSuccessListener { snap ->

                listaClases.clear()

                for (clase in snap.documents) {
                    val fecha = clase.getTimestamp("fecha")?.toDate()
                    val tema = clase.getString("tema") ?: "Clase"

                    val map = hashMapOf<String, Any>(
                        "id" to clase.id,
                        "tema" to tema,
                        "fecha" to android.text.format.DateFormat.format("dd/MM/yyyy", fecha).toString(),
                        "hora" to android.text.format.DateFormat.format("HH:mm", fecha).toString()
                    )
                    listaClases.add(map)
                }

                if (listaClases.isNotEmpty()) {
                    val clase = listaClases.last()
                    claseIdActual = clase["id"] as String

                    tvClaseRegistrada.text =
                        "${clase["fecha"]} - ${clase["tema"]} (${clase["hora"]})"
                }
            }
    }

    // =========================================================
    // MOSTRAR SELECTOR DE CLASES
    // =========================================================
    private fun mostrarSelectorClases() {
        if (listaClases.isEmpty()) return

        val nombres = listaClases.map {
            "${it["fecha"]} - ${it["tema"]}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Seleccionar clase")
            .setItems(nombres) { _, i ->
                val clase = listaClases[i]

                claseIdActual = clase["id"] as String
                tvClaseRegistrada.text = nombres[i]
            }
            .show()
    }

    // =========================================================
    // CARGAR ESTUDIANTES
    // =========================================================
    private fun cargarEstudiantesDelCurso(cursoId: String) {

        db.collection("cursos").document(cursoId)
            .get()
            .addOnSuccessListener { curso ->

                val listaIds = curso.get("estudiantesInscritos") as? List<String> ?: emptyList()

                val contenedor = LinearLayout(this)
                contenedor.orientation = LinearLayout.VERTICAL
                contenedor.setPadding(12, 12, 12, 12)

                containerEstudiantes.removeAllViews()
                containerEstudiantes.addView(contenedor)

                listaIds.forEach { id ->
                    db.collection("users").document(id)
                        .get()
                        .addOnSuccessListener { user ->

                            val nombre = user.getString("nombre") ?: "Estudiante"
                            agregarItemEstudiante(contenedor, id, nombre)
                        }
                }
            }
    }

    // =========================================================
    // AGREGAR ITEM ESTUDIANTE
    // =========================================================
    private fun agregarItemEstudiante(parent: LinearLayout, estudianteId: String, nombre: String) {

        val item = LinearLayout(this)
        item.orientation = LinearLayout.HORIZONTAL
        item.gravity = Gravity.CENTER_VERTICAL
        item.setPadding(12, 18, 12, 18)

        val tvNombre = TextView(this)
        tvNombre.text = nombre
        tvNombre.tag = estudianteId
        tvNombre.textSize = 16f
        tvNombre.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val btnP = crearBotonEstado("Presente", "#7C4DFF", "#FFFFFF")
        val btnA = crearBotonEstado("Ausente", "#EFE3FF", "#7C4DFF")
        val btnT = crearBotonEstado("Tardanza", "#EFE3FF", "#7C4DFF")

        item.addView(tvNombre)
        item.addView(btnP)
        item.addView(btnA)
        item.addView(btnT)

        btnP.setOnClickListener { seleccionar(estudianteId, "P", btnP, btnA, btnT) }
        btnA.setOnClickListener { seleccionar(estudianteId, "A", btnP, btnA, btnT) }
        btnT.setOnClickListener { seleccionar(estudianteId, "T", btnP, btnA, btnT) }

        parent.addView(item)
    }

    private fun crearBotonEstado(texto: String, bg: String, color: String): Button {
        val btn = Button(this)
        btn.text = texto
        btn.setBackgroundColor(android.graphics.Color.parseColor(bg))
        btn.setTextColor(android.graphics.Color.parseColor(color))
        btn.textSize = 12f
        btn.setPadding(24, 12, 24, 12)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(12, 0, 0, 0)
        btn.layoutParams = params
        return btn
    }

    private fun seleccionar(id: String, estado: String, p: Button, a: Button, t: Button) {
        estados[id] = estado

        p.setBackgroundColor(android.graphics.Color.parseColor(if (estado == "P") "#7C4DFF" else "#EFE3FF"))
        p.setTextColor(android.graphics.Color.parseColor(if (estado == "P") "#FFFFFF" else "#7C4DFF"))

        a.setBackgroundColor(android.graphics.Color.parseColor(if (estado == "A") "#7C4DFF" else "#EFE3FF"))
        a.setTextColor(android.graphics.Color.parseColor(if (estado == "A") "#FFFFFF" else "#7C4DFF"))

        t.setBackgroundColor(android.graphics.Color.parseColor(if (estado == "T") "#7C4DFF" else "#EFE3FF"))
        t.setTextColor(android.graphics.Color.parseColor(if (estado == "T") "#FFFFFF" else "#7C4DFF"))
    }

    // =========================================================
    // GUARDAR
    // =========================================================
    private fun configurarBotones() {
        btnMarcarPresentes.setOnClickListener {
            marcarTodoComo("P")
        }

        btnLimpiar.setOnClickListener {
            estados.clear()
            recreate()
        }

        btnGuardar.setOnClickListener {
            guardarAsistencia()
        }
    }

    private fun marcarTodoComo(estado: String) {
        val layout = containerEstudiantes.getChildAt(0) as LinearLayout

        for (i in 0 until layout.childCount) {
            val item = layout.getChildAt(i) as LinearLayout
            val nombre = item.getChildAt(0) as TextView
            val estudianteId = nombre.tag.toString()

            val p = item.getChildAt(1) as Button
            val a = item.getChildAt(2) as Button
            val t = item.getChildAt(3) as Button

            seleccionar(estudianteId, estado, p, a, t)
        }
    }

    private fun guardarAsistencia() {

        if (estados.isEmpty()) {
            Toast.makeText(this, "No seleccionaste asistencia", Toast.LENGTH_SHORT).show()
            return
        }

        for (item in estados) {
            val data = hashMapOf(
                "cursoId" to cursoIdActual,
                "claseId" to claseIdActual,
                "estudianteId" to item.key,
                "estado" to item.value,
                "fecha" to Timestamp.now()
            )

            db.collection("asistencia").add(data)
        }

        Toast.makeText(this, "Asistencia guardada", Toast.LENGTH_LONG).show()
        finish()
    }
}