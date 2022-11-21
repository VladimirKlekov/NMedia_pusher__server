package ru.netology.nmedia.di

import android.content.Context
import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl


class DependencyContainer(
    private val context: Context
) {

    /** --------бизнес логика- репозиторий---------------------------------**/
    /** --------перенес из ApiService и создал из class AppAuth----------------------------------**/
    companion object {

        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"

        @Volatile
        private var instance: DependencyContainer? = null

        //для 2 варианта. инициализация class DependencyContainer
        fun initApp(context: Context) {
            instance = DependencyContainer(context)
        }

        fun getInstance(): DependencyContainer {
            return instance!!
        }

        //для 1 варианта с добавлением context в fun getInstance()
//        fun getInstance(context: Context): DependencyContainer {
//            return instance ?: synchronized(this) {
//                instance ?: DependencyContainer(context).also { instance = it }
//            }
//        }


    }

    private val logging = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    val appAuth = AppAuth(context)

    private val okhttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            appAuth.authStateFlow.value.token?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okhttp)
        .build()


    /** --------перенес в переменную val appBd данные из class AppDb ----------------------------**/
    val appBd = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()


    val apiService = retrofit.create<ApiService>()

    private val postDao = appBd.postDao()
    val repository: PostRepository = PostRepositoryImpl(
        postDao,
        apiService
    )
}
/** --------класс контейнер для зависимостей-----------------------------------------------------**/
/** перечисляю зависимости**/
/**
 * ******************бизнес логика - репозиторий***************************************************
зависимости:
1.  база данных -> Папка ->db, точка входа class AppDb;
- за создание базы данных отвечает функция private fun buildDatabase(context: Context) в abstract class AppDb.
Ее внутренности и буду помещать в контейнер (и удалю ее из class AppDb) в переменную private val appBd;
-теперь потребуется context. Что бы получить доступ к контексту поместим его в констуктор класса;
2.далее делаю переменную private val postDao для доступа к базе данных и оперций над ней;
3.далее создаю репозитоий  val repository, который будет вмешиваться во все классы;
-далее перхожу в PostRepositoryImpl и проверяю (вношу изменения) в конструкторе, нет ли неявных зависимостей ->...
-после изменения добавления ApiService в конструктор PostRepositoryImpl вношу изменения в этом классе
переменной val repository:
 *для этого иду в interface ApiService ->2-> и переношу из него сюда в companion object {объекты BASE_URL,
private val logging, private val okhttp, private val retrofit в class DependencyContainer;
 *private const val BASE_URL перенесем в companion object. Остальное просто добавлю ниже
4. создаю переменную private val apiService = retrofit.create<ApiService>();
5. добавляю переменную private val apiService в конструктор  val repository: PostRepository = PostRepositoryImpl(
6.Далее сламалась в классах вызовы, где было обращение к ApiService. Вношу изменение меняя ссылку с ApiService
на этот класс-контейнер DependencyContainer. Иду в class AppAuth и вношу изменения ->1->...
7. После выбора создания 'DependencyContainer.Companion.getInstance' в class AppAuth для getInstance()
Делаю class DependencyContainer синглтоном. (Класс Singleton - это класс, который определяется
таким образом, что только один экземпляр класса может быть создан и использован везде.). Функция
fun getInstance(): Any автоматом доавится в companion object {. Далее буре тело объекта @Volatile
private val instance из abstract class AppDb и вношу изменения по типам этому классу.
Далее возврращаюсь в class AppAuth что бы заполнить конструктор у функция getInstance()->2->...
8.Что бы не добавлять context в функцию fun getInstance() иницализируем class DependencyContainer
- создаю:
fun initApp(context: Context){
instance = DependencyContainer(context)
}

fun getInstance(): DependencyContainer {
return instance!!
}
}
- удаляю:
fun getInstance(context: Context): DependencyContainer {
//            return instance ?: synchronized(this) {
//                instance ?: DependencyContainer(context).also { instance = it }
//            }
//        }
-далее опять нехера не работает, так как неправильно создали переменную private val apiService. Нужно
убрать private и все заработает.

 *************************** бизнес логика - класс авторизации **************************************
"Выношу" class AppAuth в class DependencyContainer:
1.Создаю переменную для класса авторизации val appAuth = AppAuth.getInstance()
2.Далее переношу данные из companion object { class AppAuth сюда и правлю данные в классе AppAuth ->3->
3.Изменяю только что созданную переменную val appAuth = AppAuth.getInstance() на val appAuth = AppAuth()
4.теперь опять нехера не работает, так как переменную создали не там и не использовали. Переношу
переменную val appAuth = AppAuth()
5.Вношу изменения в private val okhttp = OkHttpClient.Builder()
Было:  AppAuth.getInstance().authStateFlow.value.token?.let { token ->
Стало:  appAuth.authStateFlow.value.token?.let { token ->
6. Нужно инициализировать class DependencyContainer( в class AppAuth. Какая-то игра зависимостей
иницализируется в классе приложения который создается в момент создания приложения.Иду в class NMediaApplication
и меняю инициализацию
Было: AppAuth.initApp(this)
Стало: DependencyContainer.initApp(this)

7. Куча ошибок в class AppActivity AppAuth.getInstance().setAuth. Меняю все на  DependencyContainer.getInstance().appAuth
 **/

/** ---------------------------------------------------------------------------------------------**/
