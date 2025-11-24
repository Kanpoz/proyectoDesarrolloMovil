package com.jjcc.proyectmovil.roles.docente

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        val uid = auth.currentUser?.uid ?: return

        db.collection("clases")
            .whereEqualTo("docenteId", uid)
            .orderBy("fecha")
            .get()
            .addOnSuccessListener { snap ->

                listaClases.clear()

                for (clase in snap.documents) {

                    // Filtrar manualmente SOLO las clases de ese curso
                    if (clase.getString("cursoId") != cursoId) continue

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
                } else {
                    tvClaseRegistrada.text = "Sin clases registradas"
                }
            }
    }

    // =========================================================
    // MOSTRAR SELECTOR DE CLASES
    // =========================================================
    private fun mostrarSelectorClases() {
        if (listaClases.isEmpty()) {
            Toast.makeText(this, "No hay clases disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = listaClases.map {
            "${it["fecha"]} - ${it["tema"]} (${it["hora"]})"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Seleccionar clase")
            .setItems(nombres) { _, i ->
                val clase = listaClases[i]

                claseIdActual = clase["id"] as String
                tvClaseRegistrada.text = nombres[i]

                // Actualizar contenido después de cambiar clase si lo deseas
                // cargarEstudiantesDelCurso(cursoIdActual) // ← opcional
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

        // CARD CONTENEDORA
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(20, 20, 20, 20)
        card.background = ContextCompat.getDrawable(this, R.drawable.bg_card_lila)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 20)
        card.layoutParams = params

        // NOMBRE (arriba)
        val tvNombre = TextView(this)
        tvNombre.text = nombre
        tvNombre.tag = estudianteId
        tvNombre.textSize = 18f
        tvNombre.setTextColor(Color.WHITE)
        tvNombre.maxLines = 1
        tvNombre.ellipsize = TextUtils.TruncateAt.END
        tvNombre.setPadding(0, 0, 0, 16)

        card.addView(tvNombre)

        // LÍNEA HORIZONTAL CON LOS ESTADOS
        val filaBotones = LinearLayout(this)
        filaBotones.orientation = LinearLayout.HORIZONTAL
        filaBotones.gravity = Gravity.CENTER
        filaBotones.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        // Crea botones reales
        val btnP = crearBotonChip("Presente", "#EFE3FF", "#7C4DFF")
        val btnA = crearBotonChip("Ausente", "#EFE3FF", "#7C4DFF")
        val btnT = crearBotonChip("Tardanza", "#EFE3FF", "#7C4DFF")

        // Clicks
        btnP.setOnClickListener { seleccionar(estudianteId, "P", btnP, btnA, btnT) }
        btnA.setOnClickListener { seleccionar(estudianteId, "A", btnP, btnA, btnT) }
        btnT.setOnClickListener { seleccionar(estudianteId, "T", btnP, btnA, btnT) }

        filaBotones.addView(btnP)
        filaBotones.addView(btnA)
        filaBotones.addView(btnT)

        card.addView(filaBotones)

        parent.addView(card)
    }

    private fun crearBotonChip(texto: String, bg: String, color: String): Button {
        val btn = Button(this)
        btn.text = texto
        btn.textSize = 14f

        btn.setBackgroundColor(Color.parseColor(bg))
        btn.setTextColor(Color.parseColor(color))

        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        params.setMargins(6, 0, 6, 0)
        btn.layoutParams = params

        btn.setPadding(10, 20, 10, 20)
        btn.background = ContextCompat.getDrawable(this, R.drawable.bg_chip_button)

        return btn
    }


    private fun seleccionar(id: String, estado: String, p: Button, a: Button, t: Button) {
        estados[id] = estado

        p.setBackgroundColor(Color.parseColor(if (estado == "P") "#7C4DFF" else "#EFE3FF"))
        p.setTextColor(Color.parseColor(if (estado == "P") "#FFFFFF" else "#7C4DFF"))

        a.setBackgroundColor(Color.parseColor(if (estado == "A") "#7C4DFF" else "#EFE3FF"))
        a.setTextColor(Color.parseColor(if (estado == "A") "#FFFFFF" else "#7C4DFF"))

        t.setBackgroundColor(Color.parseColor(if (estado == "T") "#7C4DFF" else "#EFE3FF"))
        t.setTextColor(Color.parseColor(if (estado == "T") "#FFFFFF" else "#7C4DFF"))
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

            val card = layout.getChildAt(i) as LinearLayout

            // tvNombre = card.getChildAt(0)
            val tvNombre = card.getChildAt(0) as TextView
            val estudianteId = tvNombre.tag.toString()

            // filaBotones = card.getChildAt(1)
            val filaBotones = card.getChildAt(1) as LinearLayout

            val p = filaBotones.getChildAt(0) as Button
            val a = filaBotones.getChildAt(1) as Button
            val t = filaBotones.getChildAt(2) as Button

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