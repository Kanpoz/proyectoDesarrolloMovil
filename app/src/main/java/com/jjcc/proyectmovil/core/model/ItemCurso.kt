package com.jjcc.proyectmovil.core.model

data class ItemCurso(
    var id: String? = "",
    var nombre: String? = "",
    var grado: String? = "",
    var seccion: String? = "",
    var aula: String? = "",
    val periodo: String? = null,
    var estudiantesInscritos: MutableList<String>? = mutableListOf()
)