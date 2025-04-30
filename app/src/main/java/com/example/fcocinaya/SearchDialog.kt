package com.example.fcocinaya

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Diálogo de búsqueda personalizado para la aplicación de recetas
 */
class SearchDialog(
    context: Context,
    private val onSearch: (String) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Quitar el título de la ventana de diálogo
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Establecer el layout del diálogo
        setContentView(R.layout.dialog_search)

        // Referencias a las vistas
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val recentSearchesContainer = findViewById<LinearLayout>(R.id.recentSearchesContainer)

        // Mostrar búsquedas recientes (opcional)
        val recentSearches = getRecentSearches(context)
        if (recentSearches.isNotEmpty()) {
            for (search in recentSearches) {
                val recentSearchView = TextView(context)
                recentSearchView.text = search
                recentSearchView.setPadding(16, 16, 16, 16)
                recentSearchView.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.blanco))

                // Al hacer clic en una búsqueda reciente, se establece como búsqueda actual
                recentSearchView.setOnClickListener {
                    etSearch.setText(search)
                    etSearch.setSelection(search.length)
                }

                recentSearchesContainer.addView(recentSearchView)
            }
        } else {
            // Si no hay búsquedas recientes, ocultar la sección
            findViewById<TextView>(R.id.tvRecentSearches).visibility = View.GONE
            recentSearchesContainer.visibility = View.GONE
        }

        // Configurar el botón de búsqueda
        btnSearch.setOnClickListener {
            val searchText = etSearch.text.toString().trim()
            if (searchText.isNotEmpty()) {
                // Guardar la búsqueda en el historial
                saveSearch(context, searchText)

                // Llamar al callback con el texto de búsqueda
                onSearch(searchText)

                // Cerrar el diálogo
                dismiss()
            }
        }

        // Configurar el botón de cancelar
        btnCancel.setOnClickListener {
            dismiss()
        }

        // Activar/desactivar el botón de búsqueda según haya texto
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnSearch.isEnabled = !s.isNullOrBlank()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Estado inicial del botón de búsqueda
        btnSearch.isEnabled = false
    }

    /**
     * Obtiene las búsquedas recientes guardadas en preferencias
     */
    private fun getRecentSearches(context: Context): List<String> {
        val prefs = context.getSharedPreferences("SearchPrefs", Context.MODE_PRIVATE)
        val searchesString = prefs.getString("recent_searches", "") ?: ""
        return if (searchesString.isEmpty()) {
            emptyList()
        } else {
            searchesString.split("|||").filter { it.isNotEmpty() }.takeLast(5)
        }
    }

    /**
     * Guarda una búsqueda en el historial de búsquedas recientes
     */
    private fun saveSearch(context: Context, search: String) {
        val prefs = context.getSharedPreferences("SearchPrefs", Context.MODE_PRIVATE)
        val currentSearches = getRecentSearches(context).toMutableList()

        // Eliminar la búsqueda si ya existe para evitar duplicados
        currentSearches.remove(search)

        // Añadir la nueva búsqueda al inicio
        currentSearches.add(0, search)

        // Limitar a las 5 más recientes
        val limitedSearches = currentSearches.take(5)

        // Guardar en preferencias
        prefs.edit().putString("recent_searches", limitedSearches.joinToString("|||")).apply()
    }
}