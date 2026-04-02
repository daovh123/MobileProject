package com.example.mobileproject.domain.entity

enum class PostLoginDestination {
    PROFILE,
    HOME,
}

fun AuthSession.resolvePostLoginDestination(): PostLoginDestination {
    return when {
        !profileCompleted -> PostLoginDestination.PROFILE
        else -> PostLoginDestination.HOME
    }
}
