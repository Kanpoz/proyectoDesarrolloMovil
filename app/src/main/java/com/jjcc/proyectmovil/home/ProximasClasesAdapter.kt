package com.jjcc.proyectmovil.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemCurso

class ProximasClasesAdapter(
    private var clases: List<ItemCurso>
) : RecyclerView.Adapter<ProximasClasesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHora: TextView = view.findViewById(R.id.tvHoraClase)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreClase)
        val tvModalidad: TextView = view.findViewById(R.id.tvModalidadClase)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proxima_clase, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clase = clases[position]

        // Como no tenemos horario real, pondremos una hora placeholder o vacía
        // O si el modelo tuviera horario, lo usaríamos.
        holder.tvHora.text = "08:00" // Placeholder

        holder.tvNombre.text = "${clase.nombre} ${clase.grado}° ${clase.seccion}"
        holder.tvModalidad.text = "Presencial" // Placeholder
    }

    override fun getItemCount() = clases.size

    fun actualizarLista(nuevaLista: List<ItemCurso>) {
        clases = nuevaLista
        notifyDataSetChanged()
    }
}
