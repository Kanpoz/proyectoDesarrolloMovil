package com.jjcc.proyectmovil.messages

data class ConversationRow(
    var convId: String = "",
    var partnerId: String? = null,
    var partnerName: String? = null,
    var lastMessageText: String? = null,
    var lastMessageDate: Long? = null,
    var unreadCount: Long? = null,
    var fotoPerfil: String? = null
)