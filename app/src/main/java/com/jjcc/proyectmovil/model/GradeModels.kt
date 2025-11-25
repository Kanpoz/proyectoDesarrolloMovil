package com.jjcc.proyectmovil.model

import com.google.firebase.Timestamp
import java.util.Date

data class Nota(
    val id: String = "",
    val evaluacionId: String = "",
    val estudianteId: String = "",
    val nota: Double? = null
)

data class Estudiante(
    val id: String = "",
    val nombre: String = "",
    val fotoUrl: String = ""
)
