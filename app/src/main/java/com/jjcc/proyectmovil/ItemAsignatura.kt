package com.jjcc.proyectmovil

import com.jjcc.proyectmovil.models.Horario

data class ItemAsignatura(
    var id: String = "",
    var nombre: String = "",
    var cursoId: String = "",
    var docenteId: String = "",
    var horario: List<Horario> = emptyList()
)