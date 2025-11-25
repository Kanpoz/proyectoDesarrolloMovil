package com.jjcc.proyectmovil.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemCurso

class StudentCoursesAdapter(private val courses: List<ItemCurso>) :
    RecyclerView.Adapter<StudentCoursesAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCourseName: TextView = itemView.findViewById(R.id.tvCourseName)
        val tvAttendance: TextView = itemView.findViewById(R.id.tvAttendance)
        val tvGrade: TextView = itemView.findViewById(R.id.tvGrade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_card, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.tvCourseName.text = "${course.nombre} ${course.grado}° ${course.seccion}"

        // Placeholder data for now as per design request
        // In a real app, this would come from a separate query or extended model
        holder.tvAttendance.text = "100% asistencia"
        holder.tvGrade.text = "A"
    }

    override fun getItemCount(): Int = courses.size
}
