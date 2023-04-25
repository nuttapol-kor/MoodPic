package org.classapp.moodpic.Models

data class User(
    var id: String,
    var username: String,
    var email: String,
    var profileImageUrl: String
) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "id" to id,
            "username" to username,
            "email" to email,
            "profileImageUrl" to profileImageUrl
        )
    }
}
