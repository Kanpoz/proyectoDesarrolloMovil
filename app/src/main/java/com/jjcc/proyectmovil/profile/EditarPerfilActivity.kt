package com.jjcc.proyectmovil.profile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.home.HomeEstudiante

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etDireccion: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNombre = findViewById(R.id.etNombre)
        // si no tienes etCorreo en el XML, comenta la siguiente línea:
        // etCorreo = findViewById(R.id.etCorreo)
        etTelefono = findViewById(R.id.etTelefono)
        etDireccion = findViewById(R.id.etDireccion)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)

        // Cargar datos desde PerfilActivity
        cargarDatosDesdeIntent()

        // Configurar validaciones en tiempo real
        configurarValidacionesTiempoReal()

        // Al inicio, validamos una vez por si vienen datos ya válidos
        validarCampos()

        btnGuardar.setOnClickListener {
            // Solo intentamos guardar si el botón está habilitado
            if (btnGuardar.isEnabled) {
                guardarCambios()
            }
        }

        btnCancelar.setOnClickListener {
            finish() // volver a PerfilActivity
        }
    }

    private fun cargarDatosDesdeIntent() {
        val nombre = intent.getStringExtra("nombre") ?: ""
        val telefono = intent.getStringExtra("telefono") ?: ""
        val direccion = intent.getStringExtra("direccion") ?: ""

        etNombre.setText(nombre)
        etTelefono.setText(telefono)
        etDireccion.setText(direccion)
    }

    private fun configurarValidacionesTiempoReal() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validarCampos()
            }
        }

        etNombre.addTextChangedListener(watcher)
        etTelefono.addTextChangedListener(watcher)
        // Dirección no es obligatoria, así que no hace falta
    }

    private fun validarCampos() {
        val nuevoNombre = etNombre.text.toString().trim()
        val nuevoTelefono = etTelefono.text.toString().trim()

        var todoValido = true

        // Nombre obligatorio
        if (nuevoNombre.isEmpty()) {
            etNombre.error = "El nombre es obligatorio"
            todoValido = false
        } else {
            etNombre.error = null
        }

        // Teléfono obligatorio, solo números, exactamente 10 dígitos
        if (nuevoTelefono.isEmpty()) {
            etTelefono.error = "El teléfono es obligatorio"
            todoValido = false
        } else {
            val telefonoRegex = Regex("^\\d{10}$")
            if (!telefonoRegex.matches(nuevoTelefono)) {
                etTelefono.error = "Debe tener exactamente 10 números (no incluyas el +57)"
                todoValido = false
            } else {
                etTelefono.error = null
            }
        }

        // Habilitamos / deshabilitamos el botón según el resultado
        btnGuardar.isEnabled = todoValido
        btnGuardar.alpha = if (todoValido) 1f else 0.5f   // 👈 aquí se ve más opaco cuando está deshabilitado
    }


    private fun guardarCambios() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid

        val nuevoNombre = etNombre.text.toString().trim()
        val nuevoTelefono = etTelefono.text.toString().trim()
        val nuevaDireccion = etDireccion.text.toString().trim()

        // Por seguridad, validamos otra vez:
        validarCampos()
        if (!btnGuardar.isEnabled) {
            return
        }

        val updates = mapOf(
            "nombre" to nuevoNombre,
            "telefono" to nuevoTelefono,
            "direccion" to nuevaDireccion
        )

        btnGuardar.isEnabled = false

        db.collection("users")
            .document(userId)
            .update(updates)
            .addOnSuccessListener {
                btnGuardar.isEnabled = true
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PerfilActivity::class.java))
            }
            .addOnFailureListener {
                btnGuardar.isEnabled = true
                Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
