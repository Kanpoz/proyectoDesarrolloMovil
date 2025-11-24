package com.jjcc.proyectmovil.messages

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.jjcc.proyectmovil.R
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jjcc.proyectmovil.core.adapter.MessageAdapter
import com.jjcc.proyectmovil.core.model.Message

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private var convId: String? = null
    private var partnerId: String? = null
    private var partnerName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendButton)

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        convId = intent.getStringExtra("convId")
        partnerId = intent.getStringExtra("partnerId")
        partnerName = intent.getStringExtra("partnerName")

        if (convId == null || partnerId == null) {
            Toast.makeText(this, "Conversación no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportActionBar?.title = partnerName ?: "Chat"

        // 🔹 Escuchar mensajes en tiempo real desde chat_stream
        listenMessages()

        sendButton.setOnClickListener {
            val text = messageBox.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                messageBox.setText("")
            }
        }
    }

    private fun listenMessages() {
        val messagesRef = mDbRef.child("chat_stream")
            .child(convId!!)
            .child("messages")

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = ArrayList<Message>()

                for (child in snapshot.children) {
                    val messageText = child.child("texto").getValue(String::class.java)
                    val senderId = child.child("remitenteId").getValue(String::class.java)
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L

                    val msg = Message(messageText, senderId, timestamp)
                    tempList.add(msg)
                }

                // Ordenar por timestamp ascendente
                tempList.sortBy { it.timestamp ?: 0L }

                messageList.clear()
                messageList.addAll(tempList)
                messageAdapter.notifyDataSetChanged()

                if (messageList.isNotEmpty()) {
                    chatRecyclerView.scrollToPosition(messageList.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Puedes loguear el error si quieres
            }
        })
    }

    private fun sendMessage(text: String) {
        val senderId = mAuth.currentUser?.uid ?: return
        val receiverId = partnerId ?: return
        val now = System.currentTimeMillis()

        // ID para este mensaje en RTDB
        val messageId = mDbRef.child("chat_stream")
            .child(convId!!)
            .child("messages")
            .push()
            .key ?: return

        // 1️⃣ Guardar en chat_stream
        val streamMap = mapOf(
            "texto" to text,
            "remitenteId" to senderId,
            "timestamp" to now
        )

        mDbRef.child("chat_stream")
            .child(convId!!)
            .child("messages")
            .child(messageId)
            .setValue(streamMap)

        // 2️⃣ Actualizar índice para ambos usuarios (chat_index)
        val indexUpdates = mapOf(
            "lastMessageText" to text,
            "lastMessageDate" to now,
            "lastMessageSenderId" to senderId
        )

        val indexRef = mDbRef.child("chat_index")

        // Para el remitente: nombreChat = nombre del otro, 0 no leídos
        indexRef.child(senderId)
            .child(convId!!)
            .updateChildren(
                indexUpdates + mapOf(
                    "nombreChat" to (partnerName ?: "Chat"),
                    "unreadCount" to 0L
                )
            )

        // Para el destinatario: por ahora solo actualizamos texto y fecha.
        // (Si luego quieres usar unreadCount, aquí puedes incrementarlo)
        indexRef.child(receiverId)
            .child(convId!!)
            .updateChildren(
                indexUpdates + mapOf(
                    "unreadCount" to 0L
                )
            )
    }
}