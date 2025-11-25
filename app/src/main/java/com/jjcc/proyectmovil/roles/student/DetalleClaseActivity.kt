package com.jjcc.proyectmovil.roles.student

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.Clase
import java.text.SimpleDateFormat
import java.util.Locale

class DetalleClaseActivity : AppCompatActivity() {

    private lateinit var tvTema: TextView
    private lateinit var tvCurso: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvDuracion: TextView
    private lateinit var tvDocente: TextView
    private lateinit var tvObjetivos: TextView
    private lateinit var tvObservaciones: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_clase)

        initViews()

        val claseId = intent.getStringExtra("CLASE_ID")
        val cursoName = intent.getStringExtra("CURSO_NAME") ?: "Curso"

        tvCurso.text = cursoName

        if (claseId != null) {
            fetchClaseDetails(claseId)
        }
    }

    private fun initViews() {
        tvTema = findViewById(R.id.tvTema)
        tvCurso = findViewById(R.id.tvCurso)
        tvFecha = findViewById(R.id.tvFecha)
        tvDuracion = findViewById(R.id.tvDuracion)
        tvDocente = findViewById(R.id.tvDocente)
        tvObjetivos = findViewById(R.id.tvObjetivos)
        tvObservaciones = findViewById(R.id.tvObservaciones)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
    }

    private fun fetchClaseDetails(claseId: String) {
        progressBar.visibility = View.VISIBLE
        db.collection("clases").document(claseId).get()
            .addOnSuccessListener { document ->
                val clase = document.toObject(Clase::class.java)
                if (clase != null) {
                    displayData(clase)
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
            }
    }

    private fun displayData(clase: Clase) {
        tvTema.text = clase.tema

        val dateFormat = SimpleDateFormat("dd 'de' MMMM yyyy, hh:mm a", Locale.getDefault())
        tvFecha.text = clase.fecha?.let { dateFormat.format(it) } ?: "Fecha no asignada"

        tvDuracion.text = "${clase.duracionMinutos} minutos"

        val objetivos = clase.objetivos?.joinToString("\n- ", prefix = "- ") ?: "N/A"
        tvObjetivos.text = objetivos

        tvObservaciones.text = clase.observaciones ?: "Ninguna"

        // Fetch teacher name
        if (clase.docenteId.isNotEmpty()) {
            db.collection("users").document(clase.docenteId).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: "Desconocido"
                    tvDocente.text = name
                }
        } else {
            tvDocente.text = "No asignado"
        }
    }
}
