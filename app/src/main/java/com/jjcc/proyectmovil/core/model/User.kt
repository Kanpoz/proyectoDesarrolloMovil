package com.jjcc.proyectmovil.core.model

data class User(
    var nombres: String? = null,
    var apellidos: String? = null,
    var tipoDocumento: String? = null,
    var numeroDocumento: String? = null,
    var celular: String? = null,
    var direccion: String? = null,
    var email: String? = null,
    var password: String? = null,
    var uid: String? = null,
    var tipoRol: String? = null
) {
    // Función para obtener el nombre completo
    fun getNombreCompleto(): String {
        return "${nombres ?: ""} ${apellidos ?: ""}".trim()
    }

    // Constructor sin parámetros requerido por Firebase
    constructor() : this(null, null, null, null, null, null, null, null, null, null)
}