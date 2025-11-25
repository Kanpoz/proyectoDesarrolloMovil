package com.jjcc.proyectmovil.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.auth.LoginActivity
import com.jjcc.proyectmovil.home.HomeEstudiante
import com.jjcc.proyectmovil.messages.MainChatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.jjcc.proyectmovil.home.HomeAdmin
import com.jjcc.proyectmovil.home.HomeDocente

class PerfilActivity : AppCompatActivity() {

    private lateinit var tvRol: TextView
    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var imgPerfil: ImageView
    private lateinit var btnLogout: Button
    private lateinit var btnEditarPerfil: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        mAuth = FirebaseAuth.getInstance()

        tvRol = findViewById(R.id.tvRol)
        tvNombre = findViewById(R.id.tvNombre)
        tvCorreo = findViewById(R.id.tvCorreo)
        tvTelefono = findViewById(R.id.tvTelefono)
        tvDireccion = findViewById(R.id.tvDireccion)
        imgPerfil = findViewById(R.id.imgPerfil)
        btnLogout = findViewById(R.id.btnCerrarSesion)
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil)
        bottomNav = findViewById(R.id.bottomNavigation)

        // Obtener rol desde Intent o Firestore
        userRole = intent.getStringExtra("USER_ROLE")
        if (userRole == null) {
            obtenerRolUsuario { rol ->
                userRole = rol
            }
        }

        cargarDatosUsuario()

        btnLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }

        btnEditarPerfil.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java).apply {
                putExtra("nombre", tvNombre.text.toString())
                putExtra("telefono", tvTelefono.text.toString())
                putExtra("direccion", tvDireccion.text.toString())
            }
            startActivity(intent)
        }

        //Desactiva el efecto "ripple" (el círculo que se expande al tocar)
        bottomNav.itemRippleColor = null

        //Evita la animación o salto brusco al re-seleccionar
        bottomNav.setOnItemReselectedListener {}

        // Seleccionar "perfil" al entrar
        bottomNav.selectedItemId = R.id.nav_profile

        // Manejo de clics en el menú
        bottomNav.setOnItemSelectedListener { item ->

            // ANIMACIÓN EXACTA QUE YA USAS
            bottomNav.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(120)
                .withEndAction {
                    bottomNav.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
                .start()

            // 🔥 OBTENER ROL DESDE VARIABLE CACHEADA
            val rol = userRole ?: "ESTUDIANTE" // Fallback seguro

            when (item.itemId) {

                R.id.nav_home -> {
                    val intent = when (rol) {
                        "ADMIN" -> Intent(this, HomeAdmin::class.java)
                        "DOCENTE" -> Intent(this, HomeDocente::class.java)
                        "ESTUDIANTE" -> Intent(this, HomeEstudiante::class.java)
                        else -> Intent(this, HomeEstudiante::class.java)
                    }

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }

                R.id.nav_messages -> {
                    val intent = Intent(this, MainChatActivity::class.java)
                    intent.putExtra("USER_ROLE", rol)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }

                R.id.nav_calendar -> {
                    val intent = Intent(this, com.jjcc.proyectmovil.ui.CalendarActivity::class.java)
                    intent.putExtra("USER_ROLE", rol)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }

                R.id.nav_profile -> {
                    // Ya estamos aquí
                }
            }

            false // Return false to prevent reselection issues
        }
    }

    private fun cargarDatosUsuario() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {

                    val nombre = doc.getString("nombre") ?: "Sin nombre"
                    val rol = doc.getString("rol") ?: "Sin rol"
                    val correo = doc.getString("email") ?: "No registrado"
                    val telefono = doc.getString("telefono") ?: "No registrado"
                    val direccion = doc.getString("direccion") ?: "No registrado"
                    var fotoPerfilUrl = doc.getString("fotoPerfil")

                    tvNombre.text = nombre
                    tvRol.text = rol
                    tvCorreo.text = correo
                    tvTelefono.text = telefono
                    tvDireccion.text = direccion

                    // Sanitize invalid local paths
                    if (fotoPerfilUrl != null && fotoPerfilUrl.startsWith("/com/")) {
                        fotoPerfilUrl = null
                    }

                    // Foto de perfil: si hay URL en Firestore, la carga; si no, usa ic_person
                    if (!fotoPerfilUrl.isNullOrBlank()) {
                        Glide.with(this)
                            .load(fotoPerfilUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .circleCrop()
                            .into(imgPerfil)
                    } else {
                        imgPerfil.setImageResource(R.drawable.ic_person)
                    }

                } else {
                    Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerRolUsuario(callback: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val rol = doc.getString("rol") ?: "DESCONOCIDO"
                callback(rol.uppercase())
            }
            .addOnFailureListener {
                callback("DESCONOCIDO")
            }
    }
}
