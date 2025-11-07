package com.jjcc.proyectmovil

data class ItemNota(
    val asignatura: String = "",
    val cursoId: String = "",
    val estudianteId: String = "",
    val notaExamenFinal: Double = 0.0,
    val notaFinal: Double = 0.0,
    val notaQuices: Double = 0.0,
    val notaTareas: Double = 0.0,
    val observaciones: String = "",
    val periodo: String = ""
)
