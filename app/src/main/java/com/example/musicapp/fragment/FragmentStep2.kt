package com.example.musicapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentStep2Binding

class FragmentStep2 : Fragment() {
    private lateinit var binding: FragmentStep2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStep2Binding.inflate(inflater, container, false)
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentStep2_to_fragmentStep3)
        }
        binding.btnSkip.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentStep2_to_fragmentStep3)
        }
        // Inflate the layout for this fragment
        return binding.root
    }

}