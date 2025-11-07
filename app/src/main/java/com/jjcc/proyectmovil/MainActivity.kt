package com.jjcc.proyectmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var conexion: FirebaseAuth
    private lateinit var linkRecuperar: TextView
    private lateinit var btnSoporte: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Instancia la conección a la base de datos
        conexion=FirebaseAuth.getInstance()

        //Inicializa todos los elementos de la lista
        editEmail= findViewById(R.id.editEmail)
        editPassword= findViewById(R.id.editPassword)
        btnLogin= findViewById(R.id.btnLogin)
        linkRecuperar= findViewById(R.id.txtRecuperar)
        btnSoporte= findViewById(R.id.btnSoporte)

        //Acción del botón login
        btnLogin.setOnClickListener{
            //Validación de email y contraseña
            val email=editEmail.text.toString().trim()
            val password=editPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Porfavor complete todos los campos",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Conexion con la base de datos para validar el inicio de sesion
            conexion.signInWithEmailAndPassword(email,password).addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    val user=conexion.currentUser
                    if (user?.isEmailVerified == true){

                        //Forma de hallar en que rol en tiempo real
                        val uid = user.uid
                        val rolRef = FirebaseDatabase.getInstance()
                            .getReference("usuarios")   // <-- cambia a tu nodo real si es distinto
                            .child(uid)
                            .child("rol")

                        rolRef.get()
                            .addOnSuccessListener { snap ->
                                val rol = snap.getValue(String::class.java) ?: ""
                                Toast.makeText(this, "El rol es: $rol", Toast.LENGTH_SHORT)
                                    .show()

                                val destino = if (rol.equals("DOCENTE", ignoreCase = true)) {
                                    Intent(this, HomeDocente::class.java)
                                } else if (rol.equals("ACUDIENTE", ignoreCase = true)) {
                                    Intent(this, HomeAcudiente::class.java)
                                } else if (rol.equals("ADMINISTRADOR", ignoreCase = true)) {
                                    Intent(this, HomeAdmin::class.java)
                                }else{
                                    Intent(this, HomeActivity::class.java)
                                }

                                startActivity(destino)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "No se pudo leer el rol: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }else{
                        Toast.makeText(this,"La información que ingresaste no es válida. Revisa el correo o la contraseña e intentalo nuevamente.",Toast.LENGTH_SHORT).show()
                    }

                }else{
                    Toast.makeText(this,"Error: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                }
            }
        }

        //Acción del link ¿Olvidaste la contraseña?
        linkRecuperar.setOnClickListener {
            startActivity(Intent(this, RecoverPassActivity::class.java))
            finish()
        }

        btnSoporte.setOnClickListener {
            startActivity(Intent(this, NewUserActivity::class.java))
            finish()
        }
    }
}