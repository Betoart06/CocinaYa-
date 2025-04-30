package com.example.fcocinaya

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecipesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.fragment_recipes, container, false)

        // Configurar el RecyclerView con GridLayoutManager (2 columnas)
        recyclerView = view.findViewById(R.id.recyclerViewRecipes)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 2 columnas

        // Crear y asignar el adaptador
        recipeAdapter = RecipeAdapter(requireContext(), getSampleRecipes())
        recyclerView.adapter = recipeAdapter

        return view
    }

    // Método para obtener una lista de recetas de ejemplo
    private fun getSampleRecipes(): List<Recipe> {
        return listOf(
            Recipe("Tostadas de frijoles negros y aguacate", R.drawable.comida_fondo),
            Recipe("Huevos en Purgatorio", R.drawable.comida_fondo),
            Recipe("Ensalada César", R.drawable.comida_fondo),
            Recipe("Pasta Alfredo", R.drawable.comida_fondo),
            Recipe("Sopa de tomate", R.drawable.comida_fondo)
        )
    }
}