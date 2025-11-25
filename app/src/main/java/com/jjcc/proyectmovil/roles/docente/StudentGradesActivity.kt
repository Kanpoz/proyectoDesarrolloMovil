package com.jjcc.proyectmovil.roles.docente

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.model.Evaluacion
import com.jjcc.proyectmovil.ui.adapters.StudentGradeItem
import com.jjcc.proyectmovil.ui.adapters.StudentGradeEditAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentGradesActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: StudentGradeEditAdapter
    private val studentItems = mutableListOf<StudentGradeItem>()

    // We now filter by Title and CourseId to find all student docs for this assignment
    private var evaluationTitle: String? = null
    private var cursoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_grades)

        // We receive title and cursoId from the previous screen
        evaluationTitle = intent.getStringExtra("titulo")
        cursoId = intent.getStringExtra("cursoId")

        val notaMaxima = intent.getDoubleExtra("notaMaxima", 5.0)
        val fechaLong = intent.getLongExtra("fecha", 0L)

        setupHeader(evaluationTitle ?: "Evaluación", notaMaxima, fechaLong)
        setupRecyclerView()

        findViewById<Button>(R.id.btnSaveChanges).setOnClickListener {
            saveChanges()
        }

        findViewById<ImageView>(R.id.btnBackGrades).setOnClickListener {
            finish()
        }

        if (evaluationTitle != null && cursoId != null) {
            loadGrades()
        } else {
            Toast.makeText(this, "Error: Missing evaluation data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupHeader(titulo: String, notaMaxima: Double, fechaLong: Long) {
        findViewById<TextView>(R.id.tvEvaluationTitle).text = "Evaluation: $titulo"
        findViewById<TextView>(R.id.tvMaxGrade).text = "Max Grade: $notaMaxima"

        if (fechaLong > 0) {
            val date = Date(fechaLong)
            val format = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            findViewById<TextView>(R.id.tvEvaluationDate).text = "Due: ${format.format(date)}"
        }
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvStudentGrades)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = StudentGradeEditAdapter(studentItems)
        rv.adapter = adapter
    }

    private fun loadGrades() {
        val title = evaluationTitle ?: return
        val cid = cursoId ?: return

        // Query 'evaluaciones' collection where titulo == title AND cursoId == cid
        db.collection("evaluaciones")
            .whereEqualTo("cursoId", cid)
            .whereEqualTo("titulo", title)
            .get()
            .addOnSuccessListener { documents ->
                studentItems.clear()
                val pendingLookups = documents.size()

                if (pendingLookups == 0) {
                    Toast.makeText(this, "No students found for this evaluation", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                var completedLookups = 0

                for (doc in documents) {
                    val evaluacion = doc.toObject(Evaluacion::class.java)
                    val studentId = evaluacion.estudianteId ?: continue
                    val currentGrade = evaluacion.nota
                    val docId = doc.id

                    // Lookup student details in 'estudiantes' collection (or 'users' if that's where they are)
                    // Trying 'users' first as it's more common in this project structure based on previous files
                    // If 'estudiantes' is strictly required, swap to "estudiantes"
                    db.collection("users").document(studentId).get()
                        .addOnSuccessListener { studentDoc ->
                            val name = studentDoc.getString("nombre") ?: "Unknown Student"
                            val photo = studentDoc.getString("fotoUrl") ?: ""

                            val item = StudentGradeItem(
                                studentId = studentId,
                                studentName = name,
                                studentPhoto = photo,
                                grade = currentGrade,
                                noteId = docId // This is the ID of the evaluation document itself
                            )
                            studentItems.add(item)

                            completedLookups++
                            checkCompletion(completedLookups, pendingLookups)
                        }
                        .addOnFailureListener {
                            // Try 'estudiantes' collection as fallback if 'users' fails or is empty?
                            // For now just mark complete
                            completedLookups++
                            checkCompletion(completedLookups, pendingLookups)
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading grades: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkCompletion(current: Int, total: Int) {
        if (current == total) {
            // Sort by name for better UX
            studentItems.sortBy { it.studentName }
            adapter.notifyDataSetChanged()
        }
    }

    private fun saveChanges() {
        // Validate grades before saving
        for (item in studentItems) {
            val grade = item.grade
            if (grade != null && (grade < 0.0 || grade > 5.0)) {
                Toast.makeText(this, "Error: Las notas deben estar entre 0.0 y 5.0", Toast.LENGTH_LONG).show()
                return
            }
        }

        val batch = db.batch()

        for (item in studentItems) {
            if (item.noteId != null) {
                // Update the 'nota' field in the 'evaluaciones' document
                val ref = db.collection("evaluaciones").document(item.noteId!!)
                batch.update(ref, "nota", item.grade)
            }
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving changes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
