package com.jjcc.proyectmovil.roles.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
//import com.jjcc.proyectmovil.auth.NewCursoActivity // luego cambiar por NewCursoActivity cuando la tengas
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import com.jjcc.proyectmovil.auth.NewUserActivity
import com.jjcc.proyectmovil.core.adapter.CursosAdminAdapter
import com.jjcc.proyectmovil.core.model.ItemCursoAdmin

class GestionCursosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvCursos: RecyclerView
    private lateinit var btnNuevoCurso: Button
    private lateinit var btnActualizarCursos: Button
    private lateinit var adapter: CursosAdminAdapter

    private val listaCursos = mutableListOf<ItemCursoAdmin>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_cursos)

        db = FirebaseFirestore.getInstance()

        val root = findViewById<View>(R.id.rootGestionCursos)
        rvCursos = findViewById(R.id.rvCursosAdmin)
        btnNuevoCurso = findViewById(R.id.btnNuevoCurso)
        btnActualizarCursos = findViewById(R.id.btnActualizarCursos)

        // Ajustar padding inferior con los system bars (para que ACTUALIZAR no quede detrás de la barra)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }

        adapter = CursosAdminAdapter(
            cursos = listaCursos,
            onVer = { curso -> mostrarDetalleCurso(curso) },
            onEditar = { curso -> editarCurso(curso) },
            onEliminar = { curso -> eliminarCurso(curso) }
        )

        rvCursos.layoutManager = LinearLayoutManager(this)
        rvCursos.adapter = adapter

        btnNuevoCurso.setOnClickListener {
            // Modo "nuevo curso" → no enviamos cursoId
            val intent = Intent(this, EditarCursoActivity::class.java)
            startActivity(intent)
        }

        btnActualizarCursos.setOnClickListener {
            cargarCursos()
        }

        // Cargar al entrar
        cargarCursos()
    }

    override fun onResume() {
        super.onResume()
        // Si luego tienes NewCursoActivity, esto refresca al volver
        cargarCursos()
    }

    private fun cargarCursos() {
        db.collection("cursos")
            .get()
            .addOnSuccessListener { snap ->
                listaCursos.clear()

                for (doc in snap.documents) {
                    val id = doc.id

                    val nombre = doc.getString("nombre") ?: ""              // "Ciencias Naturales 11° A"
                    val grado = doc.getString("grado") ?: ""                // "11"
                    val seccion = doc.getString("seccion") ?: ""            // "A"
                    val asignatura = doc.getString("asignatura") ?: ""      // "Ciencias Naturales"
                    val docenteId = doc.getString("docenteId") ?: ""

                    // Para que se parezca a la app de escritorio:
                    // "Ciencias Naturales 11° A (11A)"
                    val sufijo = if (grado.isNotBlank() && seccion.isNotBlank()) {
                        " ($grado$seccion)"
                    } else {
                        ""
                    }
                    val nombreCursoFinal = nombre + sufijo

                    // Por ahora sin nombre de profe, lo rellenamos luego
                    val curso = ItemCursoAdmin(
                        id = id,
                        nombreCurso = nombreCursoFinal,
                        nombreProfesor = "",
                        nombreMateria = asignatura,
                        docenteId = docenteId
                    )

                    listaCursos.add(curso)

                    // Si hay docenteId, buscamos el nombre en "users"
                    if (docenteId.isNotBlank()) {
                        db.collection("users")
                            .document(docenteId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val nombreProfe = userDoc.getString("nombre") ?: ""

                                // Actualizar el curso en la lista
                                val idx = listaCursos.indexOfFirst { it.id == id }
                                if (idx != -1) {
                                    val actualizado = listaCursos[idx].copy(
                                        nombreProfesor = nombreProfe
                                    )
                                    listaCursos[idx] = actualizado
                                    adapter.actualizarLista(listaCursos.toList())
                                }
                            }
                    }
                }

                // Primera actualización (por si los profes demoran en llegar)
                adapter.actualizarLista(listaCursos.toList())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando cursos: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ---- Acciones (por ahora placeholders) ----
    private fun mostrarDetalleCurso(curso: ItemCursoAdmin) {
        val intent = Intent(this, DetalleCursoActivity::class.java)
        intent.putExtra("cursoId", curso.id)
        startActivity(intent)
    }

    private fun editarCurso(curso: ItemCursoAdmin) {
        val intent = Intent(this, EditarCursoActivity::class.java)
        intent.putExtra("cursoId", curso.id)
        startActivity(intent)
    }

    private fun eliminarCurso(curso: ItemCursoAdmin) {
        // Pregunta de confirmación
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar curso")
            .setMessage("¿Seguro que deseas eliminar el curso \"${curso.nombreCurso}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Si confirma, eliminamos en Firestore
                db.collection("cursos")
                    .document(curso.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Curso eliminado", Toast.LENGTH_SHORT).show()

                        // Quitar de la lista local y refrescar
                        listaCursos.removeAll { it.id == curso.id }
                        adapter.actualizarLista(listaCursos.toList())
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Error al eliminar: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
