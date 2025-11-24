package com.jjcc.proyectmovil.messages

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.jjcc.proyectmovil.R
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private lateinit var firestore: FirebaseFirestore

    private var convId: String? = null
    private var partnerId: String? = null
    private var partnerName: String? = null

    // guardamos la marca de tiempo del último mensaje del historial de Firestore
    private var lastHistoryTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

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

        // 1️⃣ Primero cargamos historial desde Firestore
        loadHistoryFromFirestore()

        sendButton.setOnClickListener {
            val text = messageBox.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                messageBox.setText("")
            }
        }
    }

    /**
     * Carga el historial desde Firestore (colección "mensajes"),
     * filtrando por notificacionOriginalId == convId y ordenado por fechaEnvio asc.
     */
    private fun loadHistoryFromFirestore() {
        firestore.collection("mensajes")
            .whereEqualTo("notificacionOriginalId", convId)
            .orderBy("fechaEnvio", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val history = ArrayList<Message>()
                var maxTs = 0L

                for (doc in snap.documents) {
                    val text = doc.getString("texto") ?: ""
                    val senderId = doc.getString("remitenteId")
                    val ts: Long = doc.getTimestamp("fechaEnvio")
                        ?.toDate()
                        ?.time ?: 0L

                    val msg = Message(text, senderId, ts)
                    history.add(msg)

                    if (ts > maxTs) maxTs = ts
                }

                messageList.clear()
                messageList.addAll(history)
                messageAdapter.notifyDataSetChanged()

                if (messageList.isNotEmpty()) {
                    chatRecyclerView.scrollToPosition(messageList.size - 1)
                }

                // guardamos el último timestamp del historial
                lastHistoryTimestamp = maxTs

                // 2️⃣ Después del historial, empezamos a escuchar los mensajes nuevos en tiempo real
                listenRealtimeMessages()
            }
            .addOnFailureListener {
                // Si falla Firestore, igual activamos el tiempo real para no dejar el chat muerto
                Toast.makeText(this, "Error cargando historial: ${it.message}", Toast.LENGTH_SHORT).show()
                listenRealtimeMessages()
            }
    }

    /**
     * Escucha en tiempo real los mensajes de chat_stream/{convId}/messages,
     * pero SOLO agrega los que tengan timestamp > lastHistoryTimestamp
     * para no duplicar lo que ya vino del historial.
     */
    private fun listenRealtimeMessages() {
        val messagesRef = mDbRef.child("chat_stream")
            .child(convId!!)
            .child("messages")

        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageText = snapshot.child("texto").getValue(String::class.java)
                val senderId = snapshot.child("remitenteId").getValue(String::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                // ignoramos mensajes que ya estaban en el historial
                if (timestamp <= lastHistoryTimestamp) return

                val msg = Message(messageText, senderId, timestamp)
                messageList.add(msg)
                messageAdapter.notifyItemInserted(messageList.size - 1)
                chatRecyclerView.scrollToPosition(messageList.size - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Envía un mensaje:
     *  - lo guarda en chat_stream (RTDB)
     *  - actualiza chat_index
     *  - lo guarda en Firestore en la colección "mensajes" con el esquema del escritorio
     */
    private fun sendMessage(text: String) {
        val senderId = mAuth.currentUser?.uid ?: return
        val receiverId = partnerId ?: return
        val now = System.currentTimeMillis()

        // 1️⃣ Guardar en Realtime Database (chat_stream)
        val messageId = mDbRef.child("chat_stream")
            .child(convId!!)
            .child("messages")
            .push()
            .key ?: return

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

        // 2️⃣ Actualizar chat_index
        val indexUpdates = mapOf(
            "lastMessageText" to text,
            "lastMessageDate" to now,
            "lastMessageSenderId" to senderId
        )

        val indexRef = mDbRef.child("chat_index")

        indexRef.child(senderId)
            .child(convId!!)
            .updateChildren(
                indexUpdates + mapOf(
                    "nombreChat" to (partnerName ?: "Chat"),
                    "unreadCount" to 0L
                )
            )

        indexRef.child(receiverId)
            .child(convId!!)
            .updateChildren(
                indexUpdates + mapOf(
                    "unreadCount" to 0L
                )
            )

        // 3️⃣ Guardar en Firestore (colección mensajes) con el esquema del escritorio
        val messageDocRef = firestore.collection("mensajes").document()

        val messageData = hashMapOf(
            "id" to messageDocRef.id,
            "asunto" to "Chat",
            "remitenteId" to senderId,
            "destinatarioId" to receiverId,
            "texto" to text,
            "notificacionOriginalId" to convId,
            "leido" to false,
            "ocultoPara" to emptyList<String>(),
            "fechaEnvio" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "fechaLectura" to null
        )

        messageDocRef.set(messageData)
            .addOnSuccessListener {
                // opcional: podrías actualizar lastHistoryTimestamp = now si quieres
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error al guardar mensaje en Firestore: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}