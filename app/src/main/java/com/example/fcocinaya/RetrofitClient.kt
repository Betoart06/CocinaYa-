import android.content.SharedPreferences
import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.fcocinaya.ApiService

object RetrofitClient {
    private const val BASE_URL = "https://cocinaya.onrender.com" // Asegúrate de tener esta constante definida
    private const val PREF_TOKEN = "token" // Asegúrate de tener esta constante definida
    private const val PREF_USER_ID = "user_id" // Asegúrate de tener esta constante definida
    private const val PREF_USER_NAME = "user_name"
    private const val PREF_USER_EMAIL = "user_email"

    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null
    private var sharedPreferences: SharedPreferences? = null

    fun initialize(context: Context) {
        // Inicializar SharedPreferences
        sharedPreferences = context.getSharedPreferences("FCocinaYa", Context.MODE_PRIVATE)

        // Configurar OkHttpClient con interceptor para logs
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Crear un interceptor para añadir el token de autorización
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()

            // Obtener el token almacenado
            val token = sharedPreferences?.getString(PREF_TOKEN, "")

            // Si existe un token, añadirlo a la cabecera de la solicitud
            val requestBuilder = if (!token.isNullOrEmpty()) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .method(originalRequest.method, originalRequest.body)
            } else {
                originalRequest.newBuilder()
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor) // Añadir el interceptor de autenticación
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Construir Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Crear instancia de la API
        apiService = retrofit?.create(ApiService::class.java)
    }

    fun getApiService(): ApiService {
        return apiService ?: throw IllegalStateException("RetrofitClient no ha sido inicializado. Llama a initialize() primero.")
    }

    // Métodos para gestionar la sesión de usuario
    fun saveUserSession(token: String, userId: Int, userName: String, email: String) {
        sharedPreferences?.edit()?.apply {
            putString(PREF_TOKEN, token)
            putInt(PREF_USER_ID, userId)
            putString(PREF_USER_NAME, userName)
            putString(PREF_USER_EMAIL, email)
            apply()
        }
    }

    fun clearUserSession() {
        sharedPreferences?.edit()?.apply {
            remove(PREF_TOKEN)
            remove(PREF_USER_ID)
            remove(PREF_USER_NAME)
            remove(PREF_USER_EMAIL)
            apply()
        }
    }

    fun getUserId(): Int {
        return sharedPreferences?.getInt(PREF_USER_ID, -1) ?: -1
    }

    fun getUserName(): String {
        return sharedPreferences?.getString(PREF_USER_NAME, "") ?: ""
    }

    fun getUserEmail(): String {
        return sharedPreferences?.getString(PREF_USER_EMAIL, "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        return !sharedPreferences?.getString(PREF_TOKEN, "").isNullOrEmpty()
    }
}