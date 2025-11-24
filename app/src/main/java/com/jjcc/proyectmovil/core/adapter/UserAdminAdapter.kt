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
    private val onVer: (ItemUsuarioAdmin) -> Unit,
    private val onEditar: (ItemUsuarioAdmin) -> Unit,
    private val onEliminar: (ItemUsuarioAdmin) -> Unit
) : RecyclerView.Adapter<UserAdminAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreUsuario)
        val tvCorreo: TextView = itemView.findViewById(R.id.tvCorreoUsuario)
        val tvRol: TextView = itemView.findViewById(R.id.tvRolUsuario)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefonoUsuario)
        val tvActivo: TextView = itemView.findViewById(R.id.tvActivoUsuario)
        val btnVer: ImageView = itemView.findViewById(R.id.btnVerUsuario)
        val btnEditar: ImageView = itemView.findViewById(R.id.btnEditarUsuario)
        val btnEliminar: ImageView = itemView.findViewById(R.id.btnEliminarUsuario)
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
        holder.tvActivo.text = if (user.activo) "Sí" else "No"

        holder.btnVer.setOnClickListener { onVer(user) }
        holder.btnEditar.setOnClickListener { onEditar(user) }
        holder.btnEliminar.setOnClickListener { onEliminar(user) }
    }

    override fun getItemCount(): Int = listaUsuarios.size

    fun actualizarLista(nuevaLista: List<ItemUsuarioAdmin>) {
        listaUsuarios = nuevaLista
        notifyDataSetChanged()
    }
}
