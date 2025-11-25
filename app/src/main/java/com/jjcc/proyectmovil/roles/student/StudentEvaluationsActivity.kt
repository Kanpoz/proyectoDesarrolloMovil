package com.jjcc.proyectmovil.roles.student

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.model.Evaluacion
import com.jjcc.proyectmovil.ui.adapters.StudentEvaluationsAdapter

class StudentEvaluationsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var layoutSubjectChips: LinearLayout
    private lateinit var layoutTypeChips: LinearLayout
    private lateinit var rvEvaluations: RecyclerView
    private lateinit var etSearch: EditText

    private val allEvaluations = mutableListOf<Evaluacion>()
    private val filteredEvaluations = mutableListOf<Evaluacion>()
    private lateinit var adapter: StudentEvaluationsAdapter

    private val subjectsMap = mutableMapOf<String, String>() // cursoId -> nombre
    private var selectedSubject: String? = null // null means "Todas"
    private var selectedType: String? = null // null means "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_evaluations)

        initViews()
        setupRecyclerView()
        setupSearch()
        loadStudentCourses()
    }

    private fun initViews() {
        layoutSubjectChips = findViewById(R.id.layoutSubjectChips)
        layoutTypeChips = findViewById(R.id.layoutTypeChips)
        rvEvaluations = findViewById(R.id.rvEvaluations)
        etSearch = findViewById(R.id.etSearch)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup type chips (static)
        setupTypeChips()
    }

    private fun setupRecyclerView() {
        rvEvaluations.layoutManager = LinearLayoutManager(this)
        adapter = StudentEvaluationsAdapter(filteredEvaluations) { evaluation ->
            // TODO: Navigate to evaluation detail
        }
        rvEvaluations.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilters()
            }
        })
    }

    private fun setupTypeChips() {
        val types = listOf("Todos", "Tarea", "Quiz", "Examen")

        for (type in types) {
            val chip = createChip(type)
            chip.setOnClickListener {
                selectedType = if (type == "Todos") null else type
                updateTypeChipStyles()
                applyFilters()
            }
            layoutTypeChips.addView(chip)
        }

        updateTypeChipStyles()
    }

    private fun loadStudentCourses() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("cursos")
            .whereArrayContains("estudiantesInscritos", userId)
            .get()
            .addOnSuccessListener { documents ->
                subjectsMap.clear()
                layoutSubjectChips.removeAllViews()

                // Add "Todas" chip
                val todasChip = createChip("Todas")
                todasChip.setOnClickListener {
                    selectedSubject = null
                    updateSubjectChipStyles()
                    applyFilters()
                }
                layoutSubjectChips.addView(todasChip)

                for (doc in documents) {
                    val cursoId = doc.id
                    val nombre = doc.getString("nombre") ?: "Curso"
                    subjectsMap[cursoId] = nombre

                    val chip = createChip(nombre)
                    chip.tag = cursoId
                    chip.setOnClickListener {
                        selectedSubject = cursoId
                        updateSubjectChipStyles()
                        applyFilters()
                    }
                    layoutSubjectChips.addView(chip)
                }

                updateSubjectChipStyles()
                loadEvaluations()
            }
    }

    private fun loadEvaluations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("evaluaciones")
            .whereEqualTo("estudianteId", userId)
            .get()
            .addOnSuccessListener { documents ->
                allEvaluations.clear()

                for (doc in documents) {
                    val evaluation = doc.toObject(Evaluacion::class.java).copy(id = doc.id)
                    allEvaluations.add(evaluation)
                }

                // Sort by date descending
                allEvaluations.sortByDescending { it.fecha }

                applyFilters()
            }
    }

    private fun applyFilters() {
        val searchQuery = etSearch.text.toString().lowercase()

        filteredEvaluations.clear()
        filteredEvaluations.addAll(
            allEvaluations.filter { evaluation ->
                // Filter by subject
                val matchesSubject = selectedSubject == null || evaluation.cursoId == selectedSubject

                // Filter by type
                val matchesType = selectedType == null || evaluation.tipo == selectedType

                // Filter by search query
                val matchesSearch = searchQuery.isEmpty() ||
                    evaluation.titulo?.lowercase()?.contains(searchQuery) == true ||
                    evaluation.descripcion?.lowercase()?.contains(searchQuery) == true

                matchesSubject && matchesType && matchesSearch
            }
        )

        adapter.notifyDataSetChanged()
    }

    private fun createChip(text: String): TextView {
        val chip = TextView(this)
        chip.text = text
        chip.setPadding(32, 16, 32, 16)
        chip.textSize = 14f

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 16, 0)
        chip.layoutParams = params

        return chip
    }

    private fun updateSubjectChipStyles() {
        for (i in 0 until layoutSubjectChips.childCount) {
            val chip = layoutSubjectChips.getChildAt(i) as? TextView ?: continue
            val cursoId = chip.tag as? String

            val isSelected = (cursoId == null && selectedSubject == null) ||
                            (cursoId == selectedSubject)

            if (isSelected) {
                chip.setBackgroundResource(R.drawable.bg_button_blue)
                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                chip.setBackgroundResource(R.drawable.bg_button_rounded)
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    private fun updateTypeChipStyles() {
        for (i in 0 until layoutTypeChips.childCount) {
            val chip = layoutTypeChips.getChildAt(i) as? TextView ?: continue
            val type = chip.text.toString()

            val isSelected = (type == "Todos" && selectedType == null) ||
                            (type == selectedType)

            if (isSelected) {
                chip.setBackgroundResource(R.drawable.bg_button_blue)
                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                chip.setBackgroundResource(R.drawable.bg_button_rounded)
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }
}
