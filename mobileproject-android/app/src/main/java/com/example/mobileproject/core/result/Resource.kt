package com.example.mobileproject.core.result

/**
 * A generic sealed interface that represents the three possible states of an asynchronous
 * data operation (typically a network or database call). Used in ViewModels and UI layers
 * to reactively handle loading, success, and error states without nullability.
 *
 * Usage in a ViewModel:
 * ```
 * val state: StateFlow<Resource<User>> = repository.getUser()
 *     .map { Resource.Success(it) }
 *     .catch { emit(Resource.Error(it)) }
 *     .stateIn(scope, SharingStarted.WhileSubscribed(5000), Resource.Loading)
 * ```
 *
 * @param T the type of data held on success; covariant so Resource<SubType> is assignable to Resource<SuperType>.
 */
sealed interface Resource<out T> {

    /** Operation completed successfully; [data] contains the result. */
    data class Success<T>(val data: T) : Resource<T>

    /** Operation failed; [throwable] holds the original exception for logging or display. */
    data class Error(val throwable: Throwable) : Resource<Nothing>

    /** Operation is in progress; no data available yet. */
    data object Loading : Resource<Nothing>
}
