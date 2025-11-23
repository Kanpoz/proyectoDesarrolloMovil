package com.jjcc.proyectmovil.core.model

import com.google.firebase.Timestamp

data class ItemAsistencia(
    val id: String? = null,
    val claseId: String? = null,
    val cursoId: String? = null,
    val estudianteId: String? = null,
    val estado: String? = null,
    val fecha: Timestamp? = null,
    val modificaciones: List<Map<String, Any>>? = null
)