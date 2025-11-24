package com.jjcc.proyectmovil.core.model

data class ItemNota(
    val id: String? = null,
    val titulo: String? = null,
    val cursoId: String? = null,
    val estudianteId: String? = null,
    val nota: Double? = null,
    val estado: String? = null,     // "CALIFICADA", "PENDIENTE", etc.
    val periodo: String? = null     // "2025-1", por ejemplo
)