package com.example.shoeshop.data
import com.example.myfirstproject.data.service.UserManagementService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy

object RetrofitInstance {
    const val SUBABASE_URL = "https://uiimljinntccrtguekxm.supabase.co"

    var client: OkHttpClient = OkHttpClient.Builder().build()

    private  val retrofit = Retrofit.Builder()
        .baseUrl(SUBABASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
    val userManagementService = retrofit.create(UserManagementService::class.java)
}