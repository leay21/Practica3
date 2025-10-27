package com.example.practica3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.practica3.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsManager = SettingsManager(requireContext())

        // Leer el tema actual y marcar el RadioButton correcto
        lifecycleScope.launch {
            val currentTheme = settingsManager.themeFlow.first()
            if (currentTheme == SettingsManager.THEME_AZUL) {
                binding.radioAzul.isChecked = true
            } else {
                binding.radioGuinda.isChecked = true
            }
        }

        // Guardar el tema cuando el usuario cambie la selección
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            lifecycleScope.launch {
                val (newTheme, oldTheme) = if (checkedId == R.id.radio_azul) {
                    Pair(SettingsManager.THEME_AZUL, SettingsManager.THEME_GUINDA)
                } else {
                    Pair(SettingsManager.THEME_GUINDA, SettingsManager.THEME_AZUL)
                }

                // Solo guardamos y reiniciamos si el tema realmente cambió
                val currentTheme = settingsManager.themeFlow.first()
                if (newTheme != currentTheme) {
                    settingsManager.setTheme(newTheme)
                    // Reiniciamos la actividad para que el nuevo tema se aplique
                    requireActivity().recreate()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = "Configuración"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}