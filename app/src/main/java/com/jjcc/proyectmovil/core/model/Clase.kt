package com.jjcc.proyectmovil.core.model

import java.util.Date

data class Clase(
    var id: String = "",
    var asistenciaTomada: Boolean = false,
    var cursoId: String = "",
    var diaSemana: String = "",
    var docenteId: String = "",
    var duracionMinutos: Int = 0,
    var fecha: Date? = null,
    var fechaAsistencia: Date? = null,
    var materiales: List<String>? = null,
    var objetivos: List<String>? = null,
    var observaciones: String? = null,
    var tema: String = ""
)
