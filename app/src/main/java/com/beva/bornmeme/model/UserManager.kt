package com.beva.bornmeme.model

object UserManager {
    var user: User = User(userId = null)

    val isLoggedIn: Boolean
        get() = user.userId != null
}