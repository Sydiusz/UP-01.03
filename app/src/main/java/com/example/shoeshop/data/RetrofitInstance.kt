package com.example.shoeshop.data

import com.example.myfirstproject.data.service.UserManagementService
import com.example.shoeshop.data.service.CartService
import com.example.shoeshop.data.service.CategoriesService
import com.example.shoeshop.data.service.FavouriteService
import com.example.shoeshop.data.service.OrderItemsReadService
import com.example.shoeshop.data.service.OrderItemsService
import com.example.shoeshop.data.service.OrderItemsWriteService
import com.example.shoeshop.data.service.OrderService
import com.example.shoeshop.data.service.OrderWriteService
import com.example.shoeshop.data.service.OrdersReadService
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

// data/RetrofitInstance.kt
object RetrofitInstance {
    const val SUPABASE_URL = "https://yixipuxyofpafnvbaprs.supabase.co"
    const val REST_URL = "$SUPABASE_URL/rest/v1/"

    private const val PROXY_HOST = "10.207.106.71"
    private const val PROXY_PORT = 3128
    private const val USE_PROXY = false

    var client: OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (USE_PROXY) {
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

            if (!url.contains("/auth/")) {
                builder.header(
                    "Authorization",
                    "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlpeGlwdXh5b2ZwYWZudmJhcHJzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NDM3OTMsImV4cCI6MjA4MTQxOTc5M30.-GHt_7WKFHWMzhN9MerHX7a3ZVW_IJDBIDmIxXW5gJ8"
                )
            }

            chain.proceed(builder.method(original.method, original.body).build())
        }
        // немного меньше connect, побольше read — комфортно для прокси
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(40, TimeUnit.SECONDS)
        .build()

    private val retrofitAuth = Retrofit.Builder()
        .baseUrl(SUPABASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val retrofitRest = Retrofit.Builder()
        .baseUrl(REST_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val userManagementService = retrofitAuth.create(UserManagementService::class.java)
    val productsService = retrofitRest.create(ProductsService::class.java)
    val categoriesService = retrofitRest.create(CategoriesService::class.java)
    val favouriteService: FavouriteService = retrofitRest.create(FavouriteService::class.java)
    val profileService: ProfileService = retrofitRest.create(ProfileService::class.java)
    val cartService: CartService = retrofitRest.create(CartService::class.java)
    val orderService: OrderService = retrofitRest.create(OrderService::class.java)
    val orderItemsService: OrderItemsService =
        retrofitRest.create(OrderItemsService::class.java)
    val ordersReadService: OrdersReadService =
        retrofitRest.create(OrdersReadService::class.java)

    val orderItemsReadService: OrderItemsReadService =
        retrofitRest.create(OrderItemsReadService::class.java)
    val orderWriteService: OrderWriteService = retrofitRest.create(OrderWriteService::class.java)
    val orderItemsWriteService: OrderItemsWriteService = retrofitRest.create(OrderItemsWriteService::class.java)
}
