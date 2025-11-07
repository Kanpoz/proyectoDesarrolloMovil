package com.jjcc.proyectmovil.ui.courses

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.Curso
import com.jjcc.proyectmovil.CursoAdapter

class ListaDeCursosActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val courseList = mutableListOf<Curso>()
    private lateinit var adapter: CursoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_de_cursos)

        recyclerView = findViewById(R.id.rvCursos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CursoAdapter(courseList)
        recyclerView.adapter = adapter

        db.collection("cursos").get().addOnSuccessListener {
            courseList.clear()
            courseList.addAll(it.toObjects(Curso::class.java))
            adapter.notifyDataSetChanged()
        }
    }
}
