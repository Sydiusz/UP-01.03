package com.example.shoeshop.data

import com.example.myfirstproject.data.service.UserManagementService
import com.example.shoeshop.data.service.CategoriesService
import com.example.shoeshop.data.service.FavouriteService
import com.example.shoeshop.data.service.ProductsService
import com.example.shoeshop.data.service.ProfileService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object RetrofitInstance {
    // Базовый URL для всех сервисов Supabase
    const val SUPABASE_URL = "https://yixipuxyofpafnvbaprs.supabase.co"
    const val REST_URL = "$SUPABASE_URL/rest/v1/"

    // Прокси настройки
    private const val PROXY_HOST = "10.207.106.71" // Замените на IP адрес вашего прокси
    private const val PROXY_PORT = 3128           // Замените на порт вашего прокси
    private const val USE_PROXY = false           // Включить/выключить использование прокси

    // Основной клиент для всех запросов
    var client: OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (USE_PROXY) {
                // Настройка HTTP прокси
                proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(PROXY_HOST, PROXY_PORT)))
            }
        }
        .addInterceptor { chain ->
            val original = chain.request()
            val url = original.url.toString()

            val builder = original.newBuilder()
                .header(
                    "apikey",
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlpeGlwdXh5b2ZwYWZudmJhcHJzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NDM3OTMsImV4cCI6MjA4MTQxOTc5M30.-GHt_7WKFHWMzhN9MerHX7a3ZVW_IJDBIDmIxXW5gJ8"
                )
                .header("Content-Type", "application/json")

            if (url.contains("/auth/")) {
                // Для auth-эндпоинтов НЕ подставляем свой Authorization
                // и НЕ удаляем его, если он уже есть
                // просто оставляем apikey
                // ничего больше не делаем
            } else {
                // Для rest/v1/* используем service key как Authorization
                builder.header(
                    "Authorization",
                    "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlpeGlwdXh5b2ZwYWZudmJhcHJzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NDM3OTMsImV4cCI6MjA4MTQxOTc5M30.-GHt_7WKFHWMzhN9MerHX7a3ZVW_IJDBIDmIxXW5gJ8"
                )
            }

            val request = builder.method(original.method, original.body).build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Retrofit для авторизации (использует основной URL)
    private val retrofitAuth = Retrofit.Builder()
        .baseUrl(SUPABASE_URL) // Базовый URL для auth
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    // Retrofit для товаров и категорий (использует REST URL)
    private val retrofitRest = Retrofit.Builder()
        .baseUrl(REST_URL) // Базовый URL для REST API
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    // Сервисы
    val userManagementService = retrofitAuth.create(UserManagementService::class.java)
    val productsService = retrofitRest.create(ProductsService::class.java)
    val categoriesService = retrofitRest.create(CategoriesService::class.java)
    val favouriteService: FavouriteService = retrofitRest.create(FavouriteService::class.java)
    val profileService: ProfileService = retrofitRest.create(ProfileService::class.java)



}