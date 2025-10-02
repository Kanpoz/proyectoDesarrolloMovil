package com.jjcc.proyectmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var conexion: FirebaseAuth
    private lateinit var txtRecuperar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        conexion=FirebaseAuth.getInstance()

        //Inicializa todos los elementos de la lista
        editEmail=findViewById(R.id.editEmail)
        editPassword=findViewById(R.id.editPassword)
        btnLogin=findViewById(R.id.btnLogin)
        txtRecuperar =findViewById(R.id.txtRecuperar)

        //Acción del botón login
        btnLogin.setOnClickListener{
            val email=editEmail.text.toString().trim()
            val password=editPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Complete los campos",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            conexion.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        val user=conexion.currentUser
                        if (user?.isEmailVerified == true){
                            startActivity(Intent(this, InicioActivity::class.java))
                            finish()
                        }else{
                            Toast.makeText(this,"Debes verificar tu email",Toast.LENGTH_SHORT).show()
                        }

                    }else{
                        Toast.makeText(this,"Error: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }
}