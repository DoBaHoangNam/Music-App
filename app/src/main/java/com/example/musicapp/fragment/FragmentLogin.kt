package com.example.musicapp.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentLoginBinding
import com.example.musicapp.model.User
import com.example.musicapp.ui.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class FragmentLogin : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        //initialize Firebase Auth
        auth = Firebase.auth
        //initialize Firebase database
        database = Firebase.database.reference

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        sharedPreferences = requireContext().getSharedPreferences("login_state", Context.MODE_PRIVATE)

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentLogin2_to_fragmentSignUp)
        }

        binding.btnSignIn.setOnClickListener {
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all details", Toast.LENGTH_SHORT)
                    .show()
            } else {
                createUserAccount(email, password)
            }
        }

        binding.btnSignInWithGoogle.setOnClickListener {
            val signIntent = googleSignInClient.signInIntent
            launcher.launch(signIntent)
        }



        binding.forgotPwdtv.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentLogin2_to_fragmentForgotPassword)

        }


        return binding.root
    }

    fun onBackPressed(): Boolean {
        activity?.finish()
        return true // hoặc false tùy vào xử lý của bạn
    }

    private fun createUserAccount(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(requireContext(), "Login Successfully", Toast.LENGTH_SHORT).show()
                val editor = sharedPreferences.edit()
                editor.putBoolean("logged_in", true)
                editor.apply()
                updateUi(user)
            } else {
                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount = task.result
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Successfully sign in with Google",
                                Toast.LENGTH_SHORT
                            ).show()

                            val editor = sharedPreferences.edit()
                            editor.putBoolean("logged_in", true)
                            editor.apply()

                            val email = account?.email
                            val user = User(email, "")
                            val userId = FirebaseAuth.getInstance()
                                .currentUser!!.uid
                            database.child("user").child(userId).setValue(user)
                            updateUi(authTask.result?.user)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Sign-in with Google failed",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Sign-in with Google failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

    private fun updateUi(user: FirebaseUser?) {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }


}