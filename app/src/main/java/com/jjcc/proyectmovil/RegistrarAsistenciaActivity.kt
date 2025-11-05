package com.jjcc.proyectmovil

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class RegistrarAsistenciaActivity : AppCompatActivity() {

    private lateinit var spinnerSemana: Spinner
    private lateinit var editPorcentaje: EditText
    private lateinit var btnGuardar: Button

    private val dbRef = FirebaseDatabase.getInstance().getReference("Asistencias")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_asistencia)

        spinnerSemana = findViewById(R.id.spinnerSemana)
        editPorcentaje = findViewById(R.id.editPorcentaje)
        btnGuardar = findViewById(R.id.btnGuardarAsistencia)

        // 🔹 Llenamos el spinner con semanas (podrías cambiarlo por fechas dinámicas)
        val semanas = listOf("Semana 1", "Semana 2", "Semana 3", "Semana 4", "Semana 5")
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, semanas)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSemana.adapter = adaptador

        btnGuardar.setOnClickListener {
            guardarAsistencia()
        }
    }

    private fun guardarAsistencia() {
        val semanaSeleccionada = spinnerSemana.selectedItem.toString()
        val porcentajeTexto = editPorcentaje.text.toString()

        if (porcentajeTexto.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el porcentaje", Toast.LENGTH_SHORT).show()
            return
        }

        val porcentaje = porcentajeTexto.toFloatOrNull()
        if (porcentaje == null || porcentaje < 0 || porcentaje > 100) {
            Toast.makeText(this, "Ingresa un valor válido (0–100)", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔥 Guardamos en Firebase
        val datos = mapOf("porcentaje" to porcentaje)
        dbRef.child(semanaSeleccionada).setValue(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Asistencia guardada correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra la activity y vuelve al dashboard
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}
