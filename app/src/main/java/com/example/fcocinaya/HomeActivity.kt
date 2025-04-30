package com.example.fcocinaya

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.fragment.app.Fragment
import com.example.fcocinaya.model.RecipeModel


class HomeActivity : AppCompatActivity() {

    private lateinit var toggleIndicator: View
    private lateinit var btnRecipes: Button
    private lateinit var btnFavorites: Button
    private lateinit var btnYourRecipes: Button
    private lateinit var btnSearchRecipe: Button
    private lateinit var btnSearchRecipeFavorites: Button
    private lateinit var btnAddRecipe: Button
    private lateinit var recipesContainer: LinearLayout
    private lateinit var containerRecipes: View
    private lateinit var containerFavorites: View
    private lateinit var containerYourRecipes: View
    private lateinit var favoritesContainer: LinearLayout
    private lateinit var scrollViewFavorites: ScrollView
    private lateinit var profileInitial: TextView
    private lateinit var welcomeMessage: TextView
    private lateinit var btnViewUserRecipes: Button
    // Fragmentos
    private val favoritesFragment = FavoritesFragment()
    private val yourRecipesFragment = YourRecipesFragment()
    private var activeFragment: Fragment? = null
    private lateinit var fragmentContainer: View

    // Variables para almacenar las recetas
    private var allRecipes: List<Recipe> = emptyList()
    private var favoriteRecipes: List<Recipe> = emptyList()
    private var yourRecipes: List<Recipe> = emptyList()
    private var userId = -1

    // Variable para almacenar el término de búsqueda actual
    private var currentSearchTerm: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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

        // Obtén la referencia al CardView y aplica animación
        val cardView = findViewById<CardView>(R.id.cardView)
        val slideUpFromBottom = AnimationUtils.loadAnimation(this, R.anim.slide_up_from_bottom)
        cardView.startAnimation(slideUpFromBottom)

        // Referencias a los botones y al indicador
        toggleIndicator = findViewById(R.id.toggleIndicator)
        btnRecipes = findViewById(R.id.btnRecipes)
        btnFavorites = findViewById(R.id.btnFavorites)
        btnYourRecipes = findViewById(R.id.btnYourRecipes)

        // Referencia a la foto de perfil (el TextView circular con la inicial)
        profileInitial = findViewById(R.id.profileInitial)
        welcomeMessage = findViewById(R.id.welcomeMessage)

        // Establecer la inicial del usuario y mensaje de bienvenida
        val userName = RetrofitClient.getUserName()
        if (userName.isNotEmpty()) {
            profileInitial.text = userName.first().toString().uppercase()
            welcomeMessage.text = "Hola $userName"
        }

        // Configurar el listener para abrir la actividad de perfil
        profileInitial.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Referencias a los contenedores
        containerRecipes = findViewById(R.id.containerRecipes)
        containerFavorites = findViewById(R.id.containerFavorites)
        containerYourRecipes = findViewById(R.id.containerYourRecipes)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        // Referencias a la lista de favoritos
        favoritesContainer = containerFavorites.findViewById(R.id.favoritesContainer)
        scrollViewFavorites = containerFavorites.findViewById(R.id.scrollViewFavorites)

        // Botones de búsqueda
        btnSearchRecipe = containerRecipes.findViewById(R.id.btnSearchRecipe)
        btnSearchRecipeFavorites = containerFavorites.findViewById(R.id.btnSearchRecipeFavorites)

        // Botón Ver Recetas de usuarios
        btnViewUserRecipes = containerRecipes.findViewById(R.id.btnViewUserRecipes)

        // Referencias a los contenedores dentro de container_recipes
        recipesContainer = containerRecipes.findViewById(R.id.recipesContainer)

        // Botón de agregar receta (en containerYourRecipes)
        btnAddRecipe = containerYourRecipes.findViewById(R.id.btnAddRecipe)

        // Configura el ancho inicial del indicador
        btnRecipes.post {
            toggleIndicator.layoutParams.width = btnRecipes.width
            toggleIndicator.requestLayout()
        }

        // Configurar los listeners de los botones
// Configurar los listeners de los botones
        btnRecipes.setOnClickListener {
            moveIndicatorToButton(btnRecipes)
            setButtonColors(btnRecipes, btnFavorites, btnYourRecipes)
            showContainerRecipes()
        }

        btnFavorites.setOnClickListener {
            moveIndicatorToButton(btnFavorites)
            setButtonColors(btnFavorites, btnRecipes, btnYourRecipes)
            showFavoritesFragment()
        }

        btnYourRecipes.setOnClickListener {
            moveIndicatorToButton(btnYourRecipes)
            setButtonColors(btnYourRecipes, btnRecipes, btnFavorites)
            showYourRecipesFragment()
        }

        // Configurar el clic del botón de búsqueda en la sección Recetas
        btnSearchRecipe.setOnClickListener {
            showSearchDialog("Recetas")
        }

        // Configurar el clic del botón de búsqueda en la sección Favoritos
        btnSearchRecipeFavorites.setOnClickListener {
            showSearchDialog("Favoritos")
        }

