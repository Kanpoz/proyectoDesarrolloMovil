package com.jjcc.proyectmovil

data class Curso(
    var id: String = "",
    var nombre: String = "",
    var grado: String = "",
    var seccion: String = "",
    var capacidad: String = "",
    var aula: String = "",
    var estudiantesInscritos: MutableList<String> = mutableListOf(),
    var horaInicio: String = "",
    var horaFin: String = ""
)
