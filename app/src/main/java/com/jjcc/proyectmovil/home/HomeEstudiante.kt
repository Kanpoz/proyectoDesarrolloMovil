package com.jjcc.proyectmovil.home

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.jjcc.proyectmovil.messages.MainChatActivity
import com.jjcc.proyectmovil.roles.student.MisAsignaturas
import com.jjcc.proyectmovil.roles.student.MisAsistencias
import com.jjcc.proyectmovil.roles.student.MisCursos
import com.jjcc.proyectmovil.roles.student.MisNotas
import com.jjcc.proyectmovil.profile.PerfilActivity
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.roles.student.Calendario

class HomeEstudiante : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnNotas: MaterialButton
    private lateinit var btnCalendar: MaterialButton
    private lateinit var btnAsistencias: MaterialButton
    private lateinit var btnAsignaturas: MaterialButton
    private lateinit var btnCursos: MaterialButton
    private lateinit var tvRol: TextView
    private lateinit var tvBienvenida: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_estudiante)

        btnCalendar = findViewById(R.id.btnCalendar)
        btnCursos = findViewById(R.id.btnCursos)
        btnAsignaturas = findViewById(R.id.btnAsignaturas)
        btnAsistencias = findViewById(R.id.btnAsistencias)
        btnNotas = findViewById(R.id.btnNotas)
        bottomNav = findViewById(R.id.bottomNavigation)
        tvRol = findViewById(R.id.tvRolUsuario)
        tvBienvenida = findViewById(R.id.tvBienvenida)

        tvRol.text = "Rol: Estudiante"

        //Desactiva el efecto "ripple" (el círculo que se expande al tocar)
        bottomNav.itemRippleColor = null

        //Evita la animación o salto brusco al re-seleccionar
        bottomNav.setOnItemReselectedListener {}

        //Botón de calendario
        btnCalendar.setOnClickListener {
            startActivity(Intent(this, Calendario::class.java))
        }

        //Botón de cursos
        btnCursos.setOnClickListener {
            startActivity(Intent(this, MisCursos::class.java))
        }

        //Botón de asistencias
        btnAsistencias.setOnClickListener {
            startActivity(Intent(this, MisAsistencias::class.java))
        }

        //Botón de asignaturas
        btnAsignaturas.setOnClickListener {
            startActivity(Intent(this, MisAsignaturas::class.java))
        }

        btnNotas.setOnClickListener {
            startActivity(Intent(this, MisNotas::class.java))
        }

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
}