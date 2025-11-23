package com.jjcc.proyectmovil.core.model

data class ChatMessage(
    val id: String = "",
    val sender: String = "",
    val message: String = "",
    val timestamp: Long = 0
)