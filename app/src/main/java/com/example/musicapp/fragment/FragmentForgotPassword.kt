package com.example.musicapp.fragment

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentForgotPasswordBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class FragmentForgotPassword : Fragment() {
    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var fragmentManager: FragmentManager
    private lateinit var email: String
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        fragmentManager = requireActivity().supportFragmentManager
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        //initialize Firebase Auth
        auth = Firebase.auth
        //initialize Firebase database
        database = Firebase.database.reference

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        binding.tvSignIn.setOnClickListener {
            fragmentManager.popBackStack()
        }

        binding.btnSend.setOnClickListener {
            email = binding.email.text.toString().trim()
            if (email.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all details", Toast.LENGTH_SHORT)
                    .show()
            } else {
                resetPassword(email)

            }
        }



        return binding.root
    }

    private fun resetPassword(email: String) {
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Email sent. Please check your email", Toast.LENGTH_SHORT)
                        .show()
                    Log.d(TAG, "Email sent.")
                    fragmentManager.popBackStack()
                }
            }


    }

    fun onBackPressed(): Boolean {
        fragmentManager.popBackStack()
        return true // hoặc false tùy vào xử lý của bạn
    }


}