package com.jjcc.proyectmovil.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.model.Evaluacion
import java.text.SimpleDateFormat
import java.util.Locale

class StudentEvaluationsAdapter(
    private val evaluations: List<Evaluacion>,
    private val onItemClick: (Evaluacion) -> Unit
) : RecyclerView.Adapter<StudentEvaluationsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvEvaluationTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvEvaluationDescription)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvMaxGrade: TextView = itemView.findViewById(R.id.tvMaxGrade)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_evaluation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evaluation = evaluations[position]

        holder.tvTitle.text = evaluation.titulo ?: "Sin título"
        holder.tvDescription.text = evaluation.descripcion ?: ""

        // Format date
        val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale("es", "ES"))
        holder.tvDate.text = evaluation.fecha?.let { dateFormat.format(it) } ?: "Sin fecha"

        holder.tvMaxGrade.text = "Nota Max: ${evaluation.notaMaxima?.toInt() ?: 5}"
        holder.tvType.text = evaluation.tipo ?: "Tarea"

        holder.itemView.setOnClickListener {
            onItemClick(evaluation)
        }
    }

    override fun getItemCount(): Int = evaluations.size
}
