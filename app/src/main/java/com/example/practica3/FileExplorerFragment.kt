package com.example.practica3

import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.practica3.databinding.FragmentFileExplorerBinding
import java.io.File
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.setFragmentResultListener
import androidx.constraintlayout.widget.ConstraintSet
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.appcompat.widget.SearchView

class FileExplorerFragment : Fragment() {
    private var searchView: SearchView? = null
    private lateinit var breadcrumbAdapter: BreadcrumbAdapter
    private var _binding: FragmentFileExplorerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FileViewModel by viewModels()
    private lateinit var adapter: FileAdapter
    private lateinit var settingsManager: SettingsManager

    // Mantenemos el launcher de permisos igual
    private val storagePermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadInitialFiles()
            } else {
                Toast.makeText(requireContext(), "El permiso es necesario para usar la app", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileExplorerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager = SettingsManager(requireContext())
        setupBreadcrumbs()
        setupRecyclerView()
        observeViewModel()
        setupBackButtonHandler()
        setupResultListener()
        setupMenu()

        // Si es la primera vez que entramos, iniciamos el proceso de carga
        if (viewModel.currentPath.value == null) {
            checkAndRequestPermissions()
        }
        binding.fabAddFolder.setOnClickListener {
            showInputFolderNameDialog()
        }
        binding.btnPaste.setOnClickListener {
            viewModel.pasteClipboard()
        }
        binding.btnCancelPaste.setOnClickListener {
            viewModel.clearClipboard()
        }
    }
    private fun setupBreadcrumbs() {
        breadcrumbAdapter = BreadcrumbAdapter { file ->
            // Cuando se hace clic en un breadcrumb, navegamos a esa carpeta
            viewModel.loadFiles(file.absolutePath)
        }
        // Configura el RecyclerView como horizontal
        binding.recyclerBreadcrumbs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerBreadcrumbs.adapter = breadcrumbAdapter
    }
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
                // Configura el SearchView
                val searchItem = menu.findItem(R.id.action_search)
                searchView = searchItem.actionView as? SearchView

                searchView?.queryHint = "Buscar archivos..."
                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    // Se llama cuando el usuario presiona Enter (o el botón de buscar)
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        query?.let { viewModel.searchFiles(it) }
                        searchView?.clearFocus() // Oculta el teclado
                        return true
                    }

                    // Se llama cada vez que el texto cambia
                    override fun onQueryTextChange(newText: String?): Boolean {
                        newText?.let { viewModel.searchFiles(it) }
                        return true
                    }
                })
                // Detecta cuándo se expande/contrae el SearchView
                searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                        viewModel.setSearchActive(true) // Activa el modo búsqueda
                        setOtherMenuItemsVisible(menu, item, false) // Oculta otros iconos
                        return true // Permite la expansión
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                        viewModel.setSearchActive(false) // Desactiva el modo búsqueda
                        //setOtherMenuItemsVisible(menu, item, true) // Muestra otros iconos
                        requireActivity().invalidateMenu()
                        // Limpia el texto de búsqueda al cerrar
                        searchView?.setQuery("", false)
                        return true // Permite el colapso
                    }
                })
                // Leemos el modo de vista actual para poner el icono correcto
                val viewModeItem = menu.findItem(R.id.action_toggle_view)
                lifecycleScope.launch {
                    val currentMode = settingsManager.viewModeFlow.first()
                    if (currentMode == SettingsManager.VIEW_MODE_GRID) {
                        viewModeItem.icon = resources.getDrawable(R.drawable.ic_view_list, null)
                    } else {
                        viewModeItem.icon = resources.getDrawable(R.drawable.ic_view_grid, null)
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_fileExplorerFragment_to_settingsFragment)
                        true
                    }
                    // AÑADE ESTE CASO
                    R.id.action_toggle_view -> {
                        toggleViewMode()
                        true
                    }
                    else -> false
                }
            }
            private fun setOtherMenuItemsVisible(menu: Menu, ignoreItem: MenuItem, visible: Boolean) {
                for (i in 0 until menu.size()) {
                    val item = menu.getItem(i)
                    if (item != ignoreItem) {
                        item.isVisible = visible
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    private fun toggleViewMode() {
        lifecycleScope.launch {
            // Leemos el modo actual, lo invertimos y lo guardamos
            val currentMode = settingsManager.viewModeFlow.first()
            val newMode = if (currentMode == SettingsManager.VIEW_MODE_LIST) {
                SettingsManager.VIEW_MODE_GRID
            } else {
                SettingsManager.VIEW_MODE_LIST
            }
            settingsManager.setViewMode(newMode)

            // Actualizamos la UI
            updateLayoutManager(newMode)
        }
    }
    private fun setupResultListener() {
        // Nos ponemos a escuchar los resultados del BottomSheet
        setFragmentResultListener(FileOptionsBottomSheet.REQUEST_KEY) { _, bundle ->
            // Cuando llega un resultado, extraemos los datos
            val action = bundle.getString(FileOptionsBottomSheet.ACTION_KEY)
            val path = bundle.getString(FileOptionsBottomSheet.PATH_KEY) ?: return@setFragmentResultListener

            // Decidimos qué hacer
            when (action) {
                FileOptionsBottomSheet.ACTION_RENAME -> {
                    showRenameFileDialog(path)
                }
                FileOptionsBottomSheet.ACTION_DELETE -> {
                    // Llamamos a la función que muestra la confirmación
                    showDeleteConfirmationDialog(path)
                }
                FileOptionsBottomSheet.ACTION_COPY -> {
                    viewModel.setClipboard(File(path), ClipboardOperation.COPY)
                }
                FileOptionsBottomSheet.ACTION_MOVE -> {
                    viewModel.setClipboard(File(path), ClipboardOperation.MOVE)
                }
                FileOptionsBottomSheet.ACTION_FAVORITE -> {
                    viewModel.toggleFavorite(File(path))
                    // Opcional: Mostrar un Toast confirmando la acción
                    // Puedes obtener si se añadió o quitó observando un Flow/LiveData
                    // que el ViewModel podría exponer basado en el resultado de isFavorite().
                }
            }
        }
    }

    // --- FUNCIÓN CORREGIDA Y SIMPLIFICADA ---
    private fun observeViewModel() {
        // Este observador SÓLO actualiza el texto de la ruta y la flecha de atrás
        viewModel.currentPath.observe(viewLifecycleOwner) { path ->
            val rootPath = Environment.getExternalStorageDirectory().absolutePath
            val isNotInRoot = path != null && path != rootPath
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(isNotInRoot)
        }

        // Este observador SÓLO actualiza la lista de archivos
        viewModel.files.observe(viewLifecycleOwner) { files ->
            binding.textEmpty.isVisible = files.isEmpty()
            adapter.submitList(files)
        }

        viewModel.breadcrumbParts.observe(viewLifecycleOwner) { parts ->
            if (parts != null) {
                breadcrumbAdapter.setPath(parts)
                // Hacemos scroll automático hasta el final de la lista de breadcrumbs
                binding.recyclerBreadcrumbs.scrollToPosition(parts.size - 1)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }
        viewModel.clipboard.observe(viewLifecycleOwner) { clipboardAction ->
            if (clipboardAction == null) {
                // No hay nada en el portapapeles, ocultamos la barra
                binding.pasteBar.visibility = View.GONE
                updateConstraints(isPasting = false)
            } else {
                // Hay algo, mostramos la barra y la configuramos
                val operationText = if (clipboardAction.operation == ClipboardOperation.COPY) "Copiando" else "Moviendo"
                binding.pasteFileName.text = "$operationText: ${clipboardAction.file.name}"
                binding.pasteBar.visibility = View.VISIBLE
                updateConstraints(isPasting = true)
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            // Si estamos en modo búsqueda, mostramos los resultados
            if (viewModel.isSearchActive.value == true) {
                adapter.submitList(results)
                binding.textEmpty.isVisible = results.isEmpty()
                binding.textEmpty.text = "No se encontraron resultados" // Cambia el texto
            }
        }

        viewModel.isSearchActive.observe(viewLifecycleOwner) { isActive ->
            // Si salimos del modo búsqueda, volvemos a mostrar la lista normal
            if (!isActive) {
                adapter.submitList(viewModel.files.value ?: emptyList())
                binding.textEmpty.isVisible = (viewModel.files.value ?: emptyList()).isEmpty()
                binding.textEmpty.text = "Carpeta Vacía" // Restaura el texto
            }
            // Oculta/muestra elementos cuando buscamos
            binding.fabAddFolder.isVisible = !isActive
            binding.recyclerBreadcrumbs.isVisible = !isActive
            binding.pasteBar.isVisible = !isActive && viewModel.clipboard.value != null // Oculta pegar al buscar
        }
        // --- FIN DEL OBSERVADOR AÑADIDO ---
    }
    // Esta función ajusta las restricciones del RecyclerView y el FAB
    // para que se coloquen encima de la barra de pegar cuando aparece.
    private fun updateConstraints(isPasting: Boolean) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root) // Clona el ConstraintLayout

        val pasteBarId = binding.pasteBar.id

        if (isPasting) {
            // Si pegamos: El RecyclerView y el FAB se anclan a la parte SUPERIOR de la barra
            constraintSet.connect(binding.recyclerViewFiles.id, ConstraintSet.BOTTOM, pasteBarId, ConstraintSet.TOP)
            constraintSet.connect(binding.fabAddFolder.id, ConstraintSet.BOTTOM, pasteBarId, ConstraintSet.TOP, 16)
        } else {
            // Si no pegamos: El RecyclerView y el FAB se anclan a la parte INFERIOR del padre
            constraintSet.connect(binding.recyclerViewFiles.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constraintSet.connect(binding.fabAddFolder.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 16)
        }

        // Aplica los cambios de restricciones
        constraintSet.applyTo(binding.root)
    }
    private fun setupBackButtonHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentPath = viewModel.currentPath.value ?: return
                val rootPath = Environment.getExternalStorageDirectory().absolutePath

                if (currentPath != rootPath) {
                    val parent = File(currentPath).parentFile
                    if (parent != null) {
                        viewModel.loadFiles(parent.absolutePath)
                    }
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar?.title = "Explorador de Archivos"
    }

    // --- EL RESTO DE FUNCIONES SE MANTIENEN IGUAL ---
    // 3. Modifica la creación del adaptador
    private fun setupRecyclerView() {
        // Creamos el adaptador
        adapter = FileAdapter(
            onFileClicked = { file -> onFileClicked(file) },
            onFileLongClicked = { file -> onFileLongClicked(file) }
        )
        binding.recyclerViewFiles.adapter = adapter

        // Leemos el modo de vista guardado y configuramos el layout
        lifecycleScope.launch {
            val currentMode = settingsManager.viewModeFlow.first()
            updateLayoutManager(currentMode)
        }
    }
    private fun updateLayoutManager(newMode: String) {
        // Actualizamos el adaptador
        adapter.setViewMode(newMode)

        // Cambiamos el LayoutManager
        if (newMode == SettingsManager.VIEW_MODE_GRID) {
            // Usamos 3 columnas para la cuadrícula
            //binding.recyclerViewFiles.layoutManager = GridLayoutManager(requireContext(), 3)
            // Leemos el número de columnas desde nuestros archivos de recursos
            val spanCount = requireContext().resources.getInteger(R.integer.grid_column_count)
            binding.recyclerViewFiles.layoutManager = GridLayoutManager(requireContext(), spanCount)
        } else {
            binding.recyclerViewFiles.layoutManager = LinearLayoutManager(requireContext())
        }

        // Invalidamos el menú para que el icono se actualice
        requireActivity().invalidateMenu()
    }

    // 4. Añade la función que maneja el toque largo
    private fun onFileLongClicked(file: File) {
        val bottomSheet = FileOptionsBottomSheet.newInstance(file.absolutePath)
        bottomSheet.show(parentFragmentManager, "FileOptions")
    }

    // 5. Añade los diálogos para obtener texto del usuario
    private fun showInputFolderNameDialog() {
        val context = requireContext()
        val editText = EditText(context)

        AlertDialog.Builder(context)
            .setTitle("Crear Carpeta")
            .setView(editText)
            .setPositiveButton("Crear") { dialog, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    viewModel.createFolder(name)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRenameFileDialog(filePath: String) {
        val context = requireContext()
        val file = File(filePath)
        val editText = EditText(context).apply { setText(file.name) }

        AlertDialog.Builder(context)
            .setTitle("Renombrar")
            .setView(editText)
            .setPositiveButton("Aceptar") { dialog, _ ->
                val newName = editText.text.toString()
                if (newName.isNotBlank()) {
                    viewModel.renameFile(filePath, newName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(filePath: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar '${File(filePath).name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteFile(filePath)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    // --- FIN DE LAS FUNCIONES AÑADIDAS ---

    private fun checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                storagePermissionLauncher.launch(intent)
            } else {
                loadInitialFiles()
            }
        } else {
            loadInitialFiles()
        }
    }

    private fun loadInitialFiles() {
        val initialPath = Environment.getExternalStorageDirectory().absolutePath
        viewModel.loadFiles(initialPath)
    }

    private fun onFileClicked(file: File) {
        if (file.isDirectory) {
            viewModel.loadFiles(file.absolutePath)
            return
        }
        val textExtensions = listOf("txt", "md", "log", "json", "xml", "html", "js", "css")
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        // Registramos el archivo abierto en el historial
        viewModel.addRecentFile(file)
        when (file.extension.lowercase()) {
            in textExtensions -> {
                val action = FileExplorerFragmentDirections.actionFileExplorerFragmentToTextVisualizerFragment(file.absolutePath)
                findNavController().navigate(action)
            }
            in imageExtensions -> {
                val action = FileExplorerFragmentDirections.actionFileExplorerFragmentToImageVisualizerFragment(file.absolutePath)
                findNavController().navigate(action)
            }
            else -> {
                openFileWithIntent(file)
            }
        }
    }

    private fun openFileWithIntent(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        val mime = requireContext().contentResolver.getType(uri)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mime)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No hay ninguna aplicación para abrir este archivo.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}