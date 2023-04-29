package org.classapp.moodpic.Models

import com.google.firebase.firestore.FieldValue

data class Comment (
    var commentText: String,
    var postId: String,
    var userId: String,
    var createAt: FieldValue
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "commentText" to commentText,
            "postId" to postId,
            "userId" to userId,
            "createAt" to createAt
        )
    }
}
