package com.jjcc.proyectmovil.models

data class Nota(
    var id: String = "",
    var cursoId: String = "",
    var asignaturaId: String = "",
    var estudianteId: String = "",
    var nota: Double = 0.0,
    var tipo: String = "",
    var fecha: Long = System.currentTimeMillis()
)
