package com.jjcc.proyectmovil.roles.docente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R

data class StudentAttendance(
    val id: String,
    val name: String,
    var status: String? = null, // "P", "A", "J"
    var observaciones: String? = null,
    var originalStatus: String? = null,
    var originalObservaciones: String? = null
) {
    fun hasChanges(): Boolean {
        return status != originalStatus || observaciones != originalObservaciones
    }
}

class GestionAsistenciaAdapter(
    private val students: List<StudentAttendance>,
    private val onStatusChanged: (String, String) -> Unit
) : RecyclerView.Adapter<GestionAsistenciaAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        val btnP: TextView = itemView.findViewById(R.id.btnPresente)
        val btnA: TextView = itemView.findViewById(R.id.btnAusente)
        val btnJ: TextView = itemView.findViewById(R.id.btnJustificado)

        fun bind(student: StudentAttendance) {
            tvName.text = student.name

            updateSelection(student.status)

            btnP.setOnClickListener {
                student.status = "P"
                student.observaciones = null
                updateSelection("P")
                onStatusChanged(student.id, "P")
            }
            btnA.setOnClickListener {
                student.status = "A"
                student.observaciones = null
                updateSelection("A")
                onStatusChanged(student.id, "A")
            }
            btnJ.setOnClickListener {
                // Mostrar diálogo para ingresar observaciones
                val context = itemView.context
                val input = android.widget.EditText(context)
                input.hint = "Motivo de justificación"
                input.setText(student.observaciones ?: "")

                // Agregar padding al EditText
                val padding = (16 * context.resources.displayMetrics.density).toInt()
                val container = android.widget.FrameLayout(context)
                container.setPadding(padding, padding / 2, padding, 0)
                container.addView(input)

                android.app.AlertDialog.Builder(context)
                    .setTitle("Justificación")
                    .setMessage("Ingrese el motivo de la justificación:")
                    .setView(container)
                    .setPositiveButton("Guardar") { _, _ ->
                        student.status = "J"
                        student.observaciones = input.text.toString()
                        updateSelection("J")
                        onStatusChanged(student.id, "J")
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        private fun updateSelection(status: String?) {
            val context = itemView.context
            val selectedBg = R.drawable.bg_attendance_option_selected
            val unselectedBg = R.drawable.bg_attendance_option_unselected
            val selectedColor = ContextCompat.getColor(context, R.color.white)
            val unselectedColor = ContextCompat.getColor(context, R.color.text_secondary)

            // Reset all
            btnP.setBackgroundResource(unselectedBg)
            btnP.setTextColor(unselectedColor)
            btnA.setBackgroundResource(unselectedBg)
            btnA.setTextColor(unselectedColor)
            btnJ.setBackgroundResource(unselectedBg)
            btnJ.setTextColor(unselectedColor)

            when (status) {
                "P" -> {
                    btnP.setBackgroundResource(selectedBg)
                    btnP.setTextColor(selectedColor)
                }
                "A" -> {
                    btnA.setBackgroundResource(selectedBg)
                    btnA.setTextColor(selectedColor)
                }
                "J" -> {
                    btnJ.setBackgroundResource(selectedBg)
                    btnJ.setTextColor(selectedColor)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asistencia_estudiante, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(students[position])
    }

    override fun getItemCount(): Int = students.size
}
