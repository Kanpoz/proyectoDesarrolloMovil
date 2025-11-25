package com.jjcc.proyectmovil.roles.student

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.Clase
import com.jjcc.proyectmovil.ui.adapters.ClasesAdapter
import java.util.Date

class MisClasesActivity : AppCompatActivity() {

    private lateinit var rvClases: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnBack: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val clasesList = mutableListOf<Clase>()
    private val courseNamesMap = mutableMapOf<String, String>()
    private lateinit var adapter: ClasesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_clases)

        initViews()
        fetchData()
    }

    private fun initViews() {
        rvClases = findViewById(R.id.rvClases)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnBack = findViewById(R.id.btnBack)

        rvClases.layoutManager = LinearLayoutManager(this)
        adapter = ClasesAdapter(clasesList, courseNamesMap) { clase ->
            val intent = Intent(this, DetalleClaseActivity::class.java)
            intent.putExtra("CLASE_ID", clase.id)
            intent.putExtra("CURSO_NAME", courseNamesMap[clase.cursoId])
            startActivity(intent)
        }
        rvClases.adapter = adapter

        btnBack.setOnClickListener { finish() }
    }

    private fun fetchData() {
        val userId = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE

        db.collection("cursos")
            .whereArrayContains("estudiantesInscritos", userId)
            .get()
            .addOnSuccessListener { documents ->
                val courseIds = mutableListOf<String>()
                for (doc in documents) {
                    courseIds.add(doc.id)
                    courseNamesMap[doc.id] = doc.getString("nombre") ?: "Curso sin nombre"
                }

                Log.d("MisClasesActivity", "Found ${courseIds.size} courses")

                if (courseIds.isEmpty()) {
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                fetchClasses(courseIds)
            }
            .addOnFailureListener { e ->
                Log.e("MisClasesActivity", "Error fetching courses", e)
                progressBar.visibility = View.GONE
            }
    }

    private fun fetchClasses(courseIds: List<String>) {
        val chunks = courseIds.chunked(10)
        var completedChunks = 0
        val now = Date()

        for (chunk in chunks) {
            db.collection("clases")
                .whereIn("cursoId", chunk)
                .get()
                .addOnSuccessListener { documents ->
                    Log.d("MisClasesActivity", "Found ${documents.size()} classes in chunk")

                    for (doc in documents) {
                        val clase = doc.toObject(Clase::class.java)
                        clase.id = doc.id

                        // Filter future classes on client side
                        if (clase.fecha != null && clase.fecha!!.after(now)) {
                            clasesList.add(clase)
                            Log.d("MisClasesActivity", "Added future class: ${clase.tema}")
                        }
                    }

                    completedChunks++
                    if (completedChunks == chunks.size) {
                        clasesList.sortBy { it.fecha }
                        adapter.notifyDataSetChanged()
                        progressBar.visibility = View.GONE

                        Log.d("MisClasesActivity", "Total future classes: ${clasesList.size}")

                        if (clasesList.isEmpty()) {
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MisClasesActivity", "Error fetching classes", e)
                    completedChunks++
                    if (completedChunks == chunks.size) {
                         progressBar.visibility = View.GONE
                    }
                }
        }
    }
}
