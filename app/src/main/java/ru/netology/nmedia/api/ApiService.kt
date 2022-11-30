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

/** --------------------------------PAGING 3-----------------------------------------------------**/
/**
1.В данном интерфейсе уже есть функции для получения сообщений с сервера. Теперь применю пагинацию
и буду запрашивать столько страниц, сколько нужно;
 2.Создам нужные функции:
 - метод для начальной загрузки данных. Он возьмет последню страницу с сервера
@GET("posts/latest")
suspend fun getLatest(): Response<List<Post>>
- метод для получения постов, которые пришли уже после полученных...
 * */


interface ApiService {
    /** --------------------------------PAGING 3-------------------------------------------------**/
    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count :Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count :Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count :Int): Response<List<Post>>
    /** --------------------------------PAGING 3-------------------------------------------------**/
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

