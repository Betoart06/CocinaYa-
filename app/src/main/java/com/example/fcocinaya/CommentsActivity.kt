package com.example.fcocinaya

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsActivity : AppCompatActivity() {

    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var etNewComment: EditText
    private lateinit var btnAddComment: Button
    private lateinit var btnBack: Button
    private lateinit var tvRecipeName: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyComments: TextView

    private var recipeId: Int = -1
    private var recipeName: String = ""
    private var userId: Int = -1
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        // Obtener datos de la receta
        recipeId = intent.getIntExtra("recipe_id", -1)
        recipeName = intent.getStringExtra("recipe_name") ?: "Receta"

        // Obtener datos del usuario
        userId = RetrofitClient.getUserId()
        userName = RetrofitClient.getUserName()

        // Inicializar vistas
        recyclerViewComments = findViewById(R.id.recyclerViewComments)
        etNewComment = findViewById(R.id.etNewComment)
        btnAddComment = findViewById(R.id.btnAddComment)
        btnBack = findViewById(R.id.btnBack)
        tvRecipeName = findViewById(R.id.tvRecipeName)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyComments = findViewById(R.id.tvEmptyComments)

        // Configurar título
        tvRecipeName.text = recipeName

        // Configurar RecyclerView
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        commentsAdapter = CommentsAdapter(emptyList())
        recyclerViewComments.adapter = commentsAdapter

        // Configurar botón de volver
        btnBack.setOnClickListener {
            finish()
        }

        // Configurar botón para agregar comentario
        btnAddComment.setOnClickListener {
            val commentText = etNewComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            } else {
                Toast.makeText(this, "Ingresa un comentario", Toast.LENGTH_SHORT).show()
            }
        }

        // Cargar comentarios
        loadComments()
    }

    private fun loadComments() {
        if (recipeId == -1) {
            showEmptyState("No se puede cargar los comentarios")
            return
        }

        progressBar.visibility = View.VISIBLE
        tvEmptyComments.visibility = View.GONE

        // TODO: Reemplazar con llamada a API real cuando esté disponible
        // Por ahora, usamos datos de ejemplo
        RetrofitClient.getApiService().getRecipeComments(recipeId)
            .enqueue(object : Callback<List<CommentModel>> {
                override fun onResponse(
                    call: Call<List<CommentModel>>,
                    response: Response<List<CommentModel>>
                ) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val comments =
                            response.body()?.map { Comment.fromCommentModel(it) } ?: emptyList()

                        if (comments.isEmpty()) {
                            showEmptyState("No hay comentarios para esta receta")
                        } else {
                            commentsAdapter.updateComments(comments)
                            tvEmptyComments.visibility = View.GONE
                        }
                    } else {
                        // Mostrar mensaje de error y cargar datos de ejemplo
                        Toast.makeText(
                            this@CommentsActivity,
                            "Error al cargar comentarios",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadSampleComments()
                    }
                }

                override fun onFailure(call: Call<List<CommentModel>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@CommentsActivity,
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadSampleComments()
                }
            })
    }

    private fun loadSampleComments() {
        // Datos de ejemplo para mostrar cuando falla la API
        val sampleComments = listOf(
            Comment(
                id = 1,
                userId = 5,
                userName = "María López",
                recipeId = recipeId,
                content = "¡Excelente receta! La hice el fin de semana y quedó deliciosa.",
                createdAt = "2023-10-15 14:30:00"
            ),
            Comment(
                id = 2,
                userId = 8,
                userName = "Carlos Rodríguez",
                recipeId = recipeId,
                content = "¿Se puede sustituir algún ingrediente?",
                createdAt = "2023-10-16 10:15:00"
            ),
            Comment(
                id = 3,
                userId = 3,
                userName = "Ana García",
                recipeId = recipeId,
                content = "Me encantó esta receta. La voy a hacer para mi familia.",
                createdAt = "2023-10-17 18:45:00"
            )
        )

        commentsAdapter.updateComments(sampleComments)
        tvEmptyComments.visibility = View.GONE
    }

    private fun addComment(commentText: String) {
        // Deshabilitar botón mientras se procesa
        btnAddComment.isEnabled = false

        // Crear modelo de comentario
        val comment = CommentRequest(
            userId = userId,
            recipeId = recipeId,
            content = commentText
        )

        // TODO: Reemplazar con llamada a API real
        // Por ahora, simulamos la adición de un comentario
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())

        // Crear el nuevo comentario
        val newComment = Comment(
            id = (100..999).random(), // ID temporal
            userId = userId,
            userName = userName,
            recipeId = recipeId,
            content = commentText,
            createdAt = currentDateTime
        )

        // Añadir el comentario al adaptador
        val currentComments = commentsAdapter.getComments().toMutableList()
        currentComments.add(0, newComment) // Añadir al principio
        commentsAdapter.updateComments(currentComments)

        // Limpiar el campo de texto
        etNewComment.text.clear()

        // Ocultar mensaje de comentarios vacíos si estaba visible
        tvEmptyComments.visibility = View.GONE

        // Hacer scroll hacia arriba para ver el nuevo comentario
        recyclerViewComments.smoothScrollToPosition(0)

        // Habilitar el botón nuevamente
        btnAddComment.isEnabled = true

        Toast.makeText(this, "Comentario añadido", Toast.LENGTH_SHORT).show()
    }

    private fun showEmptyState(message: String) {
        tvEmptyComments.text = message
        tvEmptyComments.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }
}