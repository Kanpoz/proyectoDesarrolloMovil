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

class EvaluationsAdapter(
    private val evaluations: List<Evaluacion>,
    private val onEvaluationClick: (Evaluacion) -> Unit
) : RecyclerView.Adapter<EvaluationsAdapter.EvaluationViewHolder>() {

    class EvaluationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTituloEvaluacion)
        val description: TextView = view.findViewById(R.id.tvDescripcionEvaluacion)
        val date: TextView = view.findViewById(R.id.tvFechaEvaluacion)
        val maxGrade: TextView = view.findViewById(R.id.tvNotaMaxima)
        val type: TextView = view.findViewById(R.id.tvTipoEvaluacion)
        val card: View = view.findViewById(R.id.cardEvaluacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvaluationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evaluation_card, parent, false)
        return EvaluationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EvaluationViewHolder, position: Int) {
        val evaluation = evaluations[position]
        holder.title.text = evaluation.titulo ?: "Sin título"
        holder.description.text = evaluation.descripcion ?: "Sin descripción"

        val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        holder.date.text = evaluation.fecha?.let { dateFormat.format(it) } ?: "Sin fecha"

        holder.maxGrade.text = "Nota Max: ${evaluation.notaMaxima ?: 5.0}"
        holder.type.text = evaluation.tipo ?: "Tarea"

        holder.card.setOnClickListener { onEvaluationClick(evaluation) }
    }

    override fun getItemCount() = evaluations.size
}
