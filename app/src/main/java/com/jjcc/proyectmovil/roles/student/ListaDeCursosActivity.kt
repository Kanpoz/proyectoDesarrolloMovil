package com.jjcc.proyectmovil.roles.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.adapter.CursoAdapter
import com.jjcc.proyectmovil.core.model.ItemCurso

class ListaDeCursosActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val courseList = mutableListOf<ItemCurso>()
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
            courseList.addAll(it.toObjects(ItemCurso::class.java))
            adapter.notifyItemRangeInserted(0, courseList.size)
        }
    }
}