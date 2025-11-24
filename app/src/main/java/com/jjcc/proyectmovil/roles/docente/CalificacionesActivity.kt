package com.jjcc.proyectmovil.roles.docente

import android.os.Bundle
import android.text.*
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import java.util.*
import kotlin.collections.ArrayList

class CalificacionesActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var tvClaseSeleccionada: TextView
    private lateinit var layoutClaseSpinner: LinearLayout
    private lateinit var containerFilas: LinearLayout
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private var cursoIdActual: String? = null

    private val listaCursos = ArrayList<CursoUi>()
    private val filasEstudiantes = ArrayList<FilaCalificacion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calificaciones)

        tvClaseSeleccionada = findViewById(R.id.tvClaseSeleccionada)
        layoutClaseSpinner = findViewById(R.id.layoutClaseSpinner)
        containerFilas = findViewById(R.id.containerFilasCalificaciones)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)

        layoutClaseSpinner.isClickable = true
        layoutClaseSpinner.isFocusable = true

        layoutClaseSpinner.setOnClickListener { mostrarSelectorCursos() }

        btnCancelar.setOnClickListener { finish() }
        btnGuardar.setOnClickListener { guardarCalificaciones() }

        cargarCursosDelDocente()
    }

    // ---------------------- MODELOS ----------------------

    data class CursoUi(
        val id: String,
        val nombre: String,
        val estudiantesIds: List<String>
    )

    data class FilaCalificacion(
        val estudianteId: String,
        val nombre: String,
        val etCognitiva: EditText,
        val etProcedimental: EditText,
        val etActitudinal: EditText,
        val tvTotal: TextView
    )

    // ---------------------- FILTRO DECIMAL 0.0 – 5.0 ----------------------

    class DecimalMinMaxFilter(private val min: Float, private val max: Float) : InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {

            val newValue = (dest.toString().substring(0, dstart) +
                    source.toString() +
                    dest.toString().substring(dend))

            // Permitir borrar
            if (newValue.isBlank()) return null

            // Permitir "0.", "4.", etc. mientras el usuario escribe
            if (newValue.endsWith(".")) return null

            // Permitir formatos decimales correctos: 0-5 con decimales
            if (!newValue.matches(Regex("^\\d{1}(\\.\\d{0,2})?$"))) {
                return ""
            }

            try {
                val value = newValue.toFloat()
                if (value < min || value > max) return ""
            } catch (e: Exception) {
                return ""
            }

            return null
        }
    }

    // ---------------------- CARGAR CURSOS ----------------------

    private fun cargarCursosDelDocente() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("cursos")
            .whereEqualTo("docenteId", uid)
            .get()
            .addOnSuccessListener { snap ->
                listaCursos.clear()

                for (doc in snap.documents) {
                    val nombre = doc.getString("nombre") ?: "Curso"
                    val estudiantes = doc.get("estudiantesInscritos") as? List<String> ?: emptyList()

                    listaCursos.add(CursoUi(doc.id, nombre, estudiantes))
                }

                if (listaCursos.isNotEmpty()) {
                    val curso = listaCursos.first()
                    cursoIdActual = curso.id
                    tvClaseSeleccionada.text = curso.nombre
                    cargarEstudiantesDelCurso(curso)
                } else {
                    tvClaseSeleccionada.text = "Sin cursos asignados"
                }
            }
    }

    // ---------------------- SPINNER VISUAL ----------------------

    private fun mostrarSelectorCursos() {
        if (listaCursos.isEmpty()) return

        val nombres = listaCursos.map { it.nombre }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Seleccionar clase")
            .setItems(nombres) { _, which ->
                val curso = listaCursos[which]
                cursoIdActual = curso.id
                tvClaseSeleccionada.text = curso.nombre
                cargarEstudiantesDelCurso(curso)
            }
            .show()
    }

    // ---------------------- CARGAR ESTUDIANTES ----------------------

    private fun cargarEstudiantesDelCurso(curso: CursoUi) {
        containerFilas.removeAllViews()
        filasEstudiantes.clear()

        if (curso.estudiantesIds.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No hay estudiantes inscritos"
            tv.setPadding(12, 12, 12, 12)
            containerFilas.addView(tv)
            return
        }

        for (id in curso.estudiantesIds) {
            db.collection("users").document(id).get()
                .addOnSuccessListener { student ->
                    val nombre = student.getString("nombre") ?: "Estudiante"
                    agregarCardEstudiante(id, nombre)
                }
        }
    }

    // ---------------------- CREAR CARD ----------------------

    private fun agregarCardEstudiante(id: String, nombre: String) {

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(18, 18, 18, 18)
        card.background = ContextCompat.getDrawable(this, R.drawable.bg_card_lila)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 24)
        card.layoutParams = params

        // -------------------- NOMBRE --------------------
        val tvNombre = TextView(this)
        tvNombre.text = nombre
        tvNombre.textSize = 18f
        tvNombre.setTextColor(R.color.white)
        tvNombre.setPadding(0, 0, 0, 12)
        card.addView(tvNombre)

        // -------------------- FILA COGNITIVA --------------------
        val filaCog = crearFilaNota("Cognitiva (0-5):")
        val etCog = filaCog.second
        card.addView(filaCog.first)

        // -------------------- FILA PROCEDIMENTAL --------------------
        val filaProc = crearFilaNota("Procedimental (0-5):")
        val etProc = filaProc.second
        card.addView(filaProc.first)

        // -------------------- FILA ACTITUDINAL --------------------
        val filaAct = crearFilaNota("Actitudinal (0-5):")
        val etAct = filaAct.second
        card.addView(filaAct.first)

        // -------------------- TOTAL (mantiene colores dinámicos) --------------------
        val tvTotal = TextView(this)
        tvTotal.text = "Total: -"
        tvTotal.textSize = 17f
        tvTotal.setPadding(0, 16, 0, 0)
        // ❗️ NO cambiar color aquí. Se pinta dinámico en recalcularTotal()
        card.addView(tvTotal)

        // Lista interna
        val fila = FilaCalificacion(id, nombre, etCog, etProc, etAct, tvTotal)
        filasEstudiantes.add(fila)

        // Watchers para recalcular
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = recalcularTotal(fila)
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etCog.addTextChangedListener(watcher)
        etProc.addTextChangedListener(watcher)
        etAct.addTextChangedListener(watcher)

        containerFilas.addView(card)
    }

    // ---------------------- FILA DE NOTA ----------------------

    private fun crearFilaNota(label: String): Pair<LinearLayout, EditText> {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.gravity = Gravity.CENTER_VERTICAL
        layout.setPadding(0, 6, 0, 6)

        val tv = TextView(this)
        tv.text = label
        tv.textSize = 15f
        tv.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val et = EditText(this)
        et.hint = "-"
        et.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
        et.filters = arrayOf<InputFilter>(DecimalMinMaxFilter(0f, 5f))

        val paramsET = LinearLayout.LayoutParams(120, LinearLayout.LayoutParams.WRAP_CONTENT)
        et.layoutParams = paramsET

        layout.addView(tv)
        layout.addView(et)

        return Pair(layout, et)
    }

    // ---------------------- TOTAL ----------------------

    private fun recalcularTotal(fila: FilaCalificacion) {

        // Convertir valores
        val c = fila.etCognitiva.text.toString().toFloatOrNull() ?: 0f
        val p = fila.etProcedimental.text.toString().toFloatOrNull() ?: 0f
        val a = fila.etActitudinal.text.toString().toFloatOrNull() ?: 0f

        // ---- PESOS ACADÉMICOS ----
        val pesoC = 0.4f
        val pesoP = 0.4f
        val pesoA = 0.2f

        // ---- NOTA ACADÉMICA ----
        val total = (c * pesoC) + (p * pesoP) + (a * pesoA)

        // Mostrar con un decimal
        fila.tvTotal.text = "Total: ${String.format(Locale.US, "%.1f", total)}"

        // Color según desempeño
        val color = when {
            total >= 4.0 -> R.color.verde_nota           // Excelente
            total >= 3.0 -> R.color.naranja_nota         // Aceptable
            else -> R.color.rojo_nota                    // Bajo
        }

        fila.tvTotal.setTextColor(ContextCompat.getColor(this, color))
    }

    // ---------------------- GUARDAR ----------------------

    private fun guardarCalificaciones() {
        val cursoId = cursoIdActual ?: return

        for (fila in filasEstudiantes) {

            val cog = fila.etCognitiva.text.toString().toFloatOrNull() ?: 0f
            val pro = fila.etProcedimental.text.toString().toFloatOrNull() ?: 0f
            val act = fila.etActitudinal.text.toString().toFloatOrNull() ?: 0f
            val total = cog + pro + act

            val data = hashMapOf(
                "cursoId" to cursoId,
                "estudianteId" to fila.estudianteId,
                "cognitiva" to cog,
                "procedimental" to pro,
                "actitudinal" to act,
                "total" to total,
                "timestamp" to Timestamp.now()
            )

            db.collection("calificaciones").add(data)
        }

        Toast.makeText(this, "Calificaciones guardadas", Toast.LENGTH_LONG).show()
        finish()
    }
}