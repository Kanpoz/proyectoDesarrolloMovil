package com.jjcc.proyectmovil.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.Clase
import java.text.SimpleDateFormat
import java.util.Locale

class ClasesAdapter(
    private val clases: List<Clase>,
    private val courseNames: Map<String, String>,
    private val onItemClick: (Clase) -> Unit
) : RecyclerView.Adapter<ClasesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvTema: TextView = view.findViewById(R.id.tvTema)
        val tvCurso: TextView = view.findViewById(R.id.tvCurso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clase, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clase = clases[position]

        val dateFormat = SimpleDateFormat("dd 'de' MMMM, hh:mm a", Locale.getDefault())
        holder.tvFecha.text = clase.fecha?.let { dateFormat.format(it) } ?: "Fecha pendiente"

        holder.tvTema.text = clase.tema
        holder.tvCurso.text = courseNames[clase.cursoId] ?: "Curso desconocido"

        holder.itemView.setOnClickListener {
            onItemClick(clase)
        }
    }

    override fun getItemCount() = clases.size
}
