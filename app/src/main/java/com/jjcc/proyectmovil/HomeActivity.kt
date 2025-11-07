package com.jjcc.proyectmovil

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnCalendar = findViewById<MaterialButton>(R.id.btnCalendar)
        val btnCursos = findViewById<MaterialButton>(R.id.btnCursos)
        val btnAsignaturas = findViewById<MaterialButton>(R.id.btnAsignaturas)
        val btnAsistencias = findViewById<MaterialButton>(R.id.btnAsistencias)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        //Botón de calendario
        btnCalendar.setOnClickListener {
            startActivity(Intent(this, Calendario::class.java))
        }

        //Botón de cursos
        btnCursos.setOnClickListener {
            startActivity(Intent(this, Curso::class.java))
        }

        //Botón de asistencias
        btnAsistencias.setOnClickListener {
            startActivity(Intent(this, AsistenciasActivity::class.java))
        }

        //Botón de asignaturas
        btnAsignaturas.setOnClickListener {
            startActivity(Intent(this, Asignatura::class.java))
        }

        // Seleccionar "home" al entrar
        bottomNav.selectedItemId = R.id.nav_home

        // Manejo de clics en el menú
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, MainChatActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, Menu::class.java))
                    true
                }
                else -> false
            }
        }
    }
}