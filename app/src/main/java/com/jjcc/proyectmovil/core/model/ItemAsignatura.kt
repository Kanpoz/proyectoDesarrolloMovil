package com.jjcc.proyectmovil.core.model

import com.jjcc.proyectmovil.roles.student.Horario

data class ItemAsignatura(
    var id: String = "",
    var nombre: String = "",
    var cursoId: String = "",
    var docenteId: String = "",
    var horario: List<Horario> = emptyList()
)