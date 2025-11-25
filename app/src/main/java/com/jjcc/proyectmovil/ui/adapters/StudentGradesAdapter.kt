package com.jjcc.proyectmovil.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemNota
import java.util.Locale

data class CourseGrade(
    val courseId: String,
    val courseName: String,
    val average: Double,
    val evaluations: List<ItemNota>,
    var isExpanded: Boolean = false
)

class StudentGradesAdapter(
    private val courses: List<CourseGrade>
) : RecyclerView.Adapter<StudentGradesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseName: TextView = view.findViewById(R.id.tvCourseName)
        val courseAverage: TextView = view.findViewById(R.id.tvCourseAverage)
        val overallGrade: TextView = view.findViewById(R.id.tvOverallGrade)
        val colorIndicator: View = view.findViewById(R.id.viewColorIndicator)
        val chevron: ImageView = view.findViewById(R.id.ivChevron)
        val layoutEvaluations: LinearLayout = view.findViewById(R.id.layoutEvaluations)
        val layoutHeader: LinearLayout = view.findViewById(R.id.layoutHeader)
        val divider: View = view.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_course_grade, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courses[position]

        holder.courseName.text = course.courseName
        holder.courseAverage.text = String.format(Locale.getDefault(), "Promedio: %.1f", course.average)
        holder.overallGrade.text = String.format(Locale.getDefault(), "%.1f", course.average)

        // Set color based on grade or random/hashed from ID
        val colorRes = when {
            course.average >= 4.5 -> R.color.success
            course.average >= 3.0 -> R.color.warning
            else -> R.color.error
        }
        val color = ContextCompat.getColor(holder.itemView.context, colorRes)
        holder.colorIndicator.setBackgroundColor(color)
        holder.overallGrade.setTextColor(color)

        // Handle Expansion
        val isExpanded = course.isExpanded
        holder.layoutEvaluations.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.divider.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.chevron.rotation = if (isExpanded) 180f else 0f

        holder.layoutHeader.setOnClickListener {
            course.isExpanded = !course.isExpanded
            notifyItemChanged(position)
        }

        // Populate Evaluations
        holder.layoutEvaluations.removeAllViews()
        if (course.evaluations.isEmpty()) {
            val emptyView = TextView(holder.itemView.context).apply {
                text = "Sin notas aún"
                setTextColor(ContextCompat.getColor(context, R.color.text_tertiary))
                textSize = 14f
                setPadding(0, 8, 0, 8)
            }
            holder.layoutEvaluations.addView(emptyView)
        } else {
            for (eval in course.evaluations) {
                val evalView = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.item_student_evaluation_row, holder.layoutEvaluations, false)

                val tvName = evalView.findViewById<TextView>(R.id.tvEvaluationName)
                val tvGrade = evalView.findViewById<TextView>(R.id.tvEvaluationGrade)
                val cardBadge = evalView.findViewById<MaterialCardView>(R.id.cardGradeBadge)

                tvName.text = eval.titulo ?: "Actividad"
                val grade = eval.nota ?: 0.0
                tvGrade.text = String.format(Locale.getDefault(), "%.1f", grade)

                // Badge Color
                val badgeColorRes = when {
                    grade >= 4.5 -> R.color.success_light
                    grade >= 3.0 -> R.color.warning // You might need a warning_light
                    else -> R.color.error_light
                }
                val textColorRes = when {
                    grade >= 4.5 -> R.color.success
                    grade >= 3.0 -> R.color.warning
                    else -> R.color.error
                }

                // Fallback for warning_light if not exists, use yellow tint
                // Assuming success_light and error_light exist from colors.xml check

                cardBadge.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, badgeColorRes))
                tvGrade.setTextColor(ContextCompat.getColor(holder.itemView.context, textColorRes))

                holder.layoutEvaluations.addView(evalView)
            }
        }
    }

    override fun getItemCount() = courses.size
}
