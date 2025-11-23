package com.jjcc.proyectmovil.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.jjcc.proyectmovil.R

class RecoverPassActivity : AppCompatActivity() {

    private lateinit var txtRecuperar: EditText
    private lateinit var conexion: FirebaseAuth

    private lateinit var btnBack: Button
    private lateinit var btnContinue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recover_pass)

        //Instancia la conección a la base de datos
        conexion= FirebaseAuth.getInstance()

        btnBack= findViewById(R.id.btnBack)
        txtRecuperar= findViewById(R.id.txtRecuperar)
        btnContinue= findViewById(R.id.btnContinue)

        val mensajeAprovado = findViewById<LinearLayout>(R.id.layoutApprovedMessage)
        val mensajeDenegado = findViewById<LinearLayout>(R.id.layoutDeniedMessage)

        btnContinue.setOnClickListener {
            mensajeAprovado.visibility = View.GONE
            mensajeDenegado.visibility = View.GONE
            val email=txtRecuperar.text.toString().trim()
            if (!email.isEmpty()){
                conexion.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        mensajeAprovado.visibility = View.VISIBLE
                    }
                    .addOnFailureListener{
                        mensajeDenegado.visibility = View.VISIBLE
                    }
            } else  {
                Toast.makeText(this,"Ingresa un correo válido.", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}