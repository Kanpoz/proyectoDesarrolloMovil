package com.jjcc.proyectmovil

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeAcudiente : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_acudiente)

        // Referencias a los botones del XML
        val btnCalificaciones = findViewById<LinearLayout>(R.id.btnCalificaciones)
        val btnMaterias = findViewById<LinearLayout>(R.id.btnMaterias)
        val btnObservaciones = findViewById<LinearLayout>(R.id.btnObservaciones)
        val btnTareas = findViewById<LinearLayout>(R.id.btnTareas)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        btnMaterias.setOnClickListener {
            //val intent = Intent(this, MateriasActivity::class.java)
            startActivity(intent)
        }

        btnObservaciones.setOnClickListener {
            //val intent = Intent(this, ObservacionesActivity::class.java)
            startActivity(intent)
        }

        btnTareas.setOnClickListener {
            //val intent = Intent(this, TareasActivity::class.java)
            startActivity(intent)
        }

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
                else -> false
            }
        }


}
}