        // Configurar el clic del botón de búsqueda en la sección Tus Recetas
        try {
            val btnSearchYourRecipe =
                containerYourRecipes.findViewById<Button>(R.id.btnSearchRecipe)
            btnSearchYourRecipe?.setOnClickListener {
                showSearchDialog("TusRecetas")
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error al configurar btnSearchYourRecipe", e)
        }

        // Configurar el clic del botón "Recetas Publicadas" si existe
        try {
            val btnRecetasPublicadas =
                containerYourRecipes.findViewById<Button>(R.id.btnRecetasPublicadas)
            btnRecetasPublicadas?.setOnClickListener {
                Toast.makeText(this, "Recetas Publicadas", Toast.LENGTH_SHORT).show()
                // Aquí puedes implementar la lógica para mostrar solo las recetas publicadas
                loadYourRecipes(true)
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error al configurar btnRecetasPublicadas", e)
        }

        // Configurar el clic del botón "Ver Recetas de usuarios"
        btnViewUserRecipes.setOnClickListener {
            // Iniciar la actividad CommunityActivity
            val intent = Intent(this, CommunityActivity::class.java)
            startActivity(intent)
        }

        // Configurar el clic del botón "Agregar Receta"
        btnAddRecipe.setOnClickListener {
            // Iniciar la actividad AddRecipeActivity
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

        // Cargar las recetas iniciales
        if (savedInstanceState == null) {
            loadRecipes()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando se vuelva a la actividad
        when {
            containerRecipes.visibility == View.VISIBLE -> loadRecipes()
            fragmentContainer.visibility == View.VISIBLE && activeFragment == favoritesFragment -> {
                // No es necesario hacer nada, el fragmento maneja su propio estado
            }
            fragmentContainer.visibility == View.VISIBLE && activeFragment == yourRecipesFragment -> {
                // No es necesario hacer nada, el fragmento maneja su propio estado
            }
            containerFavorites.visibility == View.VISIBLE -> loadFavorites()
            containerYourRecipes.visibility == View.VISIBLE -> loadYourRecipes()
        }
    }

    private fun moveIndicatorToButton(button: Button) {
        val endPosition = button.x - btnRecipes.x
        toggleIndicator.animate().translationX(endPosition).setDuration(200).start()
    }

    private fun setButtonColors(
        selectedButton: Button,
        unselectedButton1: Button,
        unselectedButton2: Button
    ) {
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.blanco))
        unselectedButton1.setTextColor(ContextCompat.getColor(this, R.color.darkRed))
        unselectedButton2.setTextColor(ContextCompat.getColor(this, R.color.darkRed))
    }

    /**
     * Muestra el diálogo de búsqueda y maneja los resultados
     */
    private fun showSearchDialog(section: String) {
        val searchDialog = SearchDialog(this) { searchTerm ->
            currentSearchTerm = searchTerm
            Toast.makeText(this, "Buscando: $searchTerm", Toast.LENGTH_SHORT).show()

            // Ejecutar la búsqueda según la sección actual
            when (section) {
                "Recetas" -> searchRecipes(searchTerm)
                "Favoritos" -> searchFavorites(searchTerm)
                "TusRecetas" -> searchYourRecipes(searchTerm)
            }
        }
        searchDialog.show()
    }
    private fun showContainerRecipes() {
        // Ocultar fragmentos y mostrar la vista de recetas
        fragmentContainer.visibility = View.GONE
        if (activeFragment != null) {
            supportFragmentManager.beginTransaction().hide(activeFragment!!).commit()
            activeFragment = null
        }

        containerRecipes.visibility = View.VISIBLE
        containerFavorites.visibility = View.GONE
        containerYourRecipes.visibility = View.GONE

        // Cargar recetas
        loadRecipes()
    }

    private fun showFavoritesFragment() {
        // Ocultar contenedores y mostrar el fragmento de favoritos
        containerRecipes.visibility = View.GONE
        containerFavorites.visibility = View.GONE
        containerYourRecipes.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        // Añadir y mostrar el fragmento de favoritos si no está añadido aún
        if (!favoritesFragment.isAdded) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, favoritesFragment)
                .commit()
        }

        // Ocultar el fragmento activo anterior si existe
        if (activeFragment != null && activeFragment != favoritesFragment) {
            supportFragmentManager.beginTransaction().hide(activeFragment!!).commit()
        }

