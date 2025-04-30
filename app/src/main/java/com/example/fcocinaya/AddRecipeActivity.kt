package com.example.fcocinaya


import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var btnClose: Button
    private lateinit var btnAddIngredient: ImageButton
    private lateinit var btnAddStep: ImageButton
    private lateinit var cvPhoto: CardView
    private lateinit var cvVideo: CardView
    private lateinit var btnAddRecipeSubmit: Button
    private lateinit var etRecipeName: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etSteps: EditText
    private lateinit var ivPhotoPlaceholder: ImageView

    private var selectedImageUri: Uri? = null
    private var base64Image: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Inicializar vistas
        btnClose = findViewById(R.id.btnClose)
        btnAddIngredient = findViewById(R.id.btnAddIngredient)
        btnAddStep = findViewById(R.id.btnAddStep)
        cvPhoto = findViewById(R.id.cvPhoto)
        cvVideo = findViewById(R.id.cvVideo)
        btnAddRecipeSubmit = findViewById(R.id.btnAddRecipeSubmit)
        etRecipeName = findViewById(R.id.etRecipeName)
        etIngredients = findViewById(R.id.etIngredients)
        etSteps = findViewById(R.id.etSteps)
        ivPhotoPlaceholder = findViewById(R.id.ivPhotoPlaceholder)

        // Configurar listeners
        btnClose.setOnClickListener {
            finish() // Cerrar la actividad
        }

        btnAddIngredient.setOnClickListener {
            // Para esta implementación, simplemente mostraremos un mensaje
            Toast.makeText(this, "Agrega los ingredientes en el campo de texto", Toast.LENGTH_SHORT).show()
        }

        btnAddStep.setOnClickListener {
            // Para esta implementación, simplemente mostraremos un mensaje
            Toast.makeText(this, "Agrega los pasos en el campo de texto", Toast.LENGTH_SHORT).show()
        }



        cvVideo.setOnClickListener {
            Toast.makeText(this, "Función de video no implementada aún", Toast.LENGTH_SHORT).show()
        }

        btnAddRecipeSubmit.setOnClickListener {
            submitRecipe()
        }
    }



    private fun submitRecipe() {
        // Validar campos
        val title = etRecipeName.text.toString().trim()
        if (title.isEmpty()) {
            etRecipeName.error = "El título es obligatorio"
            return
        }

        // Validar ingredientes
        val ingredients = etIngredients.text.toString().trim()
        if (ingredients.isEmpty()) {
            etIngredients.error = "Agrega al menos un ingrediente"
            return
        }

        // Validar pasos
        val steps = etSteps.text.toString().trim()
        if (steps.isEmpty()) {
            etSteps.error = "Agrega al menos un paso"
            return
        }

        // Mostrar progreso (puedes añadir un ProgressBar si lo deseas)
        btnAddRecipeSubmit.isEnabled = false
        btnAddRecipeSubmit.text = "Guardando..."

        // Crear objeto de receta
        val recipeRequest = RecipeRequest(
            titulo = title,
            ingredientes = ingredients,
            pasos = steps,
            imagen = base64Image
        )

        // Enviar a la API
        RetrofitClient.getApiService().createRecipe(recipeRequest).enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                btnAddRecipeSubmit.isEnabled = true
                btnAddRecipeSubmit.text = "agregar receta"

                if (response.isSuccessful) {
                    Toast.makeText(this@AddRecipeActivity, "Receta agregada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@AddRecipeActivity, "Error: $errorBody", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@AddRecipeActivity, "Error al agregar la receta", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                btnAddRecipeSubmit.isEnabled = true
                btnAddRecipeSubmit.text = "agregar receta"
                Toast.makeText(this@AddRecipeActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}