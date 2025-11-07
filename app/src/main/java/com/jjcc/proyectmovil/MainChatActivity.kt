package com.jjcc.proyectmovil

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainChatActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var bottomNav: BottomNavigationView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true) {
            startGeofenceService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chat)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        userList = ArrayList()
        adapter = UserAdapter(this, userList)

        userRecyclerView = findViewById(R.id.userRecyclerview)

        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        // Seleccionar "home" al entrar
        bottomNav = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_messages

        // MEJORA: Agregado logs para debug
        Log.d("ChatMain", "Usuario actual: ${mAuth.currentUser?.uid}")

        mDbRef.child("usuarios").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ChatMain", "Total usuarios en Firebase: ${snapshot.childrenCount}")

                userList.clear()
                for(post in snapshot.children){
                    Log.d("ChatMain", "Procesando usuario: ${post.key}")

                    // MEJORA: Mejor manejo de errores al convertir datos
                    try {
                        val u = post.getValue(User::class.java)
                        if (u != null) {
                            u.uid = post.key
                            Log.d("ChatMain", "Usuario cargado: ${u.nombres} ${u.apellidos}, UID: ${u.uid}")

                            // Solo agregar usuarios que no sean el actual
                            if (mAuth.currentUser?.uid != u.uid) {
                                userList.add(u)
                                Log.d("ChatMain", "Usuario agregado a la lista")
                            } else {
                                Log.d("ChatMain", "Usuario actual omitido")
                            }
                        } else {
                            Log.w("ChatMain", "No se pudo convertir usuario: ${post.key}")
                        }
                    } catch (e: Exception) {
                        Log.e("ChatMain", "Error al procesar usuario ${post.key}: ${e.message}")
                    }
                }

                Log.d("ChatMain", "Total usuarios en lista: ${userList.size}")
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatMain", "Error al cargar usuarios: ${error.message}")
            }
        })

        // Permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startGeofenceService()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }

        // Manejo de clics en el menú
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.nav_messages -> {
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                else -> false
            }
        }
    }

    private fun startGeofenceService() {
        val serviceIntent = Intent(this, GeofenceService::class.java)
        startService(serviceIntent)
    }
}
