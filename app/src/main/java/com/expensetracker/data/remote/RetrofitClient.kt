package com.expensetracker.data.remote

// BuildConfig генерируется автоматически Android
import com.expensetracker.data.local.PreferencesManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(private val preferencesManager: PreferencesManager) {
    private val BASE_URL = "https://expense-tracker-api-sbxx.onrender.com"
    
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
        val token = preferencesManager.getAuthTokenSync()
        if (token != null) {
            request.addHeader("Authorization", "Bearer $token")
        }
        request.addHeader("Content-Type", "application/json")
        request.addHeader("User-Agent", "ExpenseTracker/1.0")
        chain.proceed(request.build())
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // В production можно отключить логирование
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    
    fun getImageUrl(path: String): String {
        return if (path.startsWith("http")) {
            path
        } else {
            "$BASE_URL$path"
        }
    }
}

