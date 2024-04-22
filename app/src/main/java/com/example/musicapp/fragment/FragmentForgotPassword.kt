package com.example.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentForgotPasswordBinding

class FragmentForgotPassword : Fragment() {
    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        fragmentManager = requireActivity().supportFragmentManager

        binding.tvSignIn.setOnClickListener {
            fragmentManager.popBackStack()
        }

        binding.btnSend.setOnClickListener {
            fragmentManager.popBackStack()
        }

        return binding.root
    }

    fun onBackPressed(): Boolean {
        fragmentManager.popBackStack()
        return true // hoặc false tùy vào xử lý của bạn
    }


}