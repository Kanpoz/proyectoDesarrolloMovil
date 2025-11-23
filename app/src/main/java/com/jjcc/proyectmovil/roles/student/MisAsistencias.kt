package com.jjcc.proyectmovil.roles.student

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.adapter.AsistenciaAdapter
import com.jjcc.proyectmovil.core.model.ItemAsistencia

class MisAsistencias : AppCompatActivity() {

    private lateinit var recyclerAsistencias: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val asistenciasList = mutableListOf<ItemAsistencia>()
    private lateinit var adapter: AsistenciaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_asistencias)

        recyclerAsistencias = findViewById(R.id.recyclerAsistencias)
        recyclerAsistencias.layoutManager = LinearLayoutManager(this)
        adapter = AsistenciaAdapter(asistenciasList)
        recyclerAsistencias.adapter = adapter

        cargarAsistencias()
    }

    private fun cargarAsistencias() {
        val estudianteId = auth.currentUser?.uid

        db.collection("asistencia")
            .whereEqualTo("estudianteId", estudianteId)
            .get()
            .addOnSuccessListener { result ->
                asistenciasList.clear()
                for (doc in result) {
                    val asistencia = doc.toObject(ItemAsistencia::class.java)
                    asistenciasList.add(asistencia)
                }
                adapter.notifyItemRangeInserted(0, asistenciasList.size)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar asistencias", Toast.LENGTH_SHORT).show()
            }
    }
}