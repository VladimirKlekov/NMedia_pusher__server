package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.di.DependencyContainer

class AuthViewModel : ViewModel() {
    val data: LiveData<AuthState> =  DependencyContainer.getInstance().appAuth
        .authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() =  DependencyContainer.getInstance().appAuth.authStateFlow.value.id != 0L
}