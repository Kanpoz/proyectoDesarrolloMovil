package com.jjcc.proyectmovil

data class ChatMessage(
    val id: String = "",
    val sender: String = "",
    val message: String = "",
    val timestamp: Long = 0
)
