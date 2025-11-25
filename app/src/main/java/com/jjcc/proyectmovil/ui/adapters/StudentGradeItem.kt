package com.jjcc.proyectmovil.ui.adapters

data class StudentGradeItem(
    val studentId: String,
    val studentName: String,
    val studentPhoto: String,
    var grade: Double?,
    val noteId: String? = null
)
