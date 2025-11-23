package com.jjcc.proyectmovil.core.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.core.model.ItemAsistencia
import com.jjcc.proyectmovil.R
import java.text.SimpleDateFormat
import java.util.Locale

class AsistenciaAdapter(private val listaAsistencias: List<ItemAsistencia>) :
    RecyclerView.Adapter<AsistenciaAdapter.AsistenciaViewHolder>() {

    class AsistenciaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvEstadoOld: TextView = view.findViewById(R.id.tvEstadoOld)
        val tvEstadoNew: TextView = view.findViewById(R.id.tvEstadoNew)
        val tvMotivo: TextView = view.findViewById(R.id.tvMotivo)
        val tvMotivoTexto: TextView = view.findViewById(R.id.tvMotivoTexto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsistenciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asistencia, parent, false)
        return AsistenciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AsistenciaViewHolder, position: Int) {
        val asistencia = listaAsistencias[position]

        // Formato de fecha
        val fechaTexto = asistencia.fecha?.toDate()?.let {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
        } ?: "Sin fecha"

        holder.tvFecha.text = "Fecha: $fechaTexto"

        // Obtener modificación más reciente (si existe)
        val ultimaModificacion = asistencia.modificaciones?.lastOrNull()

        holder.tvEstadoOld.text = "Estado anterior: ${ultimaModificacion?.get("estadoAnterior") ?: "-"}"
        holder.tvEstadoNew.text = "Nuevo estado: ${ultimaModificacion?.get("estadoNuevo") ?: asistencia.estado ?: "-"}"
        holder.tvMotivoTexto.text = ultimaModificacion?.get("motivo")?.toString() ?: "Sin motivo"
    }

    override fun getItemCount(): Int = listaAsistencias.size
}