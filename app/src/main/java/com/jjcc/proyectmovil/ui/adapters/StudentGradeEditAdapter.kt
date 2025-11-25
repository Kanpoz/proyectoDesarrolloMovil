package com.jjcc.proyectmovil.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jjcc.proyectmovil.R

class StudentGradeEditAdapter(
    private val students: List<StudentGradeItem>
) : RecyclerView.Adapter<StudentGradeEditAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivStudentPhoto: ImageView = view.findViewById(R.id.ivStudentAvatar)
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val etGrade: EditText = view.findViewById(R.id.etGrade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_grade, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]

        holder.tvStudentName.text = student.studentName

        // Load student photo
        if (student.studentPhoto.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(student.studentPhoto)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(holder.ivStudentPhoto)
        } else {
            holder.ivStudentPhoto.setImageResource(R.drawable.ic_person)
        }

        // Set grade
        holder.etGrade.setText(student.grade?.toString() ?: "")

        // Remove previous text watcher to avoid conflicts
        holder.etGrade.tag?.let { tag ->
            if (tag is TextWatcher) {
                holder.etGrade.removeTextChangedListener(tag)
            }
        }

        // Add text watcher to update grade
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val gradeText = s?.toString()
                student.grade = gradeText?.toDoubleOrNull()
            }
        }
        holder.etGrade.addTextChangedListener(textWatcher)
        holder.etGrade.tag = textWatcher
    }

    override fun getItemCount() = students.size
}
