package com.jjcc.proyectmovil.model

import com.google.firebase.Timestamp
import java.util.Date

data class Evaluacion(
    val id: String? = null,
    val cursoId: String? = null,
    val docenteId: String? = null,
    val estudianteId: String? = null,
    val titulo: String? = null,
    val descripcion: String? = null,
    val notaMaxima: Double? = null,
    val nota: Double? = null,
    val fecha: Date? = null,
    val tipo: String? = null,  // "Tarea", "Examen", "Quiz", etc.
    val timestamp: Timestamp? = null
)
