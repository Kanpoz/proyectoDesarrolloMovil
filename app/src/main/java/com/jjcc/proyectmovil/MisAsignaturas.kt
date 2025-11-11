package com.jjcc.proyectmovil

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisAsignaturas : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listaItemCursos = mutableListOf<ItemCurso>()
    private lateinit var adapter: AsignaturaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_asignaturas)

        recyclerView = findViewById(R.id.recyclerAsignaturas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AsignaturaAdapter(listaItemCursos)
        recyclerView.adapter = adapter

        cargarAsignaturas()
    }

    private fun cargarAsignaturas() {
        val estudianteId = auth.currentUser?.uid ?: return

        db.collection("cursos")
            .whereArrayContains("estudiantesInscritos", estudianteId)
            .get()
            .addOnSuccessListener { documentos ->
                listaItemCursos.clear()
                for (doc in documentos) {
                    val itemCurso = doc.toObject(ItemCurso::class.java)
                    listaItemCursos.add(itemCurso)
                }
                adapter.notifyItemRangeInserted(0, listaItemCursos.size)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