        // Mostrar el fragmento de favoritos
        supportFragmentManager.beginTransaction().show(favoritesFragment).commit()
        activeFragment = favoritesFragment
    }

    private fun showYourRecipesFragment() {
        // Ocultar contenedores y mostrar el fragmento de tus recetas
        containerRecipes.visibility = View.GONE
        containerFavorites.visibility = View.GONE
        containerYourRecipes.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        // Añadir y mostrar el fragmento de tus recetas si no está añadido aún
        if (!yourRecipesFragment.isAdded) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, yourRecipesFragment)
                .commit()
        }

        // Ocultar el fragmento activo anterior si existe
        if (activeFragment != null && activeFragment != yourRecipesFragment) {
            supportFragmentManager.beginTransaction().hide(activeFragment!!).commit()
        }

        // Mostrar el fragmento de tus recetas
        supportFragmentManager.beginTransaction().show(yourRecipesFragment).commit()
        activeFragment = yourRecipesFragment
    }
    /**
     * Busca recetas en la sección Recetas
     */
    private fun searchRecipes(searchTerm: String) {
        // Si el término de búsqueda está vacío, mostrar todas las recetas
        if (searchTerm.isEmpty()) {
            displayRecipes(allRecipes)
            return
        }

        // Filtrar recetas según el término de búsqueda
        val filteredRecipes = allRecipes.filter {
            it.name.contains(searchTerm, ignoreCase = true)
        }

        // Mostrar recetas filtradas
        displayRecipes(filteredRecipes)
    }

    /**
     * Busca recetas en la sección Favoritos
     */
    private fun searchFavorites(searchTerm: String) {
        // Si el término de búsqueda está vacío, mostrar todos los favoritos
        if (searchTerm.isEmpty()) {
            displayFavorites(favoriteRecipes)
            return
        }

        // Filtrar favoritos según el término de búsqueda
        val filteredFavorites = favoriteRecipes.filter {
            it.name.contains(searchTerm, ignoreCase = true)
        }

        // Mostrar favoritos filtrados
        displayFavorites(filteredFavorites)
    }

    /**
     * Busca recetas en la sección Tus Recetas
     */
    private fun searchYourRecipes(searchTerm: String) {
        // Si el término de búsqueda está vacío, mostrar todas tus recetas
        if (searchTerm.isEmpty()) {
            displayYourRecipes(yourRecipes)
            return
        }

        // Filtrar tus recetas según el término de búsqueda
        val filteredYourRecipes = yourRecipes.filter {
            it.name.contains(searchTerm, ignoreCase = true)
        }

        // Mostrar tus recetas filtradas
        displayYourRecipes(filteredYourRecipes)
    }

    private fun loadRecipes() {
        recipesContainer.removeAllViews()

        // Mostrar un mensaje de carga
        val loadingView = TextView(this)
        loadingView.text = "Cargando recetas..."
        loadingView.textSize = 16f
        loadingView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
        loadingView.gravity = android.view.Gravity.CENTER
        recipesContainer.addView(loadingView)

        // Intentar cargar recetas desde la API
        try {
            RetrofitClient.getApiService().getAllRecipes()
                .enqueue(object : Callback<List<RecipeModel>> {
                    override fun onResponse(
                        call: Call<List<RecipeModel>>,
                        response: Response<List<RecipeModel>>
                    ) {
                        recipesContainer.removeAllViews()

                        if (response.isSuccessful) {
                            val recipeModels = response.body() ?: emptyList()
                            Log.d("HomeActivity", "Recetas recibidas: ${recipeModels.size}")

                            // Convertir modelos a objetos Recipe
                            val apiRecipes = recipeModels.map { Recipe.fromRecipeModel(it) }

                            // Combinar con recetas estáticas
                            val staticRecipes = getStaticRecipes()
                            allRecipes = staticRecipes + apiRecipes

                            // Si hay un término de búsqueda activo, filtrar las recetas
                            if (currentSearchTerm.isNotEmpty()) {
                                searchRecipes(currentSearchTerm)
                            } else {
                                displayRecipes(allRecipes)
                            }
                        } else {
                            // Si hay error, mostrar solo recetas estáticas
                            Log.e("HomeActivity", "Error al cargar recetas: ${response.code()}")
                            allRecipes = getStaticRecipes()
                            displayRecipes(allRecipes)
                        }
                    }

                    override fun onFailure(call: Call<List<RecipeModel>>, t: Throwable) {
                        recipesContainer.removeAllViews()
                        Log.e("HomeActivity", "Error de conexión al cargar recetas", t)

                        // Usar recetas estáticas como fallback
                        allRecipes = getStaticRecipes()
                        displayRecipes(allRecipes)
                    }
                })
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error al llamar al endpoint de recetas", e)

            // Cargar recetas estáticas como fallback
            allRecipes = getStaticRecipes()
            displayRecipes(allRecipes)
        }
    }

    private fun displayRecipes(recipes: List<Recipe>) {
        recipesContainer.removeAllViews()
        Log.d("HomeActivity", "Mostrando ${recipes.size} recetas")

        if (recipes.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = if (currentSearchTerm.isEmpty()) {
                "No hay recetas disponibles"
            } else {
                "No se encontraron recetas para \"$currentSearchTerm\""
            }
            emptyView.textSize = 16f
            emptyView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
            emptyView.gravity = android.view.Gravity.CENTER
            recipesContainer.addView(emptyView)
            return
        }

        for (recipe in recipes) {
            Log.d("HomeActivity", "Preparando vista para receta: ${recipe.name}, ID: ${recipe.id}")
            val cardView =
                layoutInflater.inflate(R.layout.item_recipe, recipesContainer, false) as CardView

            // Configurar el nombre de la receta
            val recipeName = cardView.findViewById<TextView>(R.id.recipeName)
            recipeName.text = recipe.name

            // Configurar la imagen de la receta
            val recipeImage = cardView.findViewById<ImageView>(R.id.recipeImage)

            // Intentar cargar la imagen desde base64 si existe
            if (recipe.imageUrl.isNotEmpty()) {
                try {
                    // Verificar si la imagen ya está en formato Base64 o si necesita un prefijo
                    var imageString = recipe.imageUrl
                    // Eliminar prefijo "data:image/jpeg;base64," si existe
                    if (imageString.contains(",")) {
                        imageString = imageString.split(",")[1]
                    }

                    val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (decodedImage != null) {
                        recipeImage.setImageBitmap(decodedImage)
                    } else {
                        // Si no se puede decodificar, usar imagen por defecto
                        recipeImage.setImageResource(recipe.imageResId)
                    }
                } catch (e: Exception) {
                    // Si hay error, usar imagen por defecto
                    recipeImage.setImageResource(recipe.imageResId)
                }
            } else {
                // Si no hay imagen base64, usar imagen por defecto
                recipeImage.setImageResource(recipe.imageResId)
            }

            // Configurar el botón para ver la receta
            val btnViewRecipe = cardView.findViewById<Button>(R.id.btnViewRecipe)
            btnViewRecipe.setOnClickListener {
                Log.d("HomeActivity", "Viendo receta: ${recipe.name}, ID: ${recipe.id}")

                // Identificar si es una receta estática (ID >= 100)
                val isStaticRecipe = recipe.id >= 100

                // Preparar el intent con todos los datos necesarios
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                intent.putExtra("recipe_name", recipe.name)

                // Si es una receta estática, pasar todos los datos necesarios
                if (isStaticRecipe) {
                    intent.putExtra("is_static_recipe", true)
                    intent.putExtra("recipe_ingredients", recipe.ingredients)
                    intent.putExtra("recipe_steps", recipe.steps)
                } else {
                    intent.putExtra("is_static_recipe", false)
                }

                startActivity(intent)
            }

            recipesContainer.addView(cardView)
        }
    }

    private fun loadFavorites() {
        favoritesContainer.removeAllViews()

        // Mostrar un mensaje de carga
        val loadingView = TextView(this)
        loadingView.text = "Cargando favoritos..."
        loadingView.textSize = 16f
        loadingView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
        loadingView.gravity = android.view.Gravity.CENTER
        favoritesContainer.addView(loadingView)

        // Obtener ID del usuario
        val userId = RetrofitClient.getUserId()

        // Verificar que el usuario tenga un ID válido
        if (userId <= 0) {
            Log.e("HomeActivity", "Error: ID de usuario no válido ($userId) al cargar favoritos")
            favoritesContainer.removeAllViews()
            val errorView = TextView(this)
            errorView.text = "No se pudieron cargar tus favoritos. Inicia sesión nuevamente."
            errorView.textSize = 16f
            errorView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
            errorView.gravity = android.view.Gravity.CENTER
            favoritesContainer.addView(errorView)
            return
        }

        Log.d("HomeActivity", "Cargando favoritos para el usuario con ID: $userId")

        try {
            // Llamar al endpoint para obtener favoritos
            RetrofitClient.getApiService().getUserFavorites(userId)
                .enqueue(object : Callback<List<RecipeModel>> {
                    override fun onResponse(
                        call: Call<List<RecipeModel>>,
                        response: Response<List<RecipeModel>>
                    ) {
                        favoritesContainer.removeAllViews()

                        // Registra información detallada para depuración
                        Log.d(
                            "HomeActivity",
                            "Respuesta getUserFavorites - código: ${response.code()}"
                        )
                        Log.d(
                            "HomeActivity",
                            "Respuesta getUserFavorites - URL: ${call.request().url}"
                        )

                        if (response.isSuccessful) {
                            val recipeModels = response.body() ?: emptyList()
                            Log.d("HomeActivity", "Favoritos recibidos: ${recipeModels.size}")

                            if (recipeModels.isEmpty()) {
                                // Mostrar mensaje si no hay favoritos
                                val emptyView = TextView(this@HomeActivity)
                                emptyView.text = "No tienes recetas favoritas"
                                emptyView.textSize = 16f
                                emptyView.setTextColor(
                                    ContextCompat.getColor(
                                        this@HomeActivity,
                                        R.color.blanco
                                    )
                                )
                                emptyView.gravity = android.view.Gravity.CENTER
                                favoritesContainer.addView(emptyView)

                                // Actualizar la lista en memoria
                                favoriteRecipes = emptyList()
                                return
                            }

                            // Convertir modelos a objetos Recipe
                            favoriteRecipes = recipeModels.map { Recipe.fromRecipeModel(it) }

                            // Si hay un término de búsqueda activo, filtrar favoritos
                            if (currentSearchTerm.isNotEmpty()) {
                                searchFavorites(currentSearchTerm)
                            } else {
                                displayFavorites(favoriteRecipes)
                            }
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                Log.e(
                                    "HomeActivity",
                                    "Error al cargar favoritos: ${response.code()}, $errorBody"
                                )

                                // Si es un error 404, podría ser que el endpoint no existe aún
                                if (response.code() == 404) {
                                    Log.w(
                                        "HomeActivity",
                                        "El endpoint de favoritos podría no estar implementado en el backend."
                                    )
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Función de favoritos no disponible",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Error al cargar favoritos: ${response.code()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                // Mostrar mensaje de error
                                val errorView = TextView(this@HomeActivity)
                                errorView.text = "No se pudieron cargar tus favoritos"
                                errorView.textSize = 16f
                                errorView.setTextColor(
                                    ContextCompat.getColor(
                                        this@HomeActivity,
                                        R.color.blanco
                                    )
                                )
                                errorView.gravity = android.view.Gravity.CENTER
                                favoritesContainer.addView(errorView)

                                // Limpiar la lista en memoria
                                favoriteRecipes = emptyList()
                            } catch (e: Exception) {
                                Log.e("HomeActivity", "Error al procesar respuesta de error", e)

                                // Mostrar mensaje de error genérico
                                val errorView = TextView(this@HomeActivity)
                                errorView.text = "Error inesperado al cargar favoritos"
                                errorView.textSize = 16f
                                errorView.setTextColor(
                                    ContextCompat.getColor(
                                        this@HomeActivity,
                                        R.color.blanco
                                    )
                                )
                                errorView.gravity = android.view.Gravity.CENTER
                                favoritesContainer.addView(errorView)

                                // Limpiar la lista en memoria
                                favoriteRecipes = emptyList()
                            }
                        }
                    }

                    override fun onFailure(call: Call<List<RecipeModel>>, t: Throwable) {
                        favoritesContainer.removeAllViews()
                        Log.e("HomeActivity", "Error al cargar favoritos", t)
                        Log.e("HomeActivity", "URL de la solicitud: ${call.request().url}")
                        Toast.makeText(
                            this@HomeActivity,
                            "Error de conexión: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Mostrar mensaje de error de conexión
                        val errorView = TextView(this@HomeActivity)
                        errorView.text = "Error de conexión. Revisa tu conexión a Internet."
                        errorView.textSize = 16f
                        errorView.setTextColor(
                            ContextCompat.getColor(
                                this@HomeActivity,
                                R.color.blanco
                            )
                        )
                        errorView.gravity = android.view.Gravity.CENTER
                        favoritesContainer.addView(errorView)

                        // Limpiar la lista en memoria
                        favoriteRecipes = emptyList()
                    }
                })
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error al llamar al endpoint de favoritos", e)

            // Mostrar mensaje de error
            favoritesContainer.removeAllViews()
            val errorView = TextView(this)
            errorView.text = "Función de favoritos no disponible"
            errorView.textSize = 16f
            errorView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
            errorView.gravity = android.view.Gravity.CENTER
            favoritesContainer.addView(errorView)

            // Limpiar la lista en memoria
            favoriteRecipes = emptyList()
        }
    }

    private fun displayYourRecipes(recipes: List<Recipe>) {
        // Obtener el contenedor de recetas
        val recipesContainer =
            containerYourRecipes.findViewById<LinearLayout>(R.id.recipesContainer)
        recipesContainer.removeAllViews()

        Log.d("HomeActivity", "Mostrando ${recipes.size} recetas del usuario")

        if (recipes.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = if (currentSearchTerm.isEmpty()) {
                "No has creado recetas todavía"
            } else {
                "No se encontraron recetas para \"$currentSearchTerm\""
            }
            emptyView.textSize = 16f
            emptyView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
            emptyView.gravity = android.view.Gravity.CENTER
            recipesContainer.addView(emptyView)
            return
        }

        // Agregar cada receta al contenedor
        for (recipe in recipes) {
            Log.d("HomeActivity", "Añadiendo receta: ID=${recipe.id}, Nombre=${recipe.name}")

            // Inflar el layout de la receta
            val recipeView =
                LayoutInflater.from(this).inflate(R.layout.item_tus_receta, recipesContainer, false)

            // Configurar los datos
            val tvRecipeName = recipeView.findViewById<TextView>(R.id.recipeName)
            tvRecipeName.text = recipe.name

            // Configurar los botones
            val btnViewRecipe = recipeView.findViewById<Button>(R.id.buttonViewRecipe)
            val btnDelete = recipeView.findViewById<Button>(R.id.buttonDelete)
            val btnPublish = recipeView.findViewById<Button>(R.id.buttonSubirReceta)

            // Añadir listeners
            btnViewRecipe.setOnClickListener {
                if (recipe.id <= 0) {
                    Toast.makeText(this, "No se puede abrir esta receta", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Log.d(
                    "HomeActivity",
                    "Abriendo detalle de receta: ID=${recipe.id}, Nombre=${recipe.name}"
                )
                // Abrir la actividad de detalle de receta
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                intent.putExtra("recipe_name", recipe.name)
                startActivity(intent)
            }

            btnDelete.setOnClickListener {
                if (recipe.id <= 0) {
                    Toast.makeText(this, "No se puede eliminar esta receta", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                deleteRecipe(recipe.id, recipe.name)
            }

            btnPublish.setOnClickListener {
                if (recipe.id <= 0) {
                    Toast.makeText(this, "No se puede publicar esta receta", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                publishRecipe(recipe.id, recipe.name, !recipe.isPublished)
            }

            // Actualizar el texto del botón de publicar según el estado actual
            if (recipe.isPublished) {
                btnPublish.text = "Despublicar"
            } else {
                btnPublish.text = "Publicar"
            }

            // Agregar la vista al contenedor
            recipesContainer.addView(recipeView)
        }
    }

    private fun publishRecipe(recipeId: Int, recipeName: String, publish: Boolean) {
        val action = if (publish) "publicar" else "despublicar"

        // Mostrar indicador de progreso
        Toast.makeText(this, "${action.capitalize()} receta...", Toast.LENGTH_SHORT).show()

        // Preparar la solicitud
        val publishRequest = PublishRecipeRequest(isPublished = publish)

        // Actualizar la UI optimistamente
        val updatedRecipes = yourRecipes.map {
            if (it.id == recipeId) {
                it.copy(isPublished = publish)
            } else {
                it
            }
        }

        yourRecipes = updatedRecipes

        // Si hay un término de búsqueda activo, volver a filtrar
        if (currentSearchTerm.isNotEmpty()) {
            searchYourRecipes(currentSearchTerm)
        } else {
            displayYourRecipes(yourRecipes)
        }

        // Llamar a la API para publicar/despublicar
        try {
            RetrofitClient.getApiService().publishRecipe(recipeId, publishRequest)
                .enqueue(object : Callback<PublishRecipeResponse> {
                    override fun onResponse(
                        call: Call<PublishRecipeResponse>,
                        response: Response<PublishRecipeResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@HomeActivity,
                                "Receta ${action}da correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            // La actualización ya se hizo optimistamente, no es necesario recargar
                        } else {
                            // Revertir el cambio optimista
                            val revertedRecipes = yourRecipes.map {
                                if (it.id == recipeId) {
                                    it.copy(isPublished = !publish)
                                } else {
                                    it
                                }
                            }
                            yourRecipes = revertedRecipes
                            displayYourRecipes(yourRecipes)

                            Toast.makeText(
                                this@HomeActivity,
                                "Error al ${action} la receta",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<PublishRecipeResponse>, t: Throwable) {
                        // Revertir el cambio optimista
                        val revertedRecipes = yourRecipes.map {
                            if (it.id == recipeId) {
                                it.copy(isPublished = !publish)
                            } else {
                                it
                            }
                        }
                        yourRecipes = revertedRecipes
                        displayYourRecipes(yourRecipes)

                        Toast.makeText(this@HomeActivity, "Error de conexión", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error al llamar al endpoint de publicar receta", e)
            Toast.makeText(this, "Función de publicar no disponible", Toast.LENGTH_SHORT).show()

            // Como es una operación simulada, dejamos el cambio optimista por ahora
        }
    }

    private fun deleteRecipe(recipeId: Int, recipeName: String) {
        // Mostrar diálogo de confirmación
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar receta")
            .setMessage("¿Estás seguro de que deseas eliminar la receta '$recipeName'?")
            .setPositiveButton("Sí") { _, _ ->
                // Mostrar algún indicador de progreso
                Toast.makeText(this, "Eliminando receta...", Toast.LENGTH_SHORT).show()

                // Actualizar la lista local optimistamente
                yourRecipes = yourRecipes.filter { it.id != recipeId }

                // Si hay un término de búsqueda activo, volver a filtrar
                if (currentSearchTerm.isNotEmpty()) {
                    searchYourRecipes(currentSearchTerm)
                } else {
                    displayYourRecipes(yourRecipes)
                }

                // Llamar a la API para eliminar
                RetrofitClient.getApiService().deleteRecipe(recipeId)
                    .enqueue(object : Callback<DeleteRecipeResponse> {
                        override fun onResponse(
                            call: Call<DeleteRecipeResponse>,
                            response: Response<DeleteRecipeResponse>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@HomeActivity,
                                    "Receta eliminada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // La actualización ya se hizo optimistamente, no es necesario recargar
                            } else {
                                // En caso de error, volver a cargar las recetas para mantener consistencia
                                Toast.makeText(
                                    this@HomeActivity,
                                    "Error al eliminar la receta",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadYourRecipes() // Recargar para asegurar consistencia
                            }
                        }

                        override fun onFailure(call: Call<DeleteRecipeResponse>, t: Throwable) {
                            Toast.makeText(
                                this@HomeActivity,
                                "Error de conexión: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadYourRecipes() // Recargar para asegurar consistencia
                        }
                    })
            }
            .setNegativeButton("No", null)
            .show()
    }

// Método loadYourRecipes:
// Asegúrate de añadir este método a tu HomeActivity.kt si está faltando

    private fun loadYourRecipes(onlyPublished: Boolean = false) {
        // Obtener el contenedor de recetas
        val recipesContainer =
            containerYourRecipes.findViewById<LinearLayout>(R.id.recipesContainer)
        recipesContainer.removeAllViews()

        // Mostrar un mensaje de carga
        val loadingView = TextView(this)
        loadingView.text = "Cargando tus recetas..."
        loadingView.textSize = 16f
        loadingView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
        loadingView.gravity = android.view.Gravity.CENTER
        recipesContainer.addView(loadingView)

        // Obtener ID del usuario
        val userId = RetrofitClient.getUserId()

        // Verifica que el usuario tenga un ID válido
        if (userId <= 0) {
            Log.e("HomeActivity", "Error: ID de usuario no válido ($userId)")
            recipesContainer.removeAllViews()
            val errorView = TextView(this)
            errorView.text = "No se pudo cargar tus recetas. Inicia sesión nuevamente."
            errorView.textSize = 16f
            errorView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
            errorView.gravity = android.view.Gravity.CENTER
            recipesContainer.addView(errorView)
            return
        }

        Log.d("HomeActivity", "Cargando recetas para el usuario con ID: $userId")

        try {
            // Llamar al endpoint con el ID de usuario
            RetrofitClient.getApiService().getUserRecipes(userId)
                .enqueue(object : Callback<List<RecipeModel>> {
                    override fun onResponse(
                        call: Call<List<RecipeModel>>,
                        response: Response<List<RecipeModel>>
                    ) {
                        recipesContainer.removeAllViews()

                        // Registra información detallada para depuración
                        Log.d(
                            "HomeActivity",
                            "Respuesta getUserRecipes - código: ${response.code()}"
                        )
                        Log.d(
                            "HomeActivity",
                            "Respuesta getUserRecipes - URL: ${call.request().url}"
                        )

                        if (response.isSuccessful) {
                            val recipeModels = response.body() ?: emptyList()
                            Log.d("HomeActivity", "Recetas recibidas: ${recipeModels.size}")

                            if (recipeModels.isEmpty()) {
                                // Mostrar mensaje si no hay recetas
                                val emptyView = TextView(this@HomeActivity)
                                emptyView.text = "No has creado ninguna receta todavía"
                                emptyView.textSize = 16f
                                emptyView.setTextColor(
                                    ContextCompat.getColor(
                                        this@HomeActivity,
                                        R.color.blanco
                                    )
                                )
                                emptyView.gravity = android.view.Gravity.CENTER
                                recipesContainer.addView(emptyView)

                                // Actualizar la lista en memoria
                                yourRecipes = emptyList()
                                return
                            }

                            // Convertir los modelos a recetas
                            val allUserRecipes = recipeModels.map { Recipe.fromRecipeModel(it) }

                            // Filtrar por publicadas si es necesario
                            yourRecipes = if (onlyPublished) {
                                allUserRecipes.filter { it.isPublished }
                            } else {
                                allUserRecipes
                            }

                            if (yourRecipes.isEmpty() && onlyPublished) {
                                // Si no hay recetas publicadas pero se pidió solo publicadas
                                val emptyView = TextView(this@HomeActivity)
                                emptyView.text = "No tienes recetas publicadas todavía"
                                emptyView.textSize = 16f
                                emptyView.setTextColor(
                                    ContextCompat.getColor(
                                        this@HomeActivity,
                                        R.color.blanco
                                    )
                                )
                                emptyView.gravity = android.view.Gravity.CENTER
                                recipesContainer.addView(emptyView)
                                return
                            }

                            // Si hay un término de búsqueda activo, filtrar tus recetas
                            if (currentSearchTerm.isNotEmpty()) {
                                searchYourRecipes(currentSearchTerm)
                            } else {
                                displayYourRecipes(yourRecipes)
                            }
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                Log.e(
                                    "HomeActivity",
                                    "Error al cargar tus recetas: ${response.code()}, $errorBody"
                                )
                                Toast.makeText(
                                    this@HomeActivity,
                                    "Error al cargar tus recetas: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Si es un error de autenticación (401), podría ser un problema con el token
                                if (response.code() == 401) {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Sesión expirada. Inicia sesión nuevamente.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                // Mostrar mensaje de error
                                val errorView = TextView(this@HomeActivity)
                                errorView.text = "Error al cargar tus recetas"
                                errorView.textSize = 16f
                                errorView.setTextColor(
                                    ContextCompat.getColor(
                                        this@HomeActivity,
                                        R.color.blanco
                                    )
                                )
                                errorView.gravity = android.view.Gravity.CENTER
                                recipesContainer.addView(errorView)

                                // Limpiar la lista en memoria
                                yourRecipes = emptyList()
                            } catch (e: Exception) {
                                Log.e("HomeActivity", "Error al procesar respuesta de error", e)

                                // Mostrar mensaje de error genérico
                                val errorView = TextView(this@HomeActivity)
                                errorView.text = "Error inesperado al cargar tus recetas"
                                errorView.textSize = 16f
                                errorView.setTextColor(
                                    ContextCompat.getColor(
                                        this@HomeActivity,
                                        R.color.blanco
                                    )
                                )
                                errorView.gravity = android.view.Gravity.CENTER
                                recipesContainer.addView(errorView)

                                // Limpiar la lista en memoria
                                yourRecipes = emptyList()
                            }
                        }
                    }

                    override fun onFailure(call: Call<List<RecipeModel>>, t: Throwable) {
                        recipesContainer.removeAllViews()
                        Log.e("HomeActivity", "Error al cargar tus recetas", t)
                        Log.e("HomeActivity", "URL de la solicitud: ${call.request().url}")
                        Toast.makeText(
                            this@HomeActivity,
                            "Error de conexión: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Mostrar mensaje de error de conexión
                        val errorView = TextView(this@HomeActivity)
                        errorView.text = "Error de conexión. Revisa tu conexión a Internet."
                        errorView.textSize = 16f
                        errorView.setTextColor(
                            ContextCompat.getColor(
                                this@HomeActivity,
                                R.color.blanco
                            )
                        )
                        errorView.gravity = android.view.Gravity.CENTER
                        recipesContainer.addView(errorView)

                        // Limpiar la lista en memoria
                        yourRecipes = emptyList()
                    }
                })
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error al llamar al endpoint de mis recetas", e)

            // Mostrar mensaje de error
            recipesContainer.removeAllViews()
            val errorView = TextView(this)
            errorView.text = "Error inesperado al cargar tus recetas"
            errorView.textSize = 16f
            errorView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
            errorView.gravity = android.view.Gravity.CENTER
            recipesContainer.addView(errorView)

            // Limpiar la lista en memoria
            yourRecipes = emptyList()
        }
    }

// Método displayFavorites:
// Asegúrate de añadir este método a tu HomeActivity.kt si está faltando

    private fun displayFavorites(favorites: List<Recipe>) {
        favoritesContainer.removeAllViews()

        Log.d("HomeActivity", "Mostrando ${favorites.size} favoritos")

        if (favorites.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = if (currentSearchTerm.isEmpty()) {
                "No tienes recetas favoritas"
            } else {
                "No se encontraron favoritos para \"$currentSearchTerm\""
            }
            emptyView.textSize = 16f
            emptyView.setTextColor(ContextCompat.getColor(this, R.color.blanco))
            emptyView.gravity = android.view.Gravity.CENTER
            favoritesContainer.addView(emptyView)
            return
        }

        for (recipe in favorites) {
            Log.d("HomeActivity", "Añadiendo favorito: ID=${recipe.id}, Nombre=${recipe.name}")

            val cardView = LayoutInflater.from(this)
                .inflate(R.layout.item_favoritos, favoritesContainer, false) as CardView

            // Configurar el nombre de la receta
            val recipeName = cardView.findViewById<TextView>(R.id.recipeName)
            recipeName.text = recipe.name

            // Configurar el botón para ver la receta
            cardView.findViewById<Button>(R.id.buttonViewRecipe).setOnClickListener {
                // Verificar que la receta tenga un ID válido
                if (recipe.id <= 0) {
                    Toast.makeText(this, "No se puede abrir esta receta", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Log.d(
                    "HomeActivity",
                    "Abriendo detalle de favorito: ID=${recipe.id}, Nombre=${recipe.name}"
                )
                // Abrir la actividad de detalle de receta
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                intent.putExtra("recipe_name", recipe.name)
                startActivity(intent)
            }



                cardView.findViewById<Button>(R.id.buttonDelete).setOnClickListener {
                    if (recipe.id <= 0) {
                        Toast.makeText(
                            this,
                            "No se puede quitar esta receta de favoritos",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }


                }

                favoritesContainer.addView(cardView)
            }
        }

        private fun getStaticRecipes(): List<Recipe> {
            return listOf(
                Recipe(
                    id = 101,
                    name = "Tostadas de frijoles negros y aguacate",
                    ingredients = "- 4 tortillas de maíz\n- 1 lata de frijoles negros\n- 1 aguacate\n- 1/4 de cebolla\n- 1 tomate\n- Cilantro\n- Jugo de 1 limón\n- Sal al gusto",
                    steps = "1. Tostar las tortillas hasta que estén crujientes\n2. Calentar los frijoles y triturarlos ligeramente\n3. Cortar el aguacate en cubitos y mezclar con cebolla, tomate, cilantro, limón y sal\n4. Untar los frijoles sobre las tortillas tostadas\n5. Colocar la mezcla de aguacate encima"
                ),
                Recipe(
                    id = 102,
                    name = "Huevos en Purgatorio",
                    ingredients = "- 4 huevos\n- 1 lata de tomates triturados\n- 2 dientes de ajo\n- 1 cebolla pequeña\n- Aceite de oliva\n- Sal y pimienta\n- Chile rojo (opcional)\n- Pan tostado para acompañar",
                    steps = "1. En una sartén, calentar aceite y dorar ajo y cebolla picados\n2. Añadir tomates triturados y cocinar 10 minutos\n3. Hacer huecos en la salsa y romper los huevos en ellos\n4. Tapar y cocinar hasta que las claras estén cuajadas\n5. Servir caliente con pan tostado"
                ),
                Recipe(
                    id = 103,
                    name = "Ensalada César",
                    ingredients = "- 1 lechuga romana\n- 100g de queso parmesano\n- 2 pechugas de pollo\n- Pan para croutones\n- 2 huevos\n- Aceite de oliva\n- Mostaza\n- Jugo de limón\n- Anchoas (opcional)",
                    steps = "1. Cortar el pan en cubos y tostar para hacer croutones\n2. Cocinar las pechugas de pollo y cortarlas en tiras\n3. Preparar la salsa mezclando huevos, aceite, mostaza, limón y anchoas\n4. Lavar y cortar la lechuga\n5. Mezclar todo y añadir queso parmesano rallado"
                ),
                Recipe(
                    id = 104,
                    name = "Pasta Alfredo",
                    ingredients = "- 400g de fettuccine\n- 200ml de crema para batir\n- 100g de mantequilla\n- 100g de queso parmesano\n- Sal y pimienta\n- Nuez moscada",
                    steps = "1. Cocinar la pasta según las instrucciones del paquete\n2. En una sartén, derretir la mantequilla\n3. Añadir la crema y calentar sin hervir\n4. Incorporar el queso rallado y mezclar hasta obtener una salsa suave\n5. Añadir la pasta escurrida y mezclar bien"
                ),
                Recipe(
                    id = 105,
                    name = "Sopa de tomate",
                    ingredients = "- 1kg de tomates maduros\n- 1 cebolla\n- 2 dientes de ajo\n- 1 zanahoria\n- 1 litro de caldo de verduras\n- Aceite de oliva\n- Sal y pimienta\n- Albahaca fresca",
                    steps = "1. Sofreír cebolla, ajo y zanahoria picados\n2. Añadir los tomates cortados y cocinar 10 minutos\n3. Verter el caldo y cocinar 20 minutos más\n4. Triturar hasta obtener una textura suave\n5. Servir con hojas de albahaca fresca"
                )
            )
        }
    }
