package com.jjcc.proyectmovil.core.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.messages.ChatActivity
import com.jjcc.proyectmovil.messages.ConversationRow

class UserAdapter(
    private val context: Context,
    private val conversationList: ArrayList<ConversationRow>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.item_conversation, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentConv = conversationList[position]

        holder.textName.text = currentConv.partnerName ?: "Chat"
        holder.textMessage.text = currentConv.lastMessageText ?: ""

        // Foto de perfil
        if (!currentConv.fotoPerfil.isNullOrBlank()) {
            Glide.with(context)
                .load(currentConv.fotoPerfil)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.imgProfile)
        } else {
            holder.imgProfile.setImageResource(R.drawable.ic_person)
        }

        // Badge de mensajes no leídos
        val unread = currentConv.unreadCount ?: 0L
        if (unread > 0) {
            holder.txtUnread.visibility = View.VISIBLE
            holder.txtUnread.text = if (unread > 99) "99+" else unread.toString()
        } else {
            holder.txtUnread.visibility = View.GONE
        }

        // Ir al chat
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("convId", currentConv.convId)
                putExtra("partnerId", currentConv.partnerId)
                putExtra("partnerName", currentConv.partnerName)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = conversationList.size

    class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.img_profile)
        val textName: TextView = itemView.findViewById(R.id.txt_name)
        val textMessage: TextView = itemView.findViewById(R.id.txt_last_message)
        val txtUnread: TextView = itemView.findViewById(R.id.txt_unread_count)
    }
}