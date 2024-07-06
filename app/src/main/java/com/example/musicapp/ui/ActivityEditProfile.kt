package com.example.musicapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityEditProfileBinding
import com.example.musicapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ActivityEditProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private var imageUrl: String? = null // Store image URL in Firebase Storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
        setUserData()

        binding.btnUpdate.setOnClickListener {
            val name = binding.name.text.toString()
            val email = binding.email.text.toString()

            updateUserData(name, email)
            finish()
        }

        binding.tvChangeAvt.setOnClickListener {
            // Check for permission before starting image picker
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker.launch("image/*")
            } else {
                // Request permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
            }
            Log.d("com.example.musicapp.ui.ActivityEditProfile", "choose img")
        }
    }

    private val openImagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri ->
            uploadImageToFirebase(imageUri)
            val name = binding.name.text.toString()
            val email = binding.email.text.toString()
        } ?: run {
            Log.e("com.example.musicapp.ui.ActivityEditProfile", "Image URI is null")
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("user").child(userId)
            userRef.child("image").setValue(imageUrl).addOnSuccessListener {
                // Update ImageView with new image URL
                updateProfileImage(imageUrl)
                Toast.makeText(this, "Image URL saved to database", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save image URL to database: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("avatars/$userId/avatar.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Image uploaded successfully, get download URL
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        imageUrl = uri.toString()
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        // Update user profile with imageUrl
                        saveImageUrlToDatabase(imageUrl!!)
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfileImage(imageUrl: String?) {
        // Update ImageView with new image URL
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.baseline_person_24) // Placeholder image while loading
                .error(R.drawable.baseline_person_24) // Error image if loading fails
                .into(binding.imgProfile)
        }
    }

    private fun updateUserData(name: String, email: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("user").child(userId)
            val updates = hashMapOf<String, Any>(
                "username" to name,
                "email" to email,
                "image" to imageUrl.orEmpty() // Save imageUrl in Firebase Database
            )
            userRef.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(this, "Profile has been updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("user").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(User::class.java)
                        if (userProfile != null) {
                            binding.name.setText(userProfile.username)
                            binding.email.setText(userProfile.email)
                            imageUrl = userProfile.image

                            // Load profile image if available
                            updateProfileImage(imageUrl)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ActivityEditProfile, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1001
    }
}
