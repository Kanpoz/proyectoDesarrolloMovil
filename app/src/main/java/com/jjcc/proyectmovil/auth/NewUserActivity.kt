package com.jjcc.proyectmovil.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jjcc.proyectmovil.R

class NewUserActivity : AppCompatActivity(){

    private lateinit var conexion: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user)

        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(this@NewUserActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        conexion = FirebaseAuth.getInstance()

        val editNombres = findViewById<EditText>(R.id.editNombres)
        val editApellidos = findViewById<EditText>(R.id.editApellidos)
        val spinnerTipoDocumento = findViewById<Spinner>(R.id.spinnerTipoDocumento)
        val editDocumento = findViewById<EditText>(R.id.editDocumento)
        val editCelular = findViewById<EditText>(R.id.editCelular)
        val editDireccion = findViewById<EditText>(R.id.editDireccion)
        val editEmail = findViewById<EditText>(R.id.editCorreo)
        val editPassword = findViewById<EditText>(R.id.editContraseña)
        val btnRegistrar = findViewById<Button>(R.id.btnGuardar)
        val spinnerTipoRol = findViewById<Spinner>(R.id.spinnerTipoRol)

        // Lista de tipos de documentos
        val tipoDocumento = listOf("Selecciona un tipo de documento", "CC", "TI", "CE", "PASAPORTE")
        val tipoRol = listOf(
            "Selecciona un rol",
            "ESTUDIANTE",
            "RECTOR",
            "ACUDIENTE",
            "DOCENTE",
            "ADMINISTRADOR",
            "COORDINADOR"
        )

        //Adaptador para el spinner 1
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipoDocumento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoDocumento.adapter = adapter

        //Adaptador para el spinner 2
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipoRol)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoRol.adapter = adapter1

        btnRegistrar.setOnClickListener {
            val nombres = editNombres.text.toString().trim()
            val apellidos = editApellidos.text.toString().trim()
            val tipoDocumento = spinnerTipoDocumento.selectedItem.toString()
            val documento = editDocumento.text.toString().trim()
            val celular = editCelular.text.toString().trim()
            val direccion = editDireccion.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val tipoRol = spinnerTipoRol.selectedItem.toString()

            if(nombres.isEmpty() || apellidos.isEmpty() || documento.isEmpty()
                || celular.isEmpty() || direccion.isEmpty() || email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT). show()
                return@setOnClickListener
            }

            if(tipoDocumento == "Selecciona un tipo de documento"){
                Toast.makeText(this, "Seleccione un tipo de documento", Toast.LENGTH_SHORT). show()
                return@setOnClickListener
            }

            if(tipoRol == "Selecciona un rol"){
                Toast.makeText(this, "Seleccione un rol", Toast.LENGTH_SHORT). show()
                return@setOnClickListener
            }

            //Crear usuario en Firebase Authenticacion
            conexion.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val user = conexion.currentUser
                        val uid = user?.uid

                        if (user != null && uid != null){
                            user.sendEmailVerification()
                                .addOnSuccessListener {
                                    val datosUsuario = mapOf(
                                        "nombres" to nombres,
                                        "apellidos" to apellidos,
                                        "tipo_documento" to  tipoDocumento,
                                        "numero_documento" to documento,
                                        "celular" to celular,
                                        "direccion" to  direccion,
                                        "rol" to tipoRol,
                                        "email" to email,
                                        "password" to password
                                    )

                                    FirebaseDatabase.getInstance().getReference("usuarios")
                                        .child(uid)
                                        .setValue(datosUsuario)
                                        .addOnSuccessListener {
                                            Toast.makeText( this, "Registro exitoso. Verifica tu correo.", Toast.LENGTH_LONG).show()
                                            startActivity(Intent(this, LoginActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Error al guardar en RTDB: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "No se pudo enviar el correo de verificacion",
                                        Toast.LENGTH_SHORT).show()
                                }
                        }else{
                            Toast.makeText(this, "Usuario inválido", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}