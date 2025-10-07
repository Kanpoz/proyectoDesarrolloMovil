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

class MainChatActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

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

        // MEJORA: Agregado logs para debug
        Log.d("ChatMain", "Usuario actual: ${mAuth.currentUser?.uid}")

        mDbRef.child("usuarios").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ChatMain", "Total usuarios en Firebase: ${snapshot.childrenCount}")

                userList.clear()
                for (post in snapshot.children) {
                    Log.d("ChatMain", "Procesando usuario: ${post.key}")

                    // MEJORA: Mejor manejo de errores al convertir datos
                    try {
                        val u = post.getValue(User::class.java)
                        if (u != null) {
                            u.uid = post.key
                            Log.d(
                                "ChatMain",
                                "Usuario cargado: ${u.nombres} ${u.apellidos}, UID: ${u.uid}"
                            )

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
    }
}
