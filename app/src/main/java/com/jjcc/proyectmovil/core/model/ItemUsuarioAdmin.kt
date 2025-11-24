package com.jjcc.proyectmovil.core.model

data class ItemUsuarioAdmin(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "",
    val telefono: String = "",
    val activo: Boolean = true
)
