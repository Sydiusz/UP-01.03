package com.example.shoeshop.data.service

import com.example.shoeshop.data.model.Category
import com.example.shoeshop.data.model.Product
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductsService {

    @GET("products")
    suspend fun getProducts(
        @Query("select") select: String = "*",
        @Query("order") order: String = "title.asc"  // Измените на существующее поле
    ): Response<List<Product>>

    @GET("products")
    suspend fun getProductsByCategory(
        @Query("select") select: String = "*",
        @Query("category_id") categoryId: String,  // УБЕРИТЕ значение по умолчанию!
        @Query("order") order: String = "title.asc"
    ): Response<List<Product>>
    @GET("products")
    suspend fun getProductById(
        @Query("id") idFilter: String,          // "eq.<uuid>"
        @Query("select") select: String = "*",
        @Query("limit") limit: Int = 1
    ): Response<List<Product>>
    @GET("products")
    suspend fun getProductsByIds(
        @Query("id") idsFilter: String, // "in.(id1,id2,...)"
        @Query("select") select: String = "*"
    ): Response<List<Product>>
}

interface CategoriesService {

    @GET("categories")
    suspend fun getCategories(
        @Query("select") select: String = "*",
        @Query("order") order: String = "title.asc"
    ): Response<List<Category>>
}