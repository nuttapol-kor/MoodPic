package org.classapp.moodpic.Models

import com.google.firebase.firestore.FieldValue

data class Post (
    var shortText: String,
    var imageUrl: String,
    var userId: String,
    var createAt: FieldValue
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "shortText" to shortText,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "createAt" to createAt
        )
    }
}