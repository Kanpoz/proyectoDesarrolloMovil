package com.jjcc.proyectmovil

data class Curso(
    var id: String = "",
    var grado: Int = 1,
    var seccion: String = "A",
    var capacidad: Int = 30,
    var estudiantesInscritos: MutableList<String> = mutableListOf()
)
