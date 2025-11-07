package com.jjcc.proyectmovil

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisCursos : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var tvNombreCurso: TextView
    private lateinit var tvAula: TextView
    private lateinit var tvPeriodo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_cursos)

        tvNombreCurso = findViewById(R.id.tvNombreCurso)
        tvAula = findViewById(R.id.tvAula)
        tvPeriodo = findViewById(R.id.tvPeriodo)

        cargarCursoDelEstudiante()
    }

    private fun cargarCursoDelEstudiante() {
        val estudianteId = auth.currentUser?.uid ?: return

        db.collection("cursos")
            .whereArrayContains("estudiantesInscritos", estudianteId)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    val curso = documentos.documents[0].toObject(ItemCurso::class.java)
                    if (curso != null) {
                        tvNombreCurso.text = "${curso.grado}° ${curso.seccion}"
                        tvAula.text = "Aula: ${curso.aula}"
                        tvPeriodo.text = "Periodo: ${curso.periodo}"
                    }
                } else {
                    tvNombreCurso.text = "No estás inscrito en ningún curso."
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

    }
}
