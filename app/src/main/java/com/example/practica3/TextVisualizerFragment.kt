package com.example.practica3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.practica3.databinding.FragmentTextVisualizerBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import java.io.File

class TextVisualizerFragment : Fragment() {

    private var _binding: FragmentTextVisualizerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TextVisualizerViewModel by viewModels()
    private val args: TextVisualizerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextVisualizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Se elimina todo el código de setSupportActionBar y supportActionBar
        viewModel.fileContent.observe(viewLifecycleOwner) { content ->
            binding.textContent.text = content
        }
        viewModel.loadFileContent(args.filePath)
    }

    // Añadimos esta función para actualizar el título
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = File(args.filePath).name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}