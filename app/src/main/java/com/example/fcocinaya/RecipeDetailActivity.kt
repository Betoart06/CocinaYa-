package com.example.fcocinaya

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fcocinaya.model.RecipeModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var btnClose: Button
    private lateinit var btnFavorite: Button
    private lateinit var ivRecipeImage: ImageView
    private lateinit var tvRecipeTitle: TextView
    private lateinit var btnReceta: Button
    private lateinit var btnIngredientes: Button
    private lateinit var svRecetaContainer: View
    private lateinit var svIngredientesContainer: View
    private lateinit var stepsContainer: LinearLayout
    private lateinit var ingredientsContainer: LinearLayout

    private var recipeId: Int = -1
    private var currentRecipe: RecipeModel? = null
    private var isStaticRecipe: Boolean = false
    private var isFavorite: Boolean = false

    // Mapa para almacenar imágenes para recetas estáticas
    private val staticRecipeImages = mapOf(
        101 to R.drawable.comida_fondo,
        102 to R.drawable.comida_fondo,
        103 to R.drawable.comida_fondo,
        104 to R.drawable.comida_fondo,
        105 to R.drawable.comida_fondo
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Inicializar vistas
        initializeViews()

        // Obtener datos de la receta del intent
        getRecipeDataFromIntent()

        // Configurar listeners de botones
        setupButtonListeners()

        // Por defecto, mostrar la vista de receta
        showRecetaView()

        // Cargar detalles de la receta
        if (isStaticRecipe) {
            loadStaticRecipeDetails()
        } else {
            loadRecipeDetailsFromApi()
        }
    }

    private fun initializeViews() {
        btnClose = findViewById(R.id.btnClose)
        btnFavorite = findViewById(R.id.btnFavorite)
        ivRecipeImage = findViewById(R.id.ivRecipeImage)
        tvRecipeTitle = findViewById(R.id.tvRecipeTitle)
        btnReceta = findViewById(R.id.btnReceta)
        btnIngredientes = findViewById(R.id.btnIngredientes)
        svRecetaContainer = findViewById(R.id.svRecetaContainer)
        svIngredientesContainer = findViewById(R.id.svIngredientesContainer)
        stepsContainer = findViewById(R.id.stepsContainer)
        ingredientsContainer = findViewById(R.id.ingredientsContainer)
    }

    private fun getRecipeDataFromIntent() {
        recipeId = intent.getIntExtra("recipe_id", -1)
        val recipeName = intent.getStringExtra("recipe_name") ?: "Cargando receta..."
        isStaticRecipe = intent.getBooleanExtra("is_static_recipe", false)

        Log.d("RecipeDetailActivity", "Abriendo receta ID: $recipeId, Nombre: $recipeName, Estática: $isStaticRecipe")
        tvRecipeTitle.text = recipeName
    }

    private fun setupButtonListeners() {
        btnClose.setOnClickListener {
            finish() // Cerrar la actividad
        }

        btnFavorite.setOnClickListener {
            // Alternar estado de favorito
            toggleFavorite()
        }

        btnReceta.setOnClickListener {
            showRecetaView()
        }

        btnIngredientes.setOnClickListener {
            showIngredientesView()
        }
    }

    private fun loadStaticRecipeDetails() {
        val ingredients = intent.getStringExtra("recipe_ingredients") ?: ""
        val steps = intent.getStringExtra("recipe_steps") ?: ""

        // Cargar imagen para receta estática según su ID
        setStaticRecipeImage(recipeId)

        // Actualizar contenido
        updateStepsLayout(steps)
        updateIngredientsLayout(ingredients)

        // Verificar si esta receta estática ya está en favoritos
        checkIfFavorite()
    }

    private fun loadRecipeDetailsFromApi() {
        if (recipeId == -1) {
            Toast.makeText(this, "Error: No se pudo cargar la receta", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar estado de carga
        tvRecipeTitle.text = "Cargando receta..."
        stepsContainer.removeAllViews()
        ingredientsContainer.removeAllViews()

        Log.d("RecipeDetailActivity", "Cargando detalles de receta con ID: $recipeId")

        RetrofitClient.getApiService().getRecipeById(recipeId).enqueue(object : Callback<RecipeModel> {
            override fun onResponse(call: Call<RecipeModel>, response: Response<RecipeModel>) {
                Log.d("RecipeDetailActivity", "Respuesta recibida: ${response.code()}")

                if (response.isSuccessful) {
                    currentRecipe = response.body()
                    Log.d("RecipeDetailActivity", "Receta cargada: ${currentRecipe?.titulo}")

                    currentRecipe?.let { recipe ->
                        // Actualizar título
                        tvRecipeTitle.text = recipe.titulo

                        // Actualizar imagen
                        updateRecipeImage(recipe.imagen)

                        // Actualizar contenido
                        updateStepsLayout(recipe.pasos)
                        updateIngredientsLayout(recipe.ingredientes)

                        // Verificar si esta receta está en favoritos
                        checkIfFavorite()
                    }
                } else {
                    handleApiError(response)
                }
            }

            override fun onFailure(call: Call<RecipeModel>, t: Throwable) {
                Log.e("RecipeDetailActivity", "Error loading recipe", t)
                Toast.makeText(this@RecipeDetailActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()

                // Mostrar mensaje de error
                tvRecipeTitle.text = "Error de conexión"
                ivRecipeImage.setImageResource(R.drawable.comida_fondo)
            }
        })
    }

    private fun handleApiError(response: Response<RecipeModel>) {
        try {
            val errorBody = response.errorBody()?.string()
            Log.e("RecipeDetailActivity", "Error al cargar la receta: ${response.code()}, $errorBody")
            Toast.makeText(this, "Error al cargar la receta: ${response.code()}", Toast.LENGTH_SHORT).show()

            // Mostrar mensaje de error
            tvRecipeTitle.text = "Error al cargar la receta"
            ivRecipeImage.setImageResource(R.drawable.comida_fondo)
        } catch (e: Exception) {
            Log.e("RecipeDetailActivity", "Error al procesar respuesta de error", e)
        }
    }

    private fun setStaticRecipeImage(recipeId: Int) {
        val imageResId = staticRecipeImages[recipeId] ?: R.drawable.comida_fondo
        ivRecipeImage.setImageResource(imageResId)
    }

    private fun updateRecipeImage(imageBase64: String) {
        if (imageBase64.isNotEmpty()) {
            try {
                // Eliminar prefijo "data:image/jpeg;base64," si existe
                var imageString = imageBase64
                if (imageString.contains(",")) {
                    imageString = imageString.split(",")[1]
                }

                val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (decodedImage != null) {
                    ivRecipeImage.setImageBitmap(decodedImage)
                } else {
                    ivRecipeImage.setImageResource(R.drawable.comida_fondo)
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailActivity", "Error decoding image", e)
                ivRecipeImage.setImageResource(R.drawable.comida_fondo)
            }
        } else {
            ivRecipeImage.setImageResource(R.drawable.comida_fondo)
        }
    }

    private fun updateStepsLayout(steps: String) {
        stepsContainer.removeAllViews()
        val stepsList = steps.split("\n").filter { it.isNotEmpty() }

        Log.d("RecipeDetailActivity", "Actualizando pasos: ${stepsList.size} pasos encontrados")

        for (i in stepsList.indices) {
            val stepView = layoutInflater.inflate(R.layout.item_step, stepsContainer, false)
            val stepNumber = stepView.findViewById<TextView>(R.id.tvStepNumber)
            val stepContent = stepView.findViewById<TextView>(R.id.tvStepContent)

            stepNumber.text = (i + 1).toString()
            stepContent.text = stepsList[i].trim()

            // Aplicar margen inferior
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (i < stepsList.size - 1) {
                layoutParams.bottomMargin = 16
            }
            stepView.layoutParams = layoutParams

            stepsContainer.addView(stepView)
        }
    }

    private fun updateIngredientsLayout(ingredients: String) {
        ingredientsContainer.removeAllViews()
        val ingredientsList = ingredients.split("\n").filter { it.isNotEmpty() }

        Log.d("RecipeDetailActivity", "Actualizando ingredientes: ${ingredientsList.size} ingredientes encontrados")

        for (i in ingredientsList.indices) {
            val tvIngredient = TextView(this)

            // Asegurar que cada ingrediente comience con un guion
            val ingredientText = if (ingredientsList[i].trim().startsWith("-")) {
                ingredientsList[i].trim()
            } else {
                "- ${ingredientsList[i].trim()}"
            }

            tvIngredient.text = ingredientText
            tvIngredient.setTextColor(resources.getColor(R.color.blanco))

            // Aplicar margen inferior
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (i < ingredientsList.size - 1) {
                layoutParams.bottomMargin = 8
            }
            tvIngredient.layoutParams = layoutParams

            ingredientsContainer.addView(tvIngredient)
        }
    }

    private fun checkIfFavorite() {
        val userId = RetrofitClient.getUserId()

        if (userId <= 0 || recipeId <= 0) {
            // No podemos verificar favoritos si no hay usuario o receta válidos
            btnFavorite.alpha = 0.5f
            isFavorite = false
            return
        }

        Log.d("RecipeDetailActivity", "Verificando si es favorito para usuario: $userId, receta: $recipeId")

        try {
            RetrofitClient.getApiService().checkFavorite(recipeId, userId).enqueue(object : Callback<FavoriteResponse> {
                override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                    if (response.isSuccessful) {
                        val isFav = response.body()?.status ?: false
                        isFavorite = isFav
                        updateFavoriteButtonAppearance()
                        Log.d("RecipeDetailActivity", "Estado de favorito: $isFav")
                    } else {
                        Log.e("RecipeDetailActivity", "Error al verificar favorito: ${response.code()}")
                        btnFavorite.alpha = 0.5f
                        isFavorite = false
                    }
                }

                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    Log.e("RecipeDetailActivity", "Error al verificar favorito", t)
                    btnFavorite.alpha = 0.5f
                    isFavorite = false
                }
            })
        } catch (e: Exception) {
            Log.e("RecipeDetailActivity", "El endpoint checkFavorite puede no estar implementado", e)
            btnFavorite.alpha = 0.5f
            isFavorite = false
        }
    }

    private fun updateFavoriteButtonAppearance() {
        btnFavorite.alpha = if (isFavorite) 1.0f else 0.5f
    }

    private fun toggleFavorite() {
        val userId = RetrofitClient.getUserId()

        if (userId <= 0) {
            Toast.makeText(this, "Debes iniciar sesión para gestionar favoritos", Toast.LENGTH_SHORT).show()
            return
        }

        if (recipeId <= 0) {
            Toast.makeText(this, "No se puede gestionar esta receta como favorito", Toast.LENGTH_SHORT).show()
            return
        }

        // Cambiar el estado de favorito
        val newFavoriteState = !isFavorite

        // Actualizar UI optimistamente
        isFavorite = newFavoriteState
        updateFavoriteButtonAppearance()

        Log.d("RecipeDetailActivity", "Cambiando estado de favorito a: $newFavoriteState")

        try {
            if (newFavoriteState) {
                // Añadir a favoritos
                addToFavorites(userId)
            } else {
                // Quitar de favoritos
                removeFromFavorites(userId)
            }
        } catch (e: Exception) {
            Log.e("RecipeDetailActivity", "Los endpoints de favoritos pueden no estar implementados", e)
            Toast.makeText(this, "Función de favoritos no disponible", Toast.LENGTH_SHORT).show()

            // Revertir cambio en UI
            isFavorite = !newFavoriteState
            updateFavoriteButtonAppearance()
        }
    }

    private fun addToFavorites(userId: Int) {
        val favoriteRequest = FavoriteRequest(userId = userId, recipeId = recipeId)

        RetrofitClient.getApiService().addToFavorites(favoriteRequest).enqueue(object : Callback<FavoriteResponse> {
            override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RecipeDetailActivity, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
                    Log.d("RecipeDetailActivity", "Añadido a favoritos correctamente")
                } else {
                    // Revertir cambio en UI
                    isFavorite = false
                    updateFavoriteButtonAppearance()
                    Toast.makeText(this@RecipeDetailActivity, "Error al añadir a favoritos", Toast.LENGTH_SHORT).show()
                    Log.e("RecipeDetailActivity", "Error al añadir a favoritos: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                // Revertir cambio en UI
                isFavorite = false
                updateFavoriteButtonAppearance()
                Toast.makeText(this@RecipeDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                Log.e("RecipeDetailActivity", "Error de conexión al añadir favorito", t)
            }
        })
    }

    private fun removeFromFavorites(userId: Int) {
        RetrofitClient.getApiService().removeFromFavorites(recipeId, userId).enqueue(object : Callback<FavoriteResponse> {
            override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RecipeDetailActivity, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    Log.d("RecipeDetailActivity", "Eliminado de favoritos correctamente")
                } else {
                    // Revertir cambio en UI
                    isFavorite = true
                    updateFavoriteButtonAppearance()
                    Toast.makeText(this@RecipeDetailActivity, "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show()
                    Log.e("RecipeDetailActivity", "Error al eliminar de favoritos: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                // Revertir cambio en UI
                isFavorite = true
                updateFavoriteButtonAppearance()
                Toast.makeText(this@RecipeDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                Log.e("RecipeDetailActivity", "Error de conexión al eliminar favorito", t)
            }
        })
    }

    private fun showRecetaView() {
        // Mostrar vista de receta y ocultar vista de ingredientes
        svRecetaContainer.visibility = View.VISIBLE
        svIngredientesContainer.visibility = View.GONE

        // Actualizar estilo de los botones
        btnReceta.alpha = 1.0f
        btnIngredientes.alpha = 0.7f
    }

    private fun showIngredientesView() {
        // Mostrar vista de ingredientes y ocultar vista de receta
        svRecetaContainer.visibility = View.GONE
        svIngredientesContainer.visibility = View.VISIBLE

        // Actualizar estilo de los botones
        btnReceta.alpha = 0.7f
        btnIngredientes.alpha = 1.0f
    }
}