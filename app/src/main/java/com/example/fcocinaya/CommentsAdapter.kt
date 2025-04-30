package com.example.fcocinaya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class CommentsAdapter(
    private var comments: List<Comment>
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        val tvTimeAgo: TextView = itemView.findViewById(R.id.tvTimeAgo)
        val tvUserInitial: TextView = itemView.findViewById(R.id.tvUserInitial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comments, parent, false)
        return CommentViewHolder(view)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        // Configurar datos del comentario
        holder.tvUserName.text = comment.userName
        holder.tvComment.text = comment.content
        holder.tvTimeAgo.text = getTimeAgo(comment.createdAt)

        // Configurar inicial del usuario
        if (comment.userName.isNotEmpty()) {
            holder.tvUserInitial.text = comment.userName.first().toString().uppercase()
        } else {
            holder.tvUserInitial.text = "U"
        }
    }

    // Actualizar la lista de comentarios
    fun updateComments(newComments: List<Comment>) {
        this.comments = newComments
        notifyDataSetChanged()
    }

    // Obtener la lista actual de comentarios
    fun getComments(): List<Comment> = comments

    // Calcular tiempo relativo (hace X minutos, horas, etc.)
    private fun getTimeAgo(dateTimeString: String): String {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getDefault()

            val past = format.parse(dateTimeString) ?: return "hace un momento"
            val now = Date()

            val timeInMillis = now.time - past.time

            return when {
                timeInMillis < TimeUnit.MINUTES.toMillis(1) -> "hace un momento"
                timeInMillis < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
                    "hace $minutes ${if (minutes == 1L) "minuto" else "minutos"}"
                }
                timeInMillis < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
                    "hace $hours ${if (hours == 1L) "hora" else "horas"}"
                }
                timeInMillis < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(timeInMillis)
                    "hace $days ${if (days == 1L) "día" else "días"}"
                }
                else -> {
                    val simpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    simpleDateFormat.format(past)
                }
            }
        } catch (e: ParseException) {
            return dateTimeString
        }
    }
}