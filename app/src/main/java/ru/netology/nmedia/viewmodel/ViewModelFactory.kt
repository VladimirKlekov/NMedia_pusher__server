//package ru.netology.nmedia.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import ru.netology.nmedia.auth.AppAuth
//import ru.netology.nmedia.di.DependencyContainer
//import ru.netology.nmedia.repository.PostRepository
//
//
///**  интерфейс ViewModelProvider.Factory  нужен что бы передавать значения через "фабрику" - class DependencyContainer
// * **/
//
//class ViewModelFactory(
//    private val repository: PostRepository,
//    private val appAuth: AppAuth
//) : ViewModelProvider.Factory {
//
//
//    //делаю анотацию  @Suppress("UNCHECKED_CAST"), что бы подавить предупреждения (выделение текста as T). Если навести на выделение,
//    // то там будет предупреждение Inspection 'UNCHECKED_CAST' options. В итоге предупреждения исчезнут
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T =
//        when {
//            //булевое значение. Возвращает правду или ложь, если PostViewModel
//            modelClass.isAssignableFrom(PostViewModel::class.java) -> {
//                PostViewModel(repository,appAuth) as T
//            }
//                  //делаю для других viewModel в приложении
//            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
//                //делаю проверкку имееь ли это класс отношение к AuthViewModel. если да, то создам его
//                AuthViewModel(appAuth) as T
//            }
//            //если создастся что-то, что не умеют классы, то выбросится ошибка
//            else -> error("Unknown class: $modelClass")
//        }
//    }
