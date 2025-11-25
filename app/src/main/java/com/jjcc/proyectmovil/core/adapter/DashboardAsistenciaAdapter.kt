package com.jjcc.proyectmovil.core.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemAsistencia
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardAsistenciaAdapter(private val listaAsistencias: List<ItemAsistencia>) :
    RecyclerView.Adapter<DashboardAsistenciaAdapter.DashboardViewHolder>() {

    class DashboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClassDate: TextView = view.findViewById(R.id.tvClassDate)
        val tvRegistrationDate: TextView = view.findViewById(R.id.tvRegistrationDate)
        val tvStatusCircle: TextView = view.findViewById(R.id.tvStatusCircle)
        val layoutJustification: LinearLayout = view.findViewById(R.id.layoutJustification)
        val tvJustification: TextView = view.findViewById(R.id.tvJustification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_asistencia, parent, false)
        return DashboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val asistencia = listaAsistencias[position]
        val context = holder.itemView.context

        // 1. Format Class Date
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMM, yyyy", Locale("es", "ES"))
        val classDate = asistencia.fecha?.toDate()
        holder.tvClassDate.text = classDate?.let {
            dateFormat.format(it).replaceFirstChar { char -> char.uppercase() }
        } ?: "Fecha desconocida"

        // 2. Format Registration Date (Using class date as fallback if no other date is available)
        val regDateFormat = SimpleDateFormat("d 'de' MMM, yyyy", Locale("es", "ES"))
        val regDateStr = classDate?.let { regDateFormat.format(it) } ?: "-"
        holder.tvRegistrationDate.text = "Registro: $regDateStr"

        // 3. Status Logic
        val estado = asistencia.estado?.uppercase() ?: "?"
        var statusLetter = "?"
        var statusText = "Desconocido"
        var colorRes = R.color.gray_light
        var textColorRes = R.color.text_secondary

        // Verificar si hay justificación en las modificaciones
        val tieneJustificacion = asistencia.modificaciones?.any { mod ->
            mod["tipo"] == "justificacion" ||
            mod["justificado"] == true ||
            mod["observacion"] != null ||
            mod["motivo"] != null
        } ?: false

        when (estado) {
            "P" -> {
                statusLetter = "P"
                statusText = "Presente"
                colorRes = R.color.success_light
                textColorRes = R.color.success
            }
            "T" -> {
                statusLetter = "T"
                statusText = "Tardanza"
                colorRes = R.color.warning
                textColorRes = R.color.warning
            }
            "A" -> {
                if (tieneJustificacion) {
                    statusLetter = "J"
                    statusText = "Justificado"
                    colorRes = R.color.warning
                    textColorRes = R.color.warning
                } else {
                    statusLetter = "A"
                    statusText = "Ausente"
                    colorRes = R.color.error_light
                    textColorRes = R.color.error
                }
            }
        }

        holder.tvStatusCircle.text = statusLetter
        holder.tvStatusCircle.background.mutate().setTint(ContextCompat.getColor(context, colorRes))

        if (tieneJustificacion && estado == "A") {
             holder.tvStatusCircle.setTextColor(ContextCompat.getColor(context, R.color.warning))
             holder.tvStatusCircle.background.mutate().setTint(Color.parseColor("#FFF9C4")) // Light Yellow
        } else {
             holder.tvStatusCircle.setTextColor(ContextCompat.getColor(context, textColorRes))
        }

        // 4. Justification Logic
        val ultimaModificacion = asistencia.modificaciones?.lastOrNull()
        val motivo = ultimaModificacion?.get("motivo") as? String
            ?: ultimaModificacion?.get("observacion") as? String

        if (tieneJustificacion && !motivo.isNullOrBlank()) {
            holder.layoutJustification.visibility = View.VISIBLE
            holder.tvJustification.text = motivo
        } else {
            holder.layoutJustification.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = listaAsistencias.size
}
