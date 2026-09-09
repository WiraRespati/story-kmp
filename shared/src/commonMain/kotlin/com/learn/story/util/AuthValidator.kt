package com.learn.story.util

object AuthValidator {
    fun validateName(name: String): String? {
        val trimmed = name.trim()
        return if (trimmed.isEmpty()) "Nama tidak boleh kosong" else null
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return "Email tidak boleh kosong"
        val atIndex = trimmed.indexOf('@')
        val dotIndex = trimmed.lastIndexOf('.')
        val isValid = atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < trimmed.length - 1
        return if (!isValid) "Format email tidak valid" else null
    }

    fun validatePassword(password: String): String? {
        val trimmed = password.trim()
        if (trimmed.isEmpty()) return "Password tidak boleh kosong"
        if (password.length < AppConstants.PASSWORD_MIN_LENGTH) return "Password minimal ${AppConstants.PASSWORD_MIN_LENGTH} karakter"
        return null
    }
}
