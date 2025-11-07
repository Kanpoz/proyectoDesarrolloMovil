package com.jjcc.proyectmovil

data class ItemAsignatura(
    var id: String = "",
    var nombre: String = "",
    var cursoId: String = "",
    var docenteId: String = "",
    var horario: List<Horario> = emptyList()
)