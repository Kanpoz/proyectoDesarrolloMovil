package com.jjcc.proyectmovil.roles.docente

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.model.Evaluacion
import com.jjcc.proyectmovil.ui.adapters.EvaluationsAdapter
import java.text.SimpleDateFormat
import java.util.*

class EvaluationsListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: EvaluationsAdapter
    private val evaluationsList = mutableListOf<Evaluacion>()
    private var cursoId: String? = null

    // Course selection
    private lateinit var layoutCourseChips: LinearLayout
    private val coursesMap = mutableMapOf<String, String>() // id -> name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluations_list)

        layoutCourseChips = findViewById(R.id.layoutCourseChips)
        setupUI()

        cursoId = intent.getStringExtra("cursoId")

        if (cursoId == null) {
            fetchAllCourses()
        } else {
            fetchAllCourses()
        }
    }

    private fun fetchAllCourses() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("cursos")
            .whereEqualTo("docenteId", uid)
            .get()
            .addOnSuccessListener { documents ->
                coursesMap.clear()
                layoutCourseChips.removeAllViews()

                if (documents.isEmpty) {
                    Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                var firstId: String? = null

                for (doc in documents) {
                    val id = doc.id
                    val name = doc.getString("nombre") ?: "Curso"
                    coursesMap[id] = name

                    if (firstId == null) firstId = id

                    addCourseChip(id, name)
                }

                // If we didn't have a courseId, select the first one
                if (cursoId == null && firstId != null) {
                    selectCourse(firstId)
                } else if (cursoId != null) {
                    selectCourse(cursoId!!)
                }
            }
    }

    private fun addCourseChip(id: String, name: String) {
        val chip = TextView(this)
        chip.text = name
        chip.setPadding(32, 16, 32, 16)
        chip.textSize = 14f

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 16, 0)
        chip.layoutParams = params

        chip.setOnClickListener {
            selectCourse(id)
        }

        // Tag to identify for styling updates
        chip.tag = id

        layoutCourseChips.addView(chip)
    }

    private fun selectCourse(id: String) {
        cursoId = id
        updateChipStyles()
        loadEvaluations()
    }

    private fun updateChipStyles() {
        for (i in 0 until layoutCourseChips.childCount) {
            val child = layoutCourseChips.getChildAt(i) as? TextView ?: continue
            val id = child.tag as? String ?: continue

            if (id == cursoId) {
                child.setBackgroundResource(R.drawable.bg_button_blue)
                child.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                child.setBackgroundResource(R.drawable.bg_button_rounded)
                child.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    private fun setupUI() {
        val rvEvaluations = findViewById<RecyclerView>(R.id.rvEvaluations)
        rvEvaluations.layoutManager = LinearLayoutManager(this)

        adapter = EvaluationsAdapter(evaluationsList) { evaluation ->
            val intent = Intent(this, StudentGradesActivity::class.java)
            intent.putExtra("evaluacionId", evaluation.id)
            intent.putExtra("titulo", evaluation.titulo ?: "Sin título")
            intent.putExtra("notaMaxima", evaluation.notaMaxima ?: 5.0)
            intent.putExtra("fecha", evaluation.fecha?.time ?: 0L)
            intent.putExtra("cursoId", cursoId) // Pass current course ID
            startActivity(intent)
        }
        rvEvaluations.adapter = adapter

        findViewById<ImageView>(R.id.btnBackEvaluations).setOnClickListener {
            finish()
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddEvaluation).setOnClickListener {
            showCreateEvaluationDialog()
        }
    }

    private fun loadEvaluations() {
        val cid = cursoId ?: return
        db.collection("evaluaciones")
            .whereEqualTo("cursoId", cid)
            .get()
            .addOnSuccessListener { documents ->
                evaluationsList.clear()
                val uniqueTitles = mutableSetOf<String>()

                for (document in documents) {
                    val evaluation = document.toObject(Evaluacion::class.java).copy(id = document.id)
                    val title = evaluation.titulo ?: "Sin título"

                    // Only add unique titles to the list to avoid duplicates per student
                    if (!uniqueTitles.contains(title)) {
                        uniqueTitles.add(title)
                        evaluationsList.add(evaluation)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading evaluations: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ============================================================
    // CREATE EVALUATION LOGIC
    // ============================================================

    private fun showCreateEvaluationDialog() {
        if (cursoId == null) {
            Toast.makeText(this, "Selecciona un curso primero", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_evaluation, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etMaxGrade = dialogView.findViewById<EditText>(R.id.etMaxGrade)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerType)

        // Date Picker
        val calendar = Calendar.getInstance()
        var selectedDate: Date = calendar.time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(selectedDate)

        tvDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.time
                tvDate.text = dateFormat.format(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Type Spinner
        val types = arrayOf("Tarea", "Examen", "Proyecto")
        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        AlertDialog.Builder(this)
            .setTitle("Nueva Evaluación")
            .setView(dialogView)
            .setPositiveButton("Crear") { _, _ ->
                val title = etTitle.text.toString()
                val desc = etDescription.text.toString()
                val maxGradeStr = etMaxGrade.text.toString()
                val type = spinnerType.selectedItem.toString()

                if (title.isNotEmpty() && maxGradeStr.isNotEmpty()) {
                    val maxGrade = maxGradeStr.toDoubleOrNull() ?: 5.0
                    createEvaluationForStudents(title, desc, maxGrade, selectedDate, type)
                } else {
                    Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createEvaluationForStudents(
        title: String,
        desc: String,
        maxGrade: Double,
        date: Date,
        type: String
    ) {
        val cid = cursoId ?: return

        // 1. Get students from course
        db.collection("cursos").document(cid).get()
            .addOnSuccessListener { courseDoc ->
                val students = courseDoc.get("estudiantesInscritos") as? List<String> ?: emptyList()

                if (students.isEmpty()) {
                    Toast.makeText(this, "No hay estudiantes en este curso", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                val uid = auth.currentUser?.uid ?: ""

                // 2. Create evaluation doc for each student
                for (studentId in students) {
                    val newDocRef = db.collection("evaluaciones").document()
                    val evaluation = hashMapOf(
                        "cursoId" to cid,
                        "docenteId" to uid,
                        "estudianteId" to studentId,
                        "titulo" to title,
                        "descripcion" to desc,
                        "notaMaxima" to maxGrade,
                        "fecha" to Timestamp(date),
                        "tipo" to type,
                        "nota" to null, // Initial grade is null
                        "timestamp" to Timestamp.now()
                    )
                    batch.set(newDocRef, evaluation)
                }

                // 3. Commit batch
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Evaluación creada para ${students.size} estudiantes", Toast.LENGTH_LONG).show()
                        loadEvaluations() // Refresh list
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al crear: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener curso", Toast.LENGTH_SHORT).show()
            }
    }
}
