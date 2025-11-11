package com.jjcc.proyectmovil

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisNotas : AppCompatActivity() {

    private lateinit var recyclerNotas: RecyclerView
    private val listaNotas = mutableListOf<ItemNota>()
    private lateinit var adapter: NotaAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_notas)

        recyclerNotas = findViewById(R.id.recyclerNotas)
        adapter = NotaAdapter(listaNotas)

        recyclerNotas.layoutManager = LinearLayoutManager(this)
        recyclerNotas.adapter = adapter
        cargarNotas()
    }

    private fun cargarNotas() {
        val estudianteId = auth.currentUser?.uid ?: return

        db.collection("notas")
            .whereEqualTo("estudianteId", estudianteId)
            .get()
            .addOnSuccessListener { documentos ->
                listaNotas.clear()
                for (doc in documentos) {
                    val nota = doc.toObject(ItemNota::class.java)
                    listaNotas.add(nota)
                }
                adapter.notifyItemRangeInserted(0, listaNotas.size)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar notas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}