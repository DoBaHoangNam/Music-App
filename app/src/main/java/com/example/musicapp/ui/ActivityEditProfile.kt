package com.example.musicapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.musicapp.databinding.ActivityEditProfileBinding
import com.example.musicapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityEditProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)

        binding.btnBack.setOnClickListener {
            finish()
        }
        setUserData()

        binding.btnUpdate.setOnClickListener {
            val name = binding.email.text.toString()
            val email = binding.name.text.toString()

            updateUserData(name,email)
            finish()
        }

        setContentView(binding.root)
    }

    private fun updateUserData(name: String, email: String) {
        val userId = auth.currentUser?.uid
        if(userId != null){
            val userRef = database.getReference("user").child(userId)
            val userData = hashMapOf(
                "name" to name,
                "email" to email,
            )
            userRef.setValue(userData).addOnSuccessListener {
                Toast.makeText(this, "Profile has been updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Profile has been updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUserData(){
        val userId = auth.currentUser?.uid
        if(userId!=null){
            val userRef = database.getReference("user").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val userProfile = snapshot.getValue(User::class.java)
                        if (userProfile != null){
                            binding.name.setText(userProfile.username)
                            binding.email.setText(userProfile.email)

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }
}