package com.example.fcocinaya

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.fcocinaya.model.RecipeModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityActivity : AppCompatActivity() {

    private lateinit var recyclerViewCommunity: RecyclerView
    private lateinit var communityAdapter: CommunityAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: Button
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        // Verificar si el usuario está logueado
        if (!RetrofitClient.isLoggedIn()) {
            // Redirigir a la pantalla de login
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Obtener el ID del usuario
        userId = RetrofitClient.getUserId()

        // Inicializar vistas
        recyclerViewCommunity = findViewById(R.id.recyclerViewCommunity)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)

        // Configurar RecyclerView
        recyclerViewCommunity.layoutManager = LinearLayoutManager(this)

        // Configurar adaptador
        communityAdapter = CommunityAdapter(
            emptyList(),
            onRecipeClick = { recipe ->
                // Abrir detalle de la receta
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                intent.putExtra("recipe_name", recipe.name)
                startActivity(intent)
            },
            onLikeClick = { recipe ->
                toggleLike(recipe)
            },
            onCommentClick = { recipe ->
                // Abrir pantalla de comentarios
                val intent = Intent(this, CommentsActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                intent.putExtra("recipe_name", recipe.name)
                startActivity(intent)
            },
            onFavoriteClick = { recipe ->
                toggleFavorite(recipe)
            }
        )

        recyclerViewCommunity.adapter = communityAdapter

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadCommunityRecipes()
        }

        // Configurar botón de volver
        btnBack.setOnClickListener {
            finish()
        }

        // Cargar recetas
        loadCommunityRecipes()
    }

    private fun loadCommunityRecipes() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        // TODO: Reemplazar con llamada a API real cuando esté disponible
        // Simulamos una carga de datos
        RetrofitClient.getApiService().getCommunityRecipes().enqueue(object : Callback<List<RecipeModel>> {
            override fun onResponse(call: Call<List<RecipeModel>>, response: Response<List<RecipeModel>>) {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val recipes = response.body()?.map { Recipe.fromRecipeModel(it) } ?: emptyList()

                    if (recipes.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                        tvEmptyState.text = "No hay recetas publicadas en la comunidad"
                    } else {
                        communityAdapter.updateRecipes(recipes)
                    }
                } else {
                    // Mostrar mensaje de error y cargar datos de ejemplo
                    Toast.makeText(this@CommunityActivity, "Error al cargar recetas de la comunidad", Toast.LENGTH_SHORT).show()
                    loadSampleCommunityRecipes()
                }
            }

            override fun onFailure(call: Call<List<RecipeModel>>, t: Throwable) {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                Log.e("CommunityActivity", "Error al cargar recetas", t)
                Toast.makeText(this@CommunityActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()

                // Cargar datos de ejemplo
                loadSampleCommunityRecipes()
            }
        })
    }

    private fun loadSampleCommunityRecipes() {
        // Datos de ejemplo para mostrar cuando falla la API
        val sampleRecipes = listOf(
            Recipe(
                id = 101,
                name = "Tacos de Carnitas",
                ingredients = "Carne de cerdo, cebolla, cilantro, tortillas, limón",
                steps = "1. Cocinar la carne...\n2. Preparar las tortillas...",
                userId = 5,
                userName = "María López",
                isPublished = true
            ),
            Recipe(
                id = 102,
                name = "Tiramisu Casero",
                ingredients = "Queso mascarpone, café, bizcochos, cacao en polvo",
                steps = "1. Preparar el café...\n2. Montar el queso...",
                userId = 8,
                userName = "Carlos Rodríguez",
                isPublished = true
            ),
            Recipe(
                id = 103,
                name = "Ensalada Mediterránea",
                ingredients = "Lechuga, tomate, pepino, aceitunas, queso feta, aceite de oliva",
                steps = "1. Lavar las verduras...\n2. Cortar en trozos...\n3. Mezclar y aliñar...",
                userId = 3,
                userName = "Ana García",
                isPublished = true
            )
        )

        communityAdapter.updateRecipes(sampleRecipes)
        tvEmptyState.visibility = View.GONE
    }

    private fun toggleLike(recipe: Recipe) {
        // TODO: Implementar llamada a API para dar/quitar like
        Toast.makeText(this, "Has dado like a: ${recipe.name}", Toast.LENGTH_SHORT).show()
    }

    private fun toggleFavorite(recipe: Recipe) {
        // TODO: Implementar llamada a API para agregar/quitar de favoritos
        Toast.makeText(this, "Has agregado a favoritos: ${recipe.name}", Toast.LENGTH_SHORT).show()
    }
}