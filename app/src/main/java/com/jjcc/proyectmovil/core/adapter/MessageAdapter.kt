package com.jjcc.proyectmovil.core.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.Message

// 1. Cambia CursoAdapter.ViewHolder por RecyclerView.ViewHolder
class MessageAdapter(val context: Context, val messageList: ArrayList<Message>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    // 2. El tipo de retorno ahora es RecyclerView.ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 1){
            // Es el layout para mensajes recibidos
            val view: View = LayoutInflater.from(context).inflate(R.layout.activity_receive, parent, false)
            return ReceiveViewHolder(view)
        } else {
            // Es el layout para mensajes enviados
            val view: View = LayoutInflater.from(context).inflate(R.layout.activity_sent, parent, false)
            return SentViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    // 3. El parámetro 'holder' ahora es de tipo RecyclerView.ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]

        // Formatear hora
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val timeString = if (currentMessage.timestamp != null) {
            timeFormat.format(java.util.Date(currentMessage.timestamp!!))
        } else {
            ""
        }

        if (holder is SentViewHolder) {
            // Si es un ViewHolder de mensaje enviado, asigna el texto y hora
            holder.sentMessage?.text = currentMessage.message
            holder.sentTime?.text = timeString
        } else if (holder is ReceiveViewHolder) {
            // Si es un ViewHolder de mensaje recibido, asigna el texto y hora
            holder.receiveMessage?.text = currentMessage.message
            holder.receiveTime?.text = timeString
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)) {
            return ITEM_SENT
        } else {
            return ITEM_RECEIVE
        }
    }

    // 4. Haz que herede de RecyclerView.ViewHolder
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView? = itemView.findViewById<TextView>(R.id.txt_sent_message)
        val sentTime: TextView? = itemView.findViewById<TextView>(R.id.txt_sent_time)
    }

    // 5. Haz que herede de RecyclerView.ViewHolder
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView? = itemView.findViewById<TextView>(R.id.txt_receive_message)
        val receiveTime: TextView? = itemView.findViewById<TextView>(R.id.txt_receive_time)
    }
}
