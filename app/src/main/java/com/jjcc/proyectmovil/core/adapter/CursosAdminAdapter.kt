package com.jjcc.proyectmovil.core.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemCursoAdmin

class CursosAdminAdapter(
    private var cursos: List<ItemCursoAdmin>,
    private val onVer: (ItemCursoAdmin) -> Unit,
    private val onEditar: (ItemCursoAdmin) -> Unit,
    private val onEliminar: (ItemCursoAdmin) -> Unit
) : RecyclerView.Adapter<CursosAdminAdapter.CursoViewHolder>() {

    inner class CursoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCurso: TextView = itemView.findViewById(R.id.tvCursoNombre)
        val tvProfesor: TextView = itemView.findViewById(R.id.tvCursoProfesor)
        val tvMateria: TextView = itemView.findViewById(R.id.tvCursoMateria)
        val btnVer: ImageView = itemView.findViewById(R.id.btnVerCurso)
        val btnEditar: ImageView = itemView.findViewById(R.id.btnEditarCurso)
        val btnEliminar: ImageView = itemView.findViewById(R.id.btnEliminarCurso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_curso_admin, parent, false)
        return CursoViewHolder(view)
    }

    override fun getItemCount(): Int = cursos.size

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        val curso = cursos[position]
        holder.tvCurso.text = curso.nombreCurso
        holder.tvProfesor.text = curso.nombreProfesor
        holder.tvMateria.text = curso.nombreMateria

        holder.btnVer.setOnClickListener { onVer(curso) }
        holder.btnEditar.setOnClickListener { onEditar(curso) }
        holder.btnEliminar.setOnClickListener { onEliminar(curso) }
    }

    fun actualizarLista(nuevaLista: List<ItemCursoAdmin>) {
        cursos = nuevaLista
        notifyDataSetChanged()
    }
}