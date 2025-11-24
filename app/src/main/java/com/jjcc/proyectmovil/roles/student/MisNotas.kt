package com.jjcc.proyectmovil.roles.student

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.adapter.NotaAdapter
import com.jjcc.proyectmovil.core.model.ItemNota

class MisNotas : AppCompatActivity() {

    private lateinit var recyclerNotas: RecyclerView
    private val listaNotas = mutableListOf<ItemNota>()
    private lateinit var adapter: NotaAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_notas)
        enableEdgeToEdge()

        recyclerNotas = findViewById(R.id.recyclerNotas)
        recyclerNotas.layoutManager = LinearLayoutManager(this)

        adapter = NotaAdapter(listaNotas)
        recyclerNotas.adapter = adapter

        cargarNotas()
    }

    private fun cargarNotas() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val estudianteId = user.uid

        db.collection("evaluaciones")
            .whereEqualTo("estudianteId", estudianteId)
            // si quieres filtrar por periodo:
            // .whereEqualTo("periodo", "2025-1")
            .get()
            .addOnSuccessListener { documentos ->
                listaNotas.clear()

                for (doc in documentos) {
                    val nota = doc.toObject(ItemNota::class.java)
                        .copy(id = doc.id) // por si quieres usar el id del doc

                    listaNotas.add(nota)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar notas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}