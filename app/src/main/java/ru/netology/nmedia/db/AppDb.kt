package ru.netology.nmedia.db


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nmedia.dao.Converters
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity

/** --------класс контейнер для зависимостей-----------------------------------------------------**/
/** Сделал контейнер DependencyContainer.
1. Перенес внутреннести функцию private fun buildDatabase(context: Context) в class DependencyContainer
2.Удалил fun getInstance(context: Context): AppDb {
return instance ?: synchronized(this) {
instance ?: buildDatabase(context).also { instance = it }
}
}

 **/
/** ---------------------------------------------------------------------------------------------**/

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var instance: AppDb? = null



    }
}