package com.jjcc.proyectmovil.core.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemUsuarioAdmin

class UserAdminAdapter(
    private var listaUsuarios: List<ItemUsuarioAdmin>,
    private val onClick: (ItemUsuarioAdmin) -> Unit
) : RecyclerView.Adapter<UserAdminAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFoto: ImageView = itemView.findViewById(R.id.imgFotoUsuario)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreUsuario)
        val tvCorreo: TextView = itemView.findViewById(R.id.tvCorreoUsuario)
        val tvRol: TextView = itemView.findViewById(R.id.tvRolUsuario)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefonoUsuario)
        val tvActivo: TextView = itemView.findViewById(R.id.tvActivoUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_admin, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val user = listaUsuarios[position]

        holder.tvNombre.text = user.nombre
        holder.tvCorreo.text = user.email
        holder.tvRol.text = user.rol
        holder.tvTelefono.text = user.telefono

        // Configurar badge de estado
        if (user.activo) {
            holder.tvActivo.text = "Activo"
            holder.tvActivo.setBackgroundResource(R.drawable.bg_badge_active)
        } else {
            holder.tvActivo.text = "Inactivo"
            holder.tvActivo.setBackgroundResource(R.drawable.bg_badge_inactive)
        }

        // Click en toda la card
        holder.itemView.setOnClickListener {
            onClick(user)
        }
    }

    override fun getItemCount(): Int = listaUsuarios.size

    fun actualizarLista(nuevaLista: List<ItemUsuarioAdmin>) {
        listaUsuarios = nuevaLista
        notifyDataSetChanged()
    }
}
