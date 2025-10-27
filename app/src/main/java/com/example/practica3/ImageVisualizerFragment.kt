package com.example.practica3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.practica3.databinding.FragmentImageVisualizerBinding
import androidx.navigation.ui.NavigationUI
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.github.chrisbanes.photoview.PhotoView
import java.io.File

class ImageVisualizerFragment : Fragment() {

    private var _binding: FragmentImageVisualizerBinding? = null
    private val binding get() = _binding!!

    // Usamos navArgs para recibir la ruta del archivo
    private val args: ImageVisualizerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageVisualizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Se elimina todo el código de setSupportActionBar y supportActionBar
        // AHORA (Correcto):
        Glide.with(this)
            .load(File(args.filePath))
            .into(binding.photoView)
    }

    // Añadimos esta función para actualizar el título cuando el fragmento es visible
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = File(args.filePath).name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}