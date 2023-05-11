package org.classapp.moodpic.Models

import com.google.firebase.firestore.FieldValue

data class Emotion(
    var emotionType: String,
    var postId: String,
    var userId: String,
    var createAt: FieldValue
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "emotionType" to emotionType,
            "postId" to postId,
            "userId" to userId,
            "createAt" to createAt
        )
    }
}
