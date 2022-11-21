package ru.netology.nmedia.auth

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.dto.PushToken


/** --------класс контейнер для зависимостей-----------------------------------------------------**/
/** 1.После изменения в ApiService необходимо изменить зависимости в функция на класс-контейнер class DependencyContainer
-  в  fun sendPushToken( меняю Api.service.sendPushToken(pushToken) на DependencyContainer.getInstance().apiService.sendPushToken(pushToken),
но!!! getInstance() не сработает. что бы зарабатола нодо чтобы class DependencyContainer "ожил" в
рамках всего преокта. Для этого сделаю синглтоном (Класс Singleton - это класс, который определяется
таким образом, что только один экземпляр класса может быть создан и использован везде.) Для этого нажму
на getInstance()  и выберу Create member function 'DependencyContainer.Companion.getInstance'. Далее
внесу изменения в class DependencyContainer->7->....
2.После изменений в class DependencyContainer нужно добавить context в конструктор getInstance().
Есть два варианта: 1) Добавить везде context. 2) Сделать специальную функциюв class DependencyContainer, которая один раз инициализирует
class DependencyContainer и context нигде добавлять не придется. Делаю по второму варианту. Возварщаюсь в
в class DependencyContainer->8->....
3. Убиарю context в конструкторе. Было: class AppAuth private constructor(context: Context).
Стало:class AppAuth (context: Context). -> class DependencyContainer 3->....
 **/
/** ---------------------------------------------------------------------------------------------**/

class AppAuth(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"

    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token))
        }

        sendPushToken()
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            apply()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            apply()
        }
        sendPushToken()
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: FirebaseMessaging.getInstance().token.await())
                //val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                DependencyContainer.getInstance().apiService.sendPushToken(pushToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}

data class AuthState(val id: Long = 0, val token: String? = null)

//companion object {
//        @Volatile
//        private var instance: AppAuth? = null
//
//        fun getInstance(): AppAuth = synchronized(this) {
//            instance ?: throw IllegalStateException(
//                "AppAuth is not initialized, you must call AppAuth.initializeApp(Context context) first."
//            )
//        }
//
//        fun initApp(context: Context): AppAuth = instance ?: synchronized(this) {
//            instance ?: buildAuth(context).also { instance = it }
//        }
//
//        private fun buildAuth(context: Context): AppAuth = AppAuth(context)
//    }