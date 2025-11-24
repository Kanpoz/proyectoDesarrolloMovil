package com.jjcc.proyectmovil.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.messages.MainChatActivity
import com.jjcc.proyectmovil.profile.PerfilActivity
import com.jjcc.proyectmovil.roles.admin.GestionCursosActivity
import com.jjcc.proyectmovil.roles.admin.GestionUsuariosActivity

class HomeAdmin : AppCompatActivity() {

    private lateinit var tvTituloAdmin: TextView
    private lateinit var tvUsuariosTotales: TextView
    private lateinit var tvCursosActivos: TextView
    private lateinit var tvDocentes: TextView
    private lateinit var tvEstudiantes: TextView
    private lateinit var tvClasesTotales: TextView
    private lateinit var tvMensajes: TextView
    private lateinit var btnGestionUsuarios: Button
    private lateinit var btnGestionCursos: Button
    private lateinit var bottomNav: BottomNavigationView

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_admin)

        tvTituloAdmin = findViewById(R.id.tvTituloAdmin)
        tvUsuariosTotales = findViewById(R.id.tvUsuariosTotales)
        tvCursosActivos = findViewById(R.id.tvCursosActivos)
        tvDocentes = findViewById(R.id.tvDocentes)
        tvEstudiantes = findViewById(R.id.tvEstudiantes)
        tvClasesTotales = findViewById(R.id.tvClasesTotales)
        tvMensajes = findViewById(R.id.tvMensajes)
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios)
        btnGestionCursos = findViewById(R.id.btnGestionCursos)
        bottomNav = findViewById(R.id.bottomNavigation)

        cargarNombreAdmin()
        cargarMetricas()

        btnGestionUsuarios.setOnClickListener {
            startActivity(Intent(this, GestionUsuariosActivity::class.java))
        }

        btnGestionCursos.setOnClickListener {
            startActivity(Intent(this, GestionCursosActivity::class.java))
        }

        //Desactiva el efecto "ripple" (el círculo que se expande al tocar)
        bottomNav.itemRippleColor = null

        //Evita la animación o salto brusco al re-seleccionar
        bottomNav.setOnItemReselectedListener {}

        // Seleccionar "home" en el menu de navegación al entrar
        bottomNav.selectedItemId = R.id.nav_home

        // Manejo de clics en el menú
        bottomNav.setOnItemSelectedListener { item ->
            bottomNav.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(120)
                .withEndAction {
                    bottomNav.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
                .start()

            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, MainChatActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                else -> false
            }
        }

    }

    private fun cargarNombreAdmin() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: "Admin"
                tvTituloAdmin.text = "¡Bienvenido, $nombre!"
            }
    }

    private fun cargarMetricas() {
        // Usuarios totales
        db.collection("users")
            .get()
            .addOnSuccessListener { snap ->
                tvUsuariosTotales.text = snap.size().toString()
            }

        // Estudiantes
        db.collection("users")
            .whereEqualTo("rol", "ESTUDIANTE")
            .get()
            .addOnSuccessListener { snap ->
                tvEstudiantes.text = snap.size().toString()
            }

        // Docentes
        db.collection("users")
            .whereEqualTo("rol", "DOCENTE")
            .get()
            .addOnSuccessListener { snap ->
                tvDocentes.text = snap.size().toString()
            }

        // Cursos activos (asumo colección "cursos" y campo "activo" = true)
        db.collection("cursos")
            .whereEqualTo("activo", true)
            .get()
            .addOnSuccessListener { snap ->
                tvCursosActivos.text = snap.size().toString()
            }

        // Clases totales (ajusta a la colección que uses: "clases", "docentes_cursos", etc.)
        db.collection("clases")
            .get()
            .addOnSuccessListener { snap ->
                tvClasesTotales.text = snap.size().toString()
            }

        // Mensajes (colección Firestore "mensajes")
        db.collection("mensajes")
            .get()
            .addOnSuccessListener { snap ->
                tvMensajes.text = snap.size().toString()
            }
    }
}
