package com.jjcc.proyectmovil.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.home.HomeAdmin
import com.jjcc.proyectmovil.home.HomeDocente
import com.jjcc.proyectmovil.home.HomeEstudiante

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var conexion: FirebaseAuth
    private lateinit var linkRecuperar: TextView
    private lateinit var btnSoporte: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        conexion = FirebaseAuth.getInstance()

        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        btnLogin = findViewById(R.id.btnLogin)
        linkRecuperar = findViewById(R.id.txtRecuperar)
        btnSoporte = findViewById(R.id.btnSoporte)

        // BOTÓN LOGIN
        btnLogin.setOnClickListener {

            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // AUTENTICACIÓN
            conexion.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val uid = conexion.currentUser!!.uid

                        // 🔥 LEER ROL DESDE FIRESTORE
                        db.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { doc ->

                                if (!doc.exists()) {
                                    Toast.makeText(this, "No se encontró información del usuario", Toast.LENGTH_SHORT).show()
                                    return@addOnSuccessListener
                                }

                                val rol = doc.getString("rol")?.uppercase() ?: ""

                                val destino = when (rol) {
                                    "ADMIN" -> Intent(this, HomeAdmin::class.java)
                                    "DOCENTE" -> Intent(this, HomeDocente::class.java)
                                    "ESTUDIANTE" -> Intent(this, HomeEstudiante::class.java)
                                    else -> {
                                        Toast.makeText(this, "Rol no válido en Firestore", Toast.LENGTH_SHORT).show()
                                        return@addOnSuccessListener
                                    }
                                }

                                startActivity(destino)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error leyendo Firestore: ${it.message}", Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        Toast.makeText(
                            this,
                            "Correo o contraseña incorrectos. Inténtalo nuevamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        // BOTÓN RECUPERAR
        linkRecuperar.setOnClickListener {
            startActivity(Intent(this, RecoverPassActivity::class.java))
        }

        // BOTÓN SOPORTE / REGISTRO
        btnSoporte.setOnClickListener {
            // TODO: terminar seccion de soporte
            Toast.makeText(this, "Sección en construcción", Toast.LENGTH_SHORT).show()
        }
    }
}