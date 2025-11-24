package com.jjcc.proyectmovil.core.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemNota

class NotaAdapter(private val listaNotas: List<ItemNota>) :
    RecyclerView.Adapter<NotaAdapter.NotaViewHolder>() {

    class NotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAsignatura: TextView = itemView.findViewById(R.id.tvAsignatura)
        val tvNotaFinal: TextView = itemView.findViewById(R.id.tvNotaFinal)
        val tvPeriodo: TextView = itemView.findViewById(R.id.tvPeriodo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nota, parent, false)
        return NotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val nota = listaNotas[position]

        // Si no hay título, mostramos algo genérico
        holder.tvAsignatura.text = nota.titulo ?: "Evaluación"

        val valorNota = nota.nota ?: 0.0
        holder.tvNotaFinal.text = "Nota: $valorNota"

        holder.tvPeriodo.text = "Periodo: ${nota.periodo ?: "N/D"}"
    }

    override fun getItemCount(): Int = listaNotas.size
}