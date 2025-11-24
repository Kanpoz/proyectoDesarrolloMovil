package com.jjcc.proyectmovil.model

data class EventoCalendario(
    val titulo: String,
    val fecha: java.time.LocalDate,
    val horaInicio: String,
    val horaFin: String,
    val ubicacion: String
)
