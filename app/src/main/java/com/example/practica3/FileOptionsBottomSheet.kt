package com.example.practica3 // Asegúrate de que sea tu paquete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf // <-- Importante
import androidx.fragment.app.setFragmentResult // <-- Importante
import com.example.practica3.databinding.FragmentFileOptionsBinding // Asegúrate de que sea tu paquete
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// 1. Ya NO necesitamos la interfaz "FileOptionsListener", la hemos borrado.

class FileOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentFileOptionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var filePath: String
    // 2. Ya NO necesitamos la variable "listener"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFileOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filePath = requireArguments().getString(ARG_FILE_PATH) ?: ""

        // 3. Cambiamos la lógica de los clics
        binding.optionRename.setOnClickListener {
            // Publicamos un resultado con la acción "renombrar" y la ruta
            setFragmentResult(REQUEST_KEY, bundleOf(
                ACTION_KEY to ACTION_RENAME,
                PATH_KEY to filePath
            ))
            dismiss() // Cerramos el menú
        }

        binding.optionDelete.setOnClickListener {
            // Publicamos un resultado con la acción "eliminar" y la ruta
            setFragmentResult(REQUEST_KEY, bundleOf(
                ACTION_KEY to ACTION_DELETE,
                PATH_KEY to filePath
            ))
            dismiss() // Cerramos el menú
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // 4. Creamos claves para enviar y recibir los resultados
        const val REQUEST_KEY = "fileOptionsRequest"
        const val ACTION_KEY = "action"
        const val PATH_KEY = "path"
        const val ACTION_RENAME = "rename"
        const val ACTION_DELETE = "delete"

        private const val ARG_FILE_PATH = "file_path"

        // La función newInstance se queda exactamente igual
        fun newInstance(filePath: String): FileOptionsBottomSheet {
            val fragment = FileOptionsBottomSheet()
            val args = Bundle()
            args.putString(ARG_FILE_PATH, filePath)
            fragment.arguments = args
            return fragment
        }
    }
}