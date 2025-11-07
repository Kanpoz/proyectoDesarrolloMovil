package com.jjcc.proyectmovil

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeAcudiente : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_acudiente)

        // Referencias a los botones del XML
        val btnCalificaciones = findViewById<LinearLayout>(R.id.btnCalificaciones)
        val btnMaterias = findViewById<LinearLayout>(R.id.btnMaterias)
        val btnObservaciones = findViewById<LinearLayout>(R.id.btnObservaciones)
        val btnTareas = findViewById<LinearLayout>(R.id.btnTareas)
        bottomNav = findViewById(R.id.bottomNavigation)

        bottomNav.selectedItemId = R.id.nav_home

        //Desactiva el efecto "ripple" (el círculo que se expande al tocar)
        bottomNav.itemRippleColor = null

        //Evita la animación o salto brusco al re-seleccionar
        bottomNav.setOnItemReselectedListener {}

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