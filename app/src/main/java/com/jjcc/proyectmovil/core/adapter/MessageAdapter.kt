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

class MessageAdapter(val context: Context, val messageList: ArrayList<Message> ):
    RecyclerView.Adapter<CursoAdapter.ViewHolder>() {

    val ITEM_RECEIVE = 1

    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoAdapter.ViewHolder {

        if(viewType == 1){

            val view: View = LayoutInflater.from(context).inflate(R.layout.activity_receive, parent,false)
            return ReceiveViewHolder(view)

        }else{

            val view: View = LayoutInflater.from(context).inflate(R.layout.activity_sent, parent,false)
            return SentViewHolder(view)

        }

    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: CursoAdapter.ViewHolder, position: Int) {

        val currentMessage = messageList[position]

        if(holder is SentViewHolder){

            holder.sentMessage?.text = currentMessage.message

        }else if(holder is ReceiveViewHolder){

            holder.receiveMessage?.text = currentMessage.message
        }
    }


    override fun getItemViewType(position: Int): Int {

        val currentMessage = messageList[position]
        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SENT
        }else{
            return ITEM_RECEIVE
        }

    }

    class SentViewHolder(itemView: View) : CursoAdapter.ViewHolder(itemView){

        val sentMessage: TextView? = itemView.findViewById<TextView>(R.id.txt_sent_message)

    }

    class ReceiveViewHolder(itemView: View) : CursoAdapter.ViewHolder(itemView){

        val receiveMessage: TextView? = itemView.findViewById<TextView>(R.id.txt_receive_message)
    }
}