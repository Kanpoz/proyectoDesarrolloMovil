package com.jjcc.proyectmovil

data class ItemAsistencia(
    val id: String? = null,
    val claseId: String? = null,
    val cursoId: String? = null,
    val estudianteId: String? = null,
    val estado: String? = null,
    val fecha: com.google.firebase.Timestamp? = null,
    val modificaciones: List<Map<String, Any>>? = null
)