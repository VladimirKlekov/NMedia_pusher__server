package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Post
import java.io.IOException

class PostPagingSource(
    private val apiService: ApiService
) : PagingSource<Long, Post>() {

    //используется, что бы мспользовать какой-то ключ при обновлении данных
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {
            val result = when (params) {
                is LoadParams -> {
                    //loadSize - количество загружаемых страниц
                    apiService.getLatest(params.loadSize)
                }
                //прокрутка внииз
                is LoadParams.Append -> {
                    apiService.getBefore(id = params.key, count = params.loadSize)
                }
                //прокрутка вверх
                is LoadParams.Prepend ->
                    return LoadResult.Page(
                        //заполнив таким образом, тогда событие больше не вернется. Отключили фиу, когда
                        //пользователь скролит вверх, у него не будут загружаться новые траницы
                        data = emptyList(), nextKey = null, prevKey = params.key
                    )
            }
            if (!result.isSuccessful) {
                throw HttpException(result)
            }
            val data = result.body().orEmpty()
            return LoadResult.Page(data = data, prevKey = params.key, data.lastOrNull()?.id)
        } catch (e: IOException) {
            return LoadResult.Error(e)

        }
    }
}
