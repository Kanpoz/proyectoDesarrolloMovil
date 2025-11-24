package com.jjcc.proyectmovil.roles.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import java.util.UUID

class EditarCursoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tvTituloEditarCurso: TextView
    private lateinit var etAsignatura: EditText
    private lateinit var etGrado: EditText
    private lateinit var etSeccion: EditText
    private lateinit var etPeriodo: EditText
    private lateinit var etAula: EditText
    private lateinit var spDocente: Spinner
    private lateinit var swActivo: SwitchCompat
    private lateinit var etIdEstudiante: EditText
    private lateinit var tvListaEstudiantesIds: TextView
    private lateinit var btnAddEstudiante: Button
    private lateinit var btnRemoveEstudiante: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private var cursoId: String = ""
    private var isNuevoCurso: Boolean = false
    private var docenteIdSeleccionado: String? = null
    private val docentesIds = mutableListOf<String>()
    private val docentesNombres = mutableListOf<String>()
    private val estudiantesIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_curso)

        db = FirebaseFirestore.getInstance()

        tvTituloEditarCurso = findViewById(R.id.tvTituloEditarCurso)
        etAsignatura = findViewById(R.id.etAsignatura)
        etGrado = findViewById(R.id.etGrado)
        etSeccion = findViewById(R.id.etSeccion)
        etPeriodo = findViewById(R.id.etPeriodo)
        etAula = findViewById(R.id.etAula)
        spDocente = findViewById(R.id.spDocente)
        swActivo = findViewById(R.id.swActivoCurso)

        etIdEstudiante = findViewById(R.id.etIdEstudiante)
        tvListaEstudiantesIds = findViewById(R.id.tvListaEstudiantesIds)
        btnAddEstudiante = findViewById(R.id.btnAddEstudiante)
        btnRemoveEstudiante = findViewById(R.id.btnRemoveEstudiante)
        btnGuardar = findViewById(R.id.btnGuardarCurso)
        btnCancelar = findViewById(R.id.btnCancelarEdicionCurso)

        cursoId = intent.getStringExtra("cursoId") ?: ""
        isNuevoCurso = cursoId.isBlank()

        btnCancelar.setOnClickListener { finish() }
        btnAddEstudiante.setOnClickListener { agregarEstudianteId() }
        btnRemoveEstudiante.setOnClickListener { eliminarEstudianteId() }
        btnGuardar.setOnClickListener { guardarCambios() }

        if (isNuevoCurso) {
            // Modo NUEVO CURSO
            tvTituloEditarCurso.text = "Nuevo curso"
            btnGuardar.text = "Crear Curso"
            swActivo.isChecked = true
            estudiantesIds.clear()
            actualizarListaEstudiantesTexto()
            cargarDocentes(null)
        } else {
            // Modo EDITAR
            cargarCurso()
        }
    }

    private fun cargarCurso() {
        db.collection("cursos")
            .document(cursoId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "No se encontró el curso", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val nombre = doc.getString("nombre") ?: ""
                val asignatura = doc.getString("asignatura") ?: ""
                val grado = doc.getString("grado") ?: ""
                val seccion = doc.getString("seccion") ?: ""
                val periodo = doc.getString("periodo") ?: ""
                val aula = doc.getString("aula") ?: ""
                val activo = doc.getBoolean("activo") ?: true
                val docenteId = doc.getString("docenteId") ?: ""

                tvTituloEditarCurso.text = "Edición del Curso: $nombre"

                etAsignatura.setText(asignatura)
                etGrado.setText(grado)
                etSeccion.setText(seccion)
                etPeriodo.setText(periodo)
                etAula.setText(aula)
                swActivo.isChecked = activo

                docenteIdSeleccionado = docenteId

                val lista = doc.get("estudiantesInscritos") as? List<String> ?: emptyList()
                estudiantesIds.clear()
                estudiantesIds.addAll(lista)
                actualizarListaEstudiantesTexto()

                cargarDocentes(docenteIdSeleccionado)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando curso: ${it.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun cargarDocentes(docenteIdActual: String?) {
        docentesIds.clear()
        docentesNombres.clear()

        docentesIds.add("")
        docentesNombres.add("Selecciona un docente")

        db.collection("users")
            .whereEqualTo("rol", "DOCENTE")
            .get()
            .addOnSuccessListener { snap ->
                for (doc in snap.documents) {
                    val id = doc.id
                    val nombre = doc.getString("nombre") ?: "(Sin nombre)"
                    docentesIds.add(id)
                    docentesNombres.add(nombre)
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    docentesNombres
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spDocente.adapter = adapter

                if (!docenteIdActual.isNullOrBlank()) {
                    val index = docentesIds.indexOf(docenteIdActual)
                    if (index >= 0) spDocente.setSelection(index)
                }

                spDocente.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: android.view.View?,
                        position: Int,
                        id: Long
                    ) {
                        docenteIdSeleccionado = if (position == 0) null else docentesIds[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando docentes: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarListaEstudiantesTexto() {
        tvListaEstudiantesIds.text =
            if (estudiantesIds.isEmpty()) "Sin estudiantes inscritos"
            else estudiantesIds.joinToString("\n") { id -> "ID: $id" }
    }

    private fun agregarEstudianteId() {
        val id = etIdEstudiante.text.toString().trim()
        if (id.isEmpty()) {
            Toast.makeText(this, "Escribe un ID de estudiante", Toast.LENGTH_SHORT).show()
            return
        }
        if (estudiantesIds.contains(id)) {
            Toast.makeText(this, "Ese ID ya está inscrito", Toast.LENGTH_SHORT).show()
            return
        }
        estudiantesIds.add(id)
        etIdEstudiante.text.clear()
        actualizarListaEstudiantesTexto()
    }

    private fun eliminarEstudianteId() {
        val id = etIdEstudiante.text.toString().trim()
        if (id.isEmpty()) {
            Toast.makeText(this, "Escribe el ID que quieres eliminar", Toast.LENGTH_SHORT).show()
            return
        }
        val removed = estudiantesIds.remove(id)
        Toast.makeText(
            this,
            if (removed) "ID eliminado" else "Ese ID no estaba inscrito",
            Toast.LENGTH_SHORT
        ).show()
        etIdEstudiante.text.clear()
        actualizarListaEstudiantesTexto()
    }

    private fun guardarCambios() {
        val asignatura = etAsignatura.text.toString().trim()
        val grado = etGrado.text.toString().trim()
        val seccion = etSeccion.text.toString().trim()
        val periodo = etPeriodo.text.toString().trim()
        val aula = etAula.text.toString().trim()
        val activo = swActivo.isChecked

        if (asignatura.isEmpty() || grado.isEmpty() || seccion.isEmpty()
            || periodo.isEmpty() || aula.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Completa todos los campos de datos generales",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val nombreCurso = "$asignatura ${grado}° $seccion"

        val base = mutableMapOf<String, Any>(
            "asignatura" to asignatura,
            "grado" to grado,
            "seccion" to seccion,
            "periodo" to periodo,
            "aula" to aula,
            "activo" to activo,
            "nombre" to nombreCurso,
            "estudiantesInscritos" to estudiantesIds.toList(),
            "cantidadEstudiantes" to estudiantesIds.size
        )

        docenteIdSeleccionado?.let { base["docenteId"] = it }

        btnGuardar.isEnabled = false

        if (isNuevoCurso) {
            val newDocRef = db.collection("cursos").document()
            val uuid = UUID.randomUUID().toString()

            base["id"] = uuid
            base["fechaCreacion"] = FieldValue.serverTimestamp()
            // horario, periodo, etc. adicionales los puedes agregar después

            newDocRef.set(base)
                .addOnSuccessListener {
                    btnGuardar.isEnabled = true
                    Toast.makeText(this, "Curso creado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    btnGuardar.isEnabled = true
                    Toast.makeText(this, "Error al crear: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection("cursos")
                .document(cursoId)
                .update(base)
                .addOnSuccessListener {
                    btnGuardar.isEnabled = true
                    Toast.makeText(this, "Curso actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    btnGuardar.isEnabled = true
                    Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}