package com.jjcc.proyectmovil.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.jjcc.proyectmovil.R
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jjcc.proyectmovil.core.adapter.UserAdapter
import com.jjcc.proyectmovil.home.HomeEstudiante
import com.jjcc.proyectmovil.profile.PerfilActivity

class MainChatActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<ConversationRow>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chat)

        bottomNav = findViewById(R.id.bottomNavigation)
        userRecyclerView = findViewById(R.id.userRecyclerview)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        userList = ArrayList()
        adapter = UserAdapter(this, userList)

        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        // Desactivar ripple y reselección
        bottomNav.itemRippleColor = null
        bottomNav.setOnItemReselectedListener {}
        bottomNav.selectedItemId = R.id.nav_messages

        // Bottom navigation
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
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeEstudiante::class.java))
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

        // 🔹 Escuchar el índice de chats del usuario actual
        listenToChatIndex()
    }

    private fun listenToChatIndex() {
        val currentUid = mAuth.currentUser?.uid
        if (currentUid == null) {
            Log.w("MainChatActivity", "No hay usuario autenticado")
            return
        }

        mDbRef.child("chat_index")
            .child(currentUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    userList.clear()

                    for (convSnap in snapshot.children) {
                        val convId = convSnap.key ?: continue

                        val parts = convId.split("_")
                        val partnerId = when (parts.size) {
                            2 if parts[0] == currentUid -> parts[1]
                            2 if parts[1] == currentUid -> parts[0]
                            else -> null
                        }

                        val nombreChat = convSnap.child("nombreChat").getValue(String::class.java)
                        val lastMessageText = convSnap.child("lastMessageText").getValue(String::class.java)
                        val lastMessageDate = convSnap.child("lastMessageDate").getValue(Long::class.java)
                        val unreadCount = convSnap.child("unreadCount").getValue(Long::class.java)

// 🔹 NUEVO
                        val fotoPerfil = convSnap.child("fotoPerfil").getValue(String::class.java)

                        val row = ConversationRow(
                            convId = convId,
                            partnerId = partnerId,
                            partnerName = nombreChat,
                            lastMessageText = lastMessageText,
                            lastMessageDate = lastMessageDate,
                            unreadCount = unreadCount,
                            fotoPerfil = fotoPerfil
                        )

                        userList.add(row)
                    }

                    // Ordenar por fecha del último mensaje (más recientes arriba)
                    userList.sortByDescending { it.lastMessageDate ?: 0L }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainChatActivity", "Error leyendo chat_index", error.toException())
                }
            })
    }
}