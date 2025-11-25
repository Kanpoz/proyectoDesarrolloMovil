package com.jjcc.proyectmovil.messages

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jjcc.proyectmovil.R
import com.jjcc.proyectmovil.core.model.User

class NewConversationActivity : AppCompatActivity() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: NewConversationAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var searchBox: android.widget.EditText
    private var allUsers: ArrayList<User> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_conversation)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        searchBox = findViewById(R.id.searchBox)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        userList = ArrayList()
        adapter = NewConversationAdapter(this, userList)

        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        searchBox.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        fetchUsers()
    }

    private fun fetchUsers() {
        val currentUid = mAuth.currentUser?.uid ?: return

        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                allUsers.clear()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    // Ensure uid is set if not present in document but is document id
                    if (user.uid == null) {
                        user.uid = document.id
                    }

                    // Exclude current user
                    if (user.uid != currentUid) {
                        userList.add(user)
                        allUsers.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    private fun filterUsers(query: String) {
        val filteredList = ArrayList<User>()
        val lowerCaseQuery = query.lowercase()

        for (user in allUsers) {
            val fullName = user.getNombreCompleto().lowercase()
            val email = user.email?.lowercase() ?: ""
            val role = user.tipoRol?.lowercase() ?: ""
            val uid = user.uid?.lowercase() ?: ""

            if (fullName.contains(lowerCaseQuery) ||
                email.contains(lowerCaseQuery) ||
                role.contains(lowerCaseQuery) ||
                uid.contains(lowerCaseQuery)) {
                filteredList.add(user)
            }
        }
        adapter.updateList(filteredList)
    }
}
