package com.jjcc.proyectmovil.core.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.core.model.ItemCurso
import com.jjcc.proyectmovil.R

class AsignaturaAdapter(private val listaItemCursos: List<ItemCurso>) :
    RecyclerView.Adapter<AsignaturaAdapter.AsignaturaViewHolder>() {

    class AsignaturaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreAsignatura)
        val aula: TextView = view.findViewById(R.id.tvAula)
        val grado: TextView = view.findViewById(R.id.tvGrado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsignaturaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asignatura, parent, false)
        return AsignaturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AsignaturaViewHolder, position: Int) {
        val curso = listaItemCursos[position]
        holder.nombre.text = curso.nombre
        holder.aula.text = "Aula: ${curso.aula}"
        holder.grado.text = "Grado ${curso.grado}° - Sección ${curso.seccion}"
    }

    override fun getItemCount(): Int = listaItemCursos.size
}
