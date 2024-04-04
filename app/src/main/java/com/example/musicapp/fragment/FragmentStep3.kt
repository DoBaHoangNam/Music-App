package com.example.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityLoginSigninBinding
import com.example.musicapp.databinding.FragmentStep3Binding
import com.example.musicapp.ui.ActivityLoginSignin

class FragmentStep3 : Fragment() {
    private lateinit var binding: FragmentStep3Binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStep3Binding.inflate(inflater, container,false)
        binding.getStartedBtn.setOnClickListener {
            val intent = Intent(requireContext(), ActivityLoginSignin::class.java)
            startActivity(intent)
            activity?.finish()
        }
        return binding.root
    }


}