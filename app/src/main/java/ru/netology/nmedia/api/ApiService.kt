package ru.netology.nmedia.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken

/** --------класс контейнер для зависимостей-----------------------------------------------------**/
/** 1. После внесения изменений в конструктор в PostRepositoryImpl удаляю object Api {
val service: ApiService by lazy {
retrofit.create(ApiService::class.java)
}
}
 ->возвращаюсь в PostRepositoryImpl для проверки ошибок
 2. Переношу обхекты BASE_URL, private val logging, private val okhttp, private val retrofit в class DependencyContainer ->...
 **/
/** ---------------------------------------------------------------------------------------------**/

interface ApiService {


    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body pushToken: PushToken): Response<Unit>

    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>
}

