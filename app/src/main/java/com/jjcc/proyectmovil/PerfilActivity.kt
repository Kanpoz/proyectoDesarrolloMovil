package com.jjcc.proyectmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase

class PerfilActivity : AppCompatActivity() {

    private lateinit var tvRol: TextView
    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var btnLogout: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        mAuth= FirebaseAuth.getInstance()

        tvRol=findViewById(R.id.tvRol)
        tvNombre=findViewById(R.id.tvNombre)
        tvCorreo=findViewById(R.id.tvCorreo)
        tvTelefono=findViewById(R.id.tvTelefono)
        tvDireccion=findViewById(R.id.tvDireccion)
        btnLogout=findViewById(R.id.btnCerrarSesion)

        cargarDatosUsuario()

        btnLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }

        //uso de los botones de abajo
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Seleccionar "perfil" al entrar
        bottomNav.selectedItemId = R.id.nav_profile

        // Manejo de clics en el menú
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, MainChatActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    true
                }
                else -> false
            }
        }
    }

    private fun cargarDatosUsuario() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val nombre = snapshot.child("nombres").getValue(String::class.java) ?: ""
                val apellido = snapshot.child("apellidos").getValue(String::class.java) ?: ""
                val rol = snapshot.child("rol").getValue(String::class.java) ?: "Sin rol"
                val correo = snapshot.child("email").getValue(String::class.java) ?: "No registrado"
                val telefono = snapshot.child("celular").getValue(String::class.java) ?: "No registrado"
                val direccion = snapshot.child("direccion").getValue(String::class.java) ?: "No registrado"

                tvNombre.text = "$nombre $apellido"
                tvRol.text = rol
                tvCorreo.text = correo
                tvTelefono.text = telefono
                tvDireccion.text = direccion
            } else {
                Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar datos: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}