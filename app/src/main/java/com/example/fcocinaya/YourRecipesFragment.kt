package com.example.fcocinaya

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fcocinaya.model.RecipeModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YourRecipesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyYourRecipes: TextView
    private lateinit var btnAddRecipe: Button
    private lateinit var btnRecetasPublicadas: Button

    private var yourRecipes: MutableList<Recipe> = mutableListOf()
    private var showOnlyPublished = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.fragment_your_recipes, container, false)

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.recyclerViewYourRecipes)
        tvEmptyYourRecipes = view.findViewById(R.id.tvEmptyYourRecipes)
        btnAddRecipe = view.findViewById(R.id.btnAddRecipe)
        btnRecetasPublicadas = view.findViewById(R.id.btnRecetasPublicadas)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Configurar listeners de botones
        btnAddRecipe.setOnClickListener {
            val intent = Intent(requireContext(), AddRecipeActivity::class.java)
            startActivity(intent)
        }

        btnRecetasPublicadas.setOnClickListener {
            showOnlyPublished = !showOnlyPublished
            btnRecetasPublicadas.text = if (showOnlyPublished) "Todas las recetas" else "Solo publicadas"
            filterRecipes()
        }

        // Cargar recetas
        loadUserRecipes()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Recargar recetas cada vez que el fragmento vuelve a ser visible
        loadUserRecipes()
    }

    private fun loadUserRecipes() {
        // Mostrar estado de carga
        showLoading()

        // Obtener ID del usuario
        val userId = RetrofitClient.getUserId()

        // Validar ID de usuario
        if (userId <= 0) {
            showError("No se pudieron cargar tus recetas. Inicia sesión nuevamente.")
            return
        }

        Log.d("YourRecipesFragment", "Cargando recetas para usuario ID: $userId")

        try {
            // Llamar al endpoint para obtener recetas del usuario
            RetrofitClient.getApiService().getUserRecipes(userId)
                .enqueue(object : Callback<List<RecipeModel>> {
                    override fun onResponse(
                        call: Call<List<RecipeModel>>,
                        response: Response<List<RecipeModel>>
                    ) {
                        if (response.isSuccessful) {
                            val recipeModels = response.body() ?: emptyList()
                            Log.d("YourRecipesFragment", "Recetas recibidas: ${recipeModels.size}")

                            // Convertir modelos a objetos Recipe
                            yourRecipes = recipeModels.map { Recipe.fromRecipeModel(it) }.toMutableList()

                            // Filtrar según el estado actual
                            filterRecipes()
                        } else {
                            Log.e("YourRecipesFragment", "Error al cargar recetas: ${response.code()}")
                            showError("No se pudieron cargar tus recetas")
                        }
                    }

                    override fun onFailure(call: Call<List<RecipeModel>>, t: Throwable) {
                        Log.e("YourRecipesFragment", "Error de conexión: ${t.message}")
                        showError("Error de conexión. Revisa tu conexión a Internet.")
                    }
                })
        } catch (e: Exception) {
            Log.e("YourRecipesFragment", "Excepción al cargar recetas: ${e.message}")
            showError("Error al cargar tus recetas")
        }
    }

    private fun filterRecipes() {
        val filteredRecipes = if (showOnlyPublished) {
            yourRecipes.filter { it.isPublished }
        } else {
            yourRecipes
        }.toMutableList()

        if (filteredRecipes.isEmpty()) {
            val message = if (showOnlyPublished) {
                "No tienes recetas publicadas"
            } else {
                "No has creado recetas todavía"
            }
            showEmpty(message)
        } else {
            showRecipes(filteredRecipes)
        }
    }

    private fun showLoading() {
        tvEmptyYourRecipes.text = "Cargando tus recetas..."
        tvEmptyYourRecipes.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showEmpty(message: String) {
        tvEmptyYourRecipes.text = message
        tvEmptyYourRecipes.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showError(message: String) {
        tvEmptyYourRecipes.text = message
        tvEmptyYourRecipes.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showRecipes(recipes: MutableList<Recipe>) {
        tvEmptyYourRecipes.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        // Crear y asignar el adaptador
        val adapter = YourRecipesAdapter(
            requireContext(),
            recipes,
            onRecipeDeleted = { size ->
                // Si no quedan recetas después de filtrar, mostrar mensaje
                if (size == 0) {
                    val message = if (showOnlyPublished) {
                        "No tienes recetas publicadas"
                    } else {
                        "No has creado recetas todavía"
                    }
                    showEmpty(message)
                }

                // Actualizar la lista principal
                loadUserRecipes()
            },
            onRecipePublished = { updatedRecipe ->
                // Actualizar la receta en la lista principal
                val index = yourRecipes.indexOfFirst { it.id == updatedRecipe.id }
                if (index != -1) {
                    yourRecipes[index] = updatedRecipe
                }

                // Si cambiamos a mostrar solo publicadas y despublicamos la última, mostrar mensaje
                if (showOnlyPublished) {
                    filterRecipes()
                }
            }
        )
        recyclerView.adapter = adapter
    }
}