package com.jjcc.proyectmovil.core.model

data class ItemCursoAdmin(
    val id: String = "",
    val nombreCurso: String = "",      // Ej: "Ciencias Naturales 11° A (11A)"
    val nombreProfesor: String = "",   // Lo sacamos de users con docenteId
    val nombreMateria: String = "",    // asignatura
    val docenteId: String = ""         // para poder buscar el nombre del profe
)