package com.jjcc.proyectmovil.ui.courses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.Curso
import com.jjcc.proyectmovil.R

class CursoAdapter(private val courseList: List<Curso>) :
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
        holder.tvCursoNombre.text = "${curso.grado}° ${curso.seccion}"
        holder.tvCupo.text = "${curso.estudiantesInscritos.size}/30"
    }

    override fun getItemCount(): Int = courseList.size
}
