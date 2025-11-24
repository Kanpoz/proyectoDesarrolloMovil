package com.jjcc.proyectmovil.roles.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.auth.NewUserActivity
import com.jjcc.proyectmovil.core.adapter.UserAdminAdapter
import com.jjcc.proyectmovil.core.model.ItemUsuarioAdmin

class GestionUsuariosActivity : AppCompatActivity() {

    private lateinit var tvTotalUsuarios: TextView
    private lateinit var tvTotalAdmins: TextView
    private lateinit var tvTotalDocentes: TextView
    private lateinit var tvTotalEstudiantes: TextView
    private lateinit var etBuscar: EditText
    private lateinit var spFiltroRol: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var btnActualizar: Button
    private lateinit var rvUsuarios: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserAdminAdapter
    private val listaCompleta = mutableListOf<ItemUsuarioAdmin>()
    private lateinit var btnRegistrarUsuario : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_usuarios)

        db = FirebaseFirestore.getInstance()

        tvTotalUsuarios = findViewById(R.id.tvTotalUsuarios)
        tvTotalAdmins = findViewById(R.id.tvTotalAdmins)
        tvTotalDocentes = findViewById(R.id.tvTotalDocentes)
        tvTotalEstudiantes = findViewById(R.id.tvTotalEstudiantes)
        etBuscar = findViewById(R.id.etBuscar)
        spFiltroRol = findViewById(R.id.spFiltroRol)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnActualizar = findViewById(R.id.btnActualizarLista)
        rvUsuarios = findViewById(R.id.rvUsuarios)
        btnRegistrarUsuario = findViewById(R.id.btnRegistrarUsuario)

        val root = findViewById<View>(R.id.rootGestionUsuarios)
        val btnActualizar = findViewById<Button>(R.id.btnActualizarLista)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // añadimos padding abajo igual a la barra de navegación / taskbar
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }

        rvUsuarios.layoutManager = LinearLayoutManager(this)

        adapter = UserAdminAdapter(
            emptyList(),
            onVer = { user -> mostrarDetalle(user) },
            onEditar = { user -> editarUsuario(user) },
            onEliminar = { user -> eliminarUsuario(user) }
        )

        rvUsuarios.adapter = adapter

        btnBuscar.setOnClickListener {
            aplicarFiltros()
        }

        btnActualizar.setOnClickListener {
            cargarUsuarios()
        }

        btnRegistrarUsuario.setOnClickListener {
            startActivity(Intent(this, NewUserActivity::class.java))
        }

        // Cargar por primera vez
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        db.collection("users")
            .get()
            .addOnSuccessListener { snap ->
                listaCompleta.clear()

                var totalAdmins = 0
                var totalDocentes = 0
                var totalEstudiantes = 0

                for (doc in snap.documents) {
                    val id = doc.id
                    val nombre = doc.getString("nombre") ?: ""
                    val email = doc.getString("email") ?: ""
                    val rol = doc.getString("rol") ?: ""
                    val telefono = doc.getString("telefono") ?: ""      // puede venir con +57
                    val activo = doc.getBoolean("activo") ?: true

                    val user = ItemUsuarioAdmin(
                        id = id,
                        nombre = nombre,
                        email = email,
                        rol = rol,
                        telefono = telefono,
                        activo = activo
                    )
                    listaCompleta.add(user)

                    when (rol.uppercase()) {
                        "ADMIN" -> totalAdmins++
                        "DOCENTE" -> totalDocentes++
                        "ESTUDIANTE" -> totalEstudiantes++
                    }
                }

                tvTotalUsuarios.text = listaCompleta.size.toString()
                tvTotalAdmins.text = totalAdmins.toString()
                tvTotalDocentes.text = totalDocentes.toString()
                tvTotalEstudiantes.text = totalEstudiantes.toString()

                aplicarFiltros()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando usuarios: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun aplicarFiltros() {
        val textoBuscar = etBuscar.text.toString().trim().lowercase()
        val rolSeleccionado = spFiltroRol.selectedItem?.toString() ?: "TODOS"

        val filtrados = listaCompleta.filter { user ->
            val coincideRol = if (rolSeleccionado == "TODOS") {
                true
            } else {
                user.rol.equals(rolSeleccionado, ignoreCase = true)
            }

            val coincideTexto =
                textoBuscar.isEmpty() ||
                        user.nombre.lowercase().contains(textoBuscar) ||
                        user.email.lowercase().contains(textoBuscar) ||
                        user.telefono.lowercase().contains(textoBuscar)

            coincideRol && coincideTexto
        }

        adapter.actualizarLista(filtrados)
    }

    // 🔍 Ver detalle de un usuario
    private fun mostrarDetalle(user: ItemUsuarioAdmin) {
        db.collection("users")
            .document(user.id)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val dialogView = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_detalle_usuario_admin, null)

                // HEADER
                val imgFoto = dialogView.findViewById<ImageView>(R.id.imgFotoDetalle)
                val tvNombre = dialogView.findViewById<TextView>(R.id.tvNombreDetalle)
                val tvRolEstado = dialogView.findViewById<TextView>(R.id.tvRolEstado)

                // TABS
                val tabDatosBase = dialogView.findViewById<TextView>(R.id.tabDatosBase)
                val tabMetadatos = dialogView.findViewById<TextView>(R.id.tabMetadatos)

                // CONTENEDORES
                val layoutDatosBase = dialogView.findViewById<LinearLayout>(R.id.layoutDatosBase)
                val layoutMetadatos = dialogView.findViewById<LinearLayout>(R.id.layoutMetadatos)

                // DATOS BASE
                val tvIdUnico = dialogView.findViewById<TextView>(R.id.tvIdUnico)
                val tvEmail = dialogView.findViewById<TextView>(R.id.tvEmailDetalle)
                val tvTelefono = dialogView.findViewById<TextView>(R.id.tvTelefonoDetalle)
                val tvRol = dialogView.findViewById<TextView>(R.id.tvRolDetalle)
                val tvFechaCreacion = dialogView.findViewById<TextView>(R.id.tvFechaCreacion)
                val tvUltimoAcceso = dialogView.findViewById<TextView>(R.id.tvUltimoAcceso)

                // METADATOS Y ACCESO
                val tvNotifPush = dialogView.findViewById<TextView>(R.id.tvNotifPush)
                val tvNotifEmail = dialogView.findViewById<TextView>(R.id.tvNotifEmail)
                val tvIdiomaPref = dialogView.findViewById<TextView>(R.id.tvIdiomaPref)
                val tvTemaOscuro = dialogView.findViewById<TextView>(R.id.tvTemaOscuro)
                val tvTokensFcm = dialogView.findViewById<TextView>(R.id.tvTokensFcm)

                val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrarDetalle)

                // ---------- LECTURA DE DATOS ----------
                val nombre = doc.getString("nombre") ?: user.nombre
                val email = doc.getString("email") ?: user.email
                val telefono = doc.getString("telefono") ?: user.telefono
                val rol = doc.getString("rol") ?: user.rol
                val activo = doc.getBoolean("activo") ?: user.activo
                val idUnico = doc.getString("id") ?: doc.id
                val fotoPerfil = doc.getString("fotoPerfil")

                val fechaCreacionTs = doc.getTimestamp("fechaCreacion")
                val ultimoAccesoTs = doc.getTimestamp("ultimoAcceso")

                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                val fechaCreacionStr = fechaCreacionTs?.toDate()?.let { sdf.format(it) } ?: "-"
                val ultimoAccesoStr = ultimoAccesoTs?.toDate()?.let { sdf.format(it) } ?: "-"

                // Preferencias (map)
                val notifPushBool = doc.getBoolean("preferencias.notificacionesPush") ?: false
                val notifEmailBool = doc.getBoolean("preferencias.notificacionesEmail") ?: false
                val idioma = doc.getString("preferencias.idioma") ?: "ES"
                val temaOscuroBool = doc.getBoolean("preferencias.temaOscuro") ?: false
                val tokensList = doc.get("tokensFCM") as? List<*>
                val tokensCount = tokensList?.size ?: 0

                // ---------- RELLENAR HEADER ----------
                tvNombre.text = nombre
                val estadoTexto = if (activo) "Activo ✓" else "Inactivo"
                val estadoColor = if (activo)
                    android.graphics.Color.parseColor("#2E7D32")
                else
                    android.graphics.Color.RED
                tvRolEstado.text = "$rol | $estadoTexto"
                tvRolEstado.setTextColor(estadoColor)

                // Foto (si usas Glide)
                try {
                    if (!fotoPerfil.isNullOrBlank()) {
                        com.bumptech.glide.Glide.with(this)
                            .load(fotoPerfil)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(imgFoto)
                    } else {
                        imgFoto.setImageResource(R.drawable.ic_person)
                    }
                } catch (e: Exception) {
                    imgFoto.setImageResource(R.drawable.ic_person)
                }

                // ---------- DATOS BASE ----------
                tvIdUnico.text = idUnico
                tvEmail.text = email
                tvTelefono.text = telefono
                tvRol.text = rol
                tvFechaCreacion.text = fechaCreacionStr
                tvUltimoAcceso.text = ultimoAccesoStr

                // ---------- METADATOS Y ACCESO ----------
                tvNotifPush.text = if (notifPushBool) "Activadas" else "Desactivadas"
                tvNotifEmail.text = if (notifEmailBool) "Activadas" else "Desactivadas"
                tvIdiomaPref.text = idioma.uppercase()
                tvTemaOscuro.text = if (temaOscuroBool) "Activado" else "Desactivado"
                tvTokensFcm.text = tokensCount.toString()

                // ---------- MANEJO DE TABS ----------
                fun selectTab(tab: Int) {
                    // reset estilos
                    val gris = android.graphics.Color.parseColor("#888888")
                    val negro = android.graphics.Color.parseColor("#000000")

                    tabDatosBase.setTextColor(gris)
                    tabMetadatos.setTextColor(gris)

                    layoutDatosBase.visibility = View.GONE
                    layoutMetadatos.visibility = View.GONE

                    when (tab) {
                        0 -> {
                            tabDatosBase.setTextColor(negro)
                            layoutDatosBase.visibility = View.VISIBLE
                        }
                        1 -> {
                            tabMetadatos.setTextColor(negro)
                            layoutMetadatos.visibility = View.VISIBLE
                        }
                    }
                }

                tabDatosBase.setOnClickListener { selectTab(0) }
                tabMetadatos.setOnClickListener { selectTab(1) }

                // Tab inicial: Datos Base
                selectTab(0)

                val dialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create()

                btnCerrar.setOnClickListener { dialog.dismiss() }

                dialog.show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando detalle: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ✏️ Editar usuario (nombre, teléfono, rol, activo)
    private fun editarUsuario(user: ItemUsuarioAdmin) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_editar_usuario_admin, null)

        val etNombre = view.findViewById<EditText>(R.id.etNombreUsuarioAdmin)
        val etTelefono = view.findViewById<EditText>(R.id.etTelefonoUsuarioAdmin)
        val spRol = view.findViewById<Spinner>(R.id.spRolUsuarioAdmin)
        val swActivo = view.findViewById<SwitchCompat>(R.id.swActivoUsuarioAdmin)

        // Cargar datos actuales
        etNombre.setText(user.nombre)
        etTelefono.setText(user.telefono)
        swActivo.isChecked = user.activo

        // Spinner de roles
        ArrayAdapter.createFromResource(
            this,
            R.array.roles_usuario_admin,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spRol.adapter = adapter
        }

        val indexRol = resources.getStringArray(R.array.roles_usuario_admin)
            .indexOfFirst { it.equals(user.rol, ignoreCase = true) }
        if (indexRol >= 0) {
            spRol.setSelection(indexRol)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar usuario")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = etNombre.text.toString().trim()
                val nuevoTelefono = etTelefono.text.toString().trim()
                val nuevoActivo = swActivo.isChecked
                val nuevoRol = spRol.selectedItem.toString().uppercase()

                // 🔐 Validaciones básicas
                if (nuevoNombre.isEmpty()) {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (nuevoTelefono.isEmpty()) {
                    Toast.makeText(this, "El teléfono no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // extraemos solo dígitos (para ignorar +57) y exigimos 10
                val soloDigitos = nuevoTelefono.filter { it.isDigit() }
                if (soloDigitos.length != 10) {
                    Toast.makeText(
                        this,
                        "El teléfono debe tener exactamente 10 números (sin contar +57)",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // 🔄 Actualizar en Firestore
                val updates = mapOf(
                    "nombre" to nuevoNombre,
                    "telefono" to nuevoTelefono,
                    "activo" to nuevoActivo,
                    "rol" to nuevoRol
                )

                db.collection("users")
                    .document(user.id)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()

                        // Actualizar en la lista local
                        val idx = listaCompleta.indexOfFirst { it.id == user.id }
                        if (idx != -1) {
                            val actualizado = user.copy(
                                nombre = nuevoNombre,
                                telefono = nuevoTelefono,
                                activo = nuevoActivo,
                                rol = nuevoRol
                            )
                            listaCompleta[idx] = actualizado
                            aplicarFiltros()

                            // Recalcular contadores
                            tvTotalUsuarios.text = listaCompleta.size.toString()
                            tvTotalAdmins.text =
                                listaCompleta.count { it.rol.equals("ADMIN", ignoreCase = true) }.toString()
                            tvTotalDocentes.text =
                                listaCompleta.count { it.rol.equals("DOCENTE", ignoreCase = true) }.toString()
                            tvTotalEstudiantes.text =
                                listaCompleta.count { it.rol.equals("ESTUDIANTE", ignoreCase = true) }.toString()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🗑 Eliminar usuario
    private fun eliminarUsuario(user: ItemUsuarioAdmin) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar usuario")
            .setMessage("¿Seguro que deseas eliminar a ${user.nombre}? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("users")
                    .document(user.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()

                        // Quitar de la lista local
                        listaCompleta.removeAll { it.id == user.id }
                        aplicarFiltros()

                        // Recalcular contadores
                        tvTotalUsuarios.text = listaCompleta.size.toString()
                        tvTotalAdmins.text =
                            listaCompleta.count { it.rol.equals("ADMIN", ignoreCase = true) }.toString()
                        tvTotalDocentes.text =
                            listaCompleta.count { it.rol.equals("DOCENTE", ignoreCase = true) }.toString()
                        tvTotalEstudiantes.text =
                            listaCompleta.count { it.rol.equals("ESTUDIANTE", ignoreCase = true) }.toString()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}