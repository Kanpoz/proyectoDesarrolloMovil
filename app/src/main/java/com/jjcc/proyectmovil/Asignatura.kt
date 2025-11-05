package com.jjcc.proyectmovil.models

data class Asignatura(
    var id: String = "",
    var nombre: String = "",
    var cursoId: String = "",
    var docenteId: String = "",
    var horario: List<Horario> = emptyList()
)
