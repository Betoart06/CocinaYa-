package com.example.fcocinaya

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

/**
 * Diálogo para filtrar recetas por diferentes criterios
 */
class RecipesFilterDialog(
    context: Context,
    private val onApplyFilters: (FilterCriteria) -> Unit
) : Dialog(context) {

    // Clase para encapsular los criterios de filtrado
    data class FilterCriteria(
        val difficulty: String = "Todas", // Fácil, Media, Difícil, Todas
        val time: String = "Todos", // <15min, <30min, <60min, Todos
        val ingredients: List<String> = emptyList(),
        val onlyFavorites: Boolean = false,
        val onlyVegetarian: Boolean = false,
        val onlyVegan: Boolean = false,
        val onlyGlutenFree: Boolean = false
    )

    private val availableIngredients = listOf(
        "Huevos", "Leche", "Harina", "Tomate", "Cebolla",
        "Ajo", "Queso", "Pollo", "Carne", "Pescado",
        "Arroz", "Pasta", "Frijoles", "Papa", "Zanahoria"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Quitar el título del diálogo
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Establecer el layout del diálogo
        setContentView(R.layout.dialog_filter_recipes)

        // Referencias a las vistas
        val radioGroupDifficulty = findViewById<RadioGroup>(R.id.radioGroupDifficulty)
        val radioGroupTime = findViewById<RadioGroup>(R.id.radioGroupTime)
        val ingredientsContainer = findViewById<LinearLayout>(R.id.ingredientsContainer)
        val checkBoxFavorites = findViewById<CheckBox>(R.id.checkBoxFavorites)
        val checkBoxVegetarian = findViewById<CheckBox>(R.id.checkBoxVegetarian)
        val checkBoxVegan = findViewById<CheckBox>(R.id.checkBoxVegan)
        val checkBoxGlutenFree = findViewById<CheckBox>(R.id.checkBoxGlutenFree)
        val btnApplyFilters = findViewById<Button>(R.id.btnApplyFilters)
        val btnCancelFilters = findViewById<Button>(R.id.btnCancelFilters)
        val btnResetFilters = findViewById<Button>(R.id.btnResetFilters)

        // Poblar la lista de ingredientes
        for (ingredient in availableIngredients) {
            val checkBox = CheckBox(context)
            checkBox.text = ingredient
            checkBox.textSize = 14f
            checkBox.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.blanco))

            // Añadir el CheckBox al contenedor
            ingredientsContainer.addView(checkBox)
        }

        // Configurar botón de aplicar filtros
        btnApplyFilters.setOnClickListener {
            // Recopilar criterios de filtrado
            val selectedDifficultyId = radioGroupDifficulty.checkedRadioButtonId
            val selectedTimeId = radioGroupTime.checkedRadioButtonId

            // Obtener dificultad seleccionada
            val difficultyText = if (selectedDifficultyId != -1) {
                findViewById<RadioButton>(selectedDifficultyId).text.toString()
            } else {
                "Todas"
            }

            // Obtener tiempo seleccionado
            val timeText = if (selectedTimeId != -1) {
                findViewById<RadioButton>(selectedTimeId).text.toString()
            } else {
                "Todos"
            }

            // Recopilar ingredientes seleccionados
            val selectedIngredients = mutableListOf<String>()
            for (i in 0 until ingredientsContainer.childCount) {
                val child = ingredientsContainer.getChildAt(i)
                if (child is CheckBox && child.isChecked) {
                    selectedIngredients.add(child.text.toString())
                }
            }

            // Crear objeto de criterios de filtrado
            val criteria = FilterCriteria(
                difficulty = difficultyText,
                time = timeText,
                ingredients = selectedIngredients,
                onlyFavorites = checkBoxFavorites.isChecked,
                onlyVegetarian = checkBoxVegetarian.isChecked,
                onlyVegan = checkBoxVegan.isChecked,
                onlyGlutenFree = checkBoxGlutenFree.isChecked
            )

            // Llamar al callback con los criterios
            onApplyFilters(criteria)

            // Cerrar el diálogo
            dismiss()
        }

        // Configurar botón de cancelar
        btnCancelFilters.setOnClickListener {
            dismiss()
        }

        // Configurar botón de resetear filtros
        btnResetFilters.setOnClickListener {
            // Resetear selecciones
            radioGroupDifficulty.clearCheck()
            radioGroupTime.clearCheck()

            // Desmarcar todos los ingredientes
            for (i in 0 until ingredientsContainer.childCount) {
                val child = ingredientsContainer.getChildAt(i)
                if (child is CheckBox) {
                    child.isChecked = false
                }
            }

            // Desmarcar opciones adicionales
            checkBoxFavorites.isChecked = false
            checkBoxVegetarian.isChecked = false
            checkBoxVegan.isChecked = false
            checkBoxGlutenFree.isChecked = false
        }
    }
}