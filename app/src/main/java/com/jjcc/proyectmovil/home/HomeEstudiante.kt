package com.jjcc.proyectmovil.home

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.ItemCurso
import com.jjcc.proyectmovil.core.model.ItemNota
import com.jjcc.proyectmovil.messages.MainChatActivity
import com.jjcc.proyectmovil.profile.PerfilActivity
import com.jjcc.proyectmovil.roles.student.MisAsistencias
import com.jjcc.proyectmovil.roles.student.MisCursos
import com.jjcc.proyectmovil.roles.student.MisNotas
import com.jjcc.proyectmovil.roles.student.MisClasesActivity
import com.jjcc.proyectmovil.roles.student.StudentEvaluationsActivity
import com.jjcc.proyectmovil.ui.adapters.StudentCoursesAdapter
import com.jjcc.proyectmovil.model.Evaluacion
import java.text.SimpleDateFormat
import java.util.Locale

class HomeEstudiante : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tvBienvenida: TextView
    private lateinit var cardPromedio: MaterialCardView
    private lateinit var cardAsistencia: MaterialCardView
    private lateinit var cardProxima: MaterialCardView
    private lateinit var tvSeeAllCourses: TextView
    private lateinit var rvCourses: RecyclerView
    private lateinit var btnNotification: ImageView
    private lateinit var cardNextAssignment: MaterialCardView
    private lateinit var tvNextAssignment: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val coursesList = mutableListOf<ItemCurso>()
    private lateinit var coursesAdapter: StudentCoursesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_estudiante)

        initViews()
        setupBottomNav()
        setupClickListeners()
        fetchCourses()
        fetchUserName()
        fetchNextAssignment()
    }

    private fun initViews() {
        bottomNav = findViewById(R.id.bottomNavigation)
        tvBienvenida = findViewById(R.id.tvBienvenida)
        cardPromedio = findViewById(R.id.cardPromedio)
        cardAsistencia = findViewById(R.id.cardAsistencia)
        cardProxima = findViewById(R.id.cardProxima)
        tvSeeAllCourses = findViewById(R.id.tvSeeAllCourses)
        rvCourses = findViewById(R.id.rvCourses)
        btnNotification = findViewById(R.id.btnNotification)
        cardNextAssignment = findViewById(R.id.cardNextAssignment)
        tvNextAssignment = findViewById(R.id.tvNextAssignment)

        rvCourses.layoutManager = LinearLayoutManager(this)
        coursesAdapter = StudentCoursesAdapter(coursesList)
        rvCourses.adapter = coursesAdapter
    }

    private fun setupClickListeners() {
        cardPromedio.setOnClickListener {
            startActivity(Intent(this, MisNotas::class.java))
        }

        cardAsistencia.setOnClickListener {
            startActivity(Intent(this, MisAsistencias::class.java))
        }

        cardProxima.setOnClickListener {
            // "Próxima hoy" -> Classes (MisClasesActivity)
            startActivity(Intent(this, MisClasesActivity::class.java))
        }

        tvSeeAllCourses.setOnClickListener {
            startActivity(Intent(this, MisCursos::class.java))
        }

        // Notification button placeholder
        btnNotification.setOnClickListener {
            // TODO: Open notifications
        }

        // Next assignment banner click
        cardNextAssignment.setOnClickListener {
            startActivity(Intent(this, StudentEvaluationsActivity::class.java))
        }
    }

    private fun fetchUserName() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Estudiante"
                    tvBienvenida.text = "¡Hola, $name!"
                }
            }
    }

    private fun fetchCourses() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("cursos")
            .whereArrayContains("estudiantesInscritos", userId)
            .get()
            .addOnSuccessListener { documents ->
                coursesList.clear()
                for (document in documents) {
                    val course = document.toObject(ItemCurso::class.java)
                    // Ensure ID is set if not auto-mapped
                    course.id = document.id
                    coursesList.add(course)
                }
                coursesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }





    private fun setupBottomNav() {
        //Desactiva el efecto "ripple"
        bottomNav.itemRippleColor = null
        //Evita la animación o salto brusco al re-seleccionar
        bottomNav.setOnItemReselectedListener {}

        // Seleccionar "home" en el menu de navegación al entrar
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            bottomNav.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(120)
                .withEndAction {
                    bottomNav.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
                .start()

            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_messages -> {
                    val intent = Intent(this, MainChatActivity::class.java)
                    intent.putExtra("USER_ROLE", "ESTUDIANTE")
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    false
                }
                R.id.nav_calendar -> {
                    val intent = Intent(this, com.jjcc.proyectmovil.ui.CalendarActivity::class.java)
                    intent.putExtra("USER_ROLE", "ESTUDIANTE")
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    false
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("USER_ROLE", "ESTUDIANTE")
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    false
                }
                else -> false
            }
        }
    }

    private fun fetchNextAssignment() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("evaluaciones")
            .whereEqualTo("estudianteId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val evaluations = documents.mapNotNull { doc ->
                    doc.toObject(Evaluacion::class.java).copy(id = doc.id)
                }

                // Filter future evaluations and sort by date
                val now = System.currentTimeMillis()
                val upcomingEvaluations = evaluations
                    .filter { it.fecha != null && it.fecha!!.time >= now }
                    .sortedBy { it.fecha }

                if (upcomingEvaluations.isNotEmpty()) {
                    val nextEval = upcomingEvaluations.first()
                    val dateFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))
                    val dateStr = nextEval.fecha?.let { dateFormat.format(it) } ?: ""

                    tvNextAssignment.text = "Próxima Entrega: ${nextEval.titulo} - $dateStr"
                } else {
                    tvNextAssignment.text = "No hay evaluaciones próximas"
                }
            }
            .addOnFailureListener {
                tvNextAssignment.text = "Próxima Entrega: -"
            }
    }
}
