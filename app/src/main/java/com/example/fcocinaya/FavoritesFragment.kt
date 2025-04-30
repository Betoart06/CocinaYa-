package com.example.fcocinaya

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fcocinaya.model.RecipeModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyFavorites: TextView
    private var favoriteRecipes: MutableList<Recipe> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.recyclerViewFavorites)
        tvEmptyFavorites = view.findViewById(R.id.tvEmptyFavorites)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        // Cargar favoritos
        loadFavorites()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Recargar favoritos cada vez que el fragmento vuelve a ser visible
        loadFavorites()
    }

    private fun loadFavorites() {
        // Mostrar estado de carga
        showLoading()

        // Obtener ID del usuario
        val userId = RetrofitClient.getUserId()

        // Validar ID de usuario
        if (userId <= 0) {
            showError("No se pudieron cargar tus favoritos. Inicia sesión nuevamente.")
            return
        }

        Log.d("FavoritesFragment", "Cargando favoritos para usuario ID: $userId")

        try {
            // Llamar al endpoint para obtener favoritos
            RetrofitClient.getApiService().getUserFavorites(userId)
                .enqueue(object : Callback<List<RecipeModel>> {
                    override fun onResponse(
                        call: Call<List<RecipeModel>>,
                        response: Response<List<RecipeModel>>
                    ) {
                        if (response.isSuccessful) {
                            val recipeModels = response.body() ?: emptyList()
                            Log.d("FavoritesFragment", "Favoritos recibidos: ${recipeModels.size}")

                            // Convertir modelos a objetos Recipe
                            favoriteRecipes = recipeModels.map { Recipe.fromRecipeModel(it) }.toMutableList()

                            if (favoriteRecipes.isEmpty()) {
                                showEmpty()
                            } else {
                                showFavorites()
                            }
                        } else {
                            Log.e("FavoritesFragment", "Error al cargar favoritos: ${response.code()}")
                            showError("No se pudieron cargar tus favoritos")
                        }
                    }

                    override fun onFailure(call: Call<List<RecipeModel>>, t: Throwable) {
                        Log.e("FavoritesFragment", "Error de conexión: ${t.message}")
                        showError("Error de conexión. Revisa tu conexión a Internet.")
                    }
                })
        } catch (e: Exception) {
            Log.e("FavoritesFragment", "Excepción al cargar favoritos: ${e.message}")
            showError("Función de favoritos no disponible")
        }
    }

    private fun showLoading() {
        tvEmptyFavorites.text = "Cargando favoritos..."
        tvEmptyFavorites.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showEmpty() {
        tvEmptyFavorites.text = "No tienes recetas favoritas"
        tvEmptyFavorites.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showError(message: String) {
        tvEmptyFavorites.text = message
        tvEmptyFavorites.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun showFavorites() {
        tvEmptyFavorites.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        // Crear y asignar el adaptador
        val adapter = FavoritesAdapter(
            requireContext(),
            favoriteRecipes,
            onFavoriteRemoved = { size ->
                // Si no quedan favoritos, mostrar mensaje
                if (size == 0) {
                    showEmpty()
                }
            }
        )
        recyclerView.adapter = adapter
    }
}