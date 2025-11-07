package com.jjcc.proyectmovil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CursoAdapter(private val courseList: List<ItemCurso>) :
    RecyclerView.Adapter<CursoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCursoNombre: TextView = itemView.findViewById(R.id.tvCursoNombre)
        val tvCupo: TextView = itemView.findViewById(R.id.tvCupo)
        val btnInscribir: Button = itemView.findViewById(R.id.btnInscribir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_curso, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courseList[position]
        holder.tvCursoNombre.text = "${course.grado}° ${course.seccion}"
        holder.tvCupo.text = "${course.estudiantesInscritos?.size}/30"
    }

    override fun getItemCount(): Int = courseList.size
}
