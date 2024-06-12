package com.example.musicapp.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentSignUpBinding
import com.example.musicapp.model.User
import com.example.musicapp.ui.ActivityLoginSignin
import com.example.musicapp.ui.ActivitySplashScreen
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


class FragmentSignUp : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var email :String
    private lateinit var password: String
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
        binding = FragmentSignUpBinding.inflate(inflater,container,false)
        auth = Firebase.auth
        database = Firebase.database.reference
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        binding.btnSignUp.setOnClickListener {
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()
            val checkbox = binding.confirmCheckbox.isChecked
            
            if(email.isBlank() || password.isBlank() || checkbox==false){
                Toast.makeText(requireContext(),"Please fill all details", Toast.LENGTH_SHORT).show()
                
            }else{
                createAccount(email,password)
            }

        }

        binding.btnSignUpWithGoogle.setOnClickListener {
            val signIntent = googleSignInClient.signInIntent
            launcher.launch(signIntent)
        }

        binding.tvSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSignUp_to_fragmentLogin2)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSignUp_to_fragmentLogin2)
        }
        return binding.root
    }



    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(requireContext(),"Account created successfully", Toast.LENGTH_SHORT).show()
                saveUserData()
                val intent = Intent(requireContext(), ActivitySplashScreen::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(requireContext(), "Account Creation Failed", Toast.LENGTH_SHORT).show()
                Log.d("Account","createAccount: Failure",task.exception)
            }

        }
    }

    private fun saveUserData() {
        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()
        val user = User(email,email, password)
        val userId = FirebaseAuth.getInstance()
            .currentUser!!.uid
        database.child("user").child(userId).setValue(user)
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
                                "Successfully sign up with Google",
                                Toast.LENGTH_SHORT
                            ).show()
                            val email = account?.email
                            val user = User(email,email, "")
                            val userId = FirebaseAuth.getInstance()
                                .currentUser!!.uid
                            database.child("user").child(userId).setValue(user)
                            updateUi(authTask.result?.user)
                        } else {
                            Toast.makeText(requireContext(), "Sign-up with Google failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Sign-up with Google failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun updateUi(user: FirebaseUser?) {

        val intent = Intent(requireContext(), ActivitySplashScreen::class.java)
        startActivity(intent)
        activity?.finish()

    }

}