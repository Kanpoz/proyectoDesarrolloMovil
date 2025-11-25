package com.jjcc.proyectmovil.messages

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.User

class NewConversationAdapter(
    private val context: Context,
    private val userList: ArrayList<User>
) : RecyclerView.Adapter<NewConversationAdapter.UserViewHolder>() {

    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.item_conversation, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.textName.text = user.getNombreCompleto()
        holder.textMessage.text = user.email ?: ""
        holder.txtUnread.visibility = View.GONE

        holder.imgProfile.setImageResource(R.drawable.ic_person)

        holder.itemView.setOnClickListener {
            val partnerId = user.uid ?: return@setOnClickListener
            val myUid = currentUid ?: return@setOnClickListener

            // Generate convId: sort UIDs to ensure consistency
            val convId = if (myUid < partnerId) {
                "${myUid}_$partnerId"
            } else {
                "${partnerId}_$myUid"
            }

            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("convId", convId)
                putExtra("partnerId", partnerId)
                putExtra("partnerName", user.getNombreCompleto())
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.img_profile)
        val textName: TextView = itemView.findViewById(R.id.txt_name)
        val textMessage: TextView = itemView.findViewById(R.id.txt_last_message)
        val txtUnread: TextView = itemView.findViewById(R.id.txt_unread_count)
    }
    fun updateList(newList: ArrayList<User>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }
}
