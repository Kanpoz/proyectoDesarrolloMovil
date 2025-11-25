package com.jjcc.proyectmovil.roles.student

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemNota
import com.jjcc.proyectmovil.ui.adapters.CourseGrade
import com.jjcc.proyectmovil.ui.adapters.StudentGradesAdapter
import java.util.Locale

class MisNotas : AppCompatActivity() {

    private lateinit var recyclerCourses: RecyclerView
    private lateinit var tvAnnualAverage: TextView
    private lateinit var tvPeriodAverage: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val courseGrades = mutableListOf<CourseGrade>()
    private lateinit var adapter: StudentGradesAdapter

    // Cache for course names: courseId -> courseName
    private val courseNames = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_notas)
        enableEdgeToEdge()

        initViews()
        loadData()
    }

    private fun initViews() {
        recyclerCourses = findViewById(R.id.rvStudentCourses)
        tvAnnualAverage = findViewById(R.id.tvAnnualAverage)
        tvPeriodAverage = findViewById(R.id.tvPeriodAverage)

        recyclerCourses.layoutManager = LinearLayoutManager(this)
        adapter = StudentGradesAdapter(courseGrades)
        recyclerCourses.adapter = adapter
    }

    private fun loadData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Fetch Courses to get names
        db.collection("cursos")
            .whereArrayContains("estudiantesInscritos", userId)
            .get()
            .addOnSuccessListener { courseDocs ->
                courseNames.clear()
                for (doc in courseDocs) {
                    val name = doc.getString("nombre") ?: "Curso sin nombre"
                    courseNames[doc.id] = name
                }

                // 2. Fetch Evaluations
                fetchEvaluations(userId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar cursos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchEvaluations(userId: String) {
        db.collection("evaluaciones")
            .whereEqualTo("estudianteId", userId)
            .get()
            .addOnSuccessListener { evalDocs ->
                val allEvaluations = evalDocs.mapNotNull { doc ->
                    doc.toObject(ItemNota::class.java).copy(id = doc.id)
                }

                processGrades(allEvaluations)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar notas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun processGrades(evaluations: List<ItemNota>) {
        courseGrades.clear()

        // Group by courseId
        val grouped = evaluations.groupBy { it.cursoId }

        var totalSum = 0.0
        var courseCount = 0

        // Iterate over known courses to show all enrolled courses
        for ((courseId, courseName) in courseNames) {
            val courseEvals = grouped[courseId] ?: emptyList()

            val average = if (courseEvals.isNotEmpty()) {
                courseEvals.mapNotNull { it.nota }.average()
            } else {
                0.0
            }

            if (courseEvals.isNotEmpty()) {
                totalSum += average
                courseCount++
            }

            courseGrades.add(CourseGrade(
                courseId = courseId,
                courseName = courseName,
                average = average,
                evaluations = courseEvals
            ))
        }

        // Calculate overall average
        val overallAverage = if (courseCount > 0) totalSum / courseCount else 0.0

        updateUI(overallAverage)
    }

    private fun updateUI(overallAverage: Double) {
        adapter.notifyDataSetChanged()
        tvAnnualAverage.text = String.format(Locale.getDefault(), "%.1f", overallAverage)
        tvPeriodAverage.text = String.format(Locale.getDefault(), "%.1f", overallAverage)
    }
}
