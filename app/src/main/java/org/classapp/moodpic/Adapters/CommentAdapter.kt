package org.classapp.moodpic.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.classapp.moodpic.R

class CommentAdapter(
    var documents: QuerySnapshot,
    var mContext: Context,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val entryView: View = inflater.inflate(R.layout.row_comment, parent, false)
        val entryViewHolder = CommentViewHolder(entryView)
        return entryViewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val document = documents.documents.get(position)
        var db = Firebase.firestore
        var commentUserId: String? = ""
        if (holder is CommentViewHolder) {
            holder.commentText.text = document.data!!.get("commentText") as String
            commentUserId = document.data!!.get("userId") as String
            db.collection("users").whereEqualTo("id", commentUserId).get().addOnSuccessListener {
                if (!it.isEmpty) {
                    var userCommentImageProfile: String? = ""
                    holder.commentUsername.text = it.documents[0].getString("username")
                    userCommentImageProfile = it.documents[0].getString("profileImageUrl")
                    if (userCommentImageProfile != null) {
                        Glide.with(mContext).load(userCommentImageProfile).apply(RequestOptions.circleCropTransform()).into(holder.commentImage)
                    }
                }
            }.addOnFailureListener{ exception -> Toast.makeText(mContext, exception.message, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun getItemCount(): Int {
        return documents.size()
    }
}

class CommentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var commentImage: ImageView = itemView.findViewById(R.id.commentUserImg)
    var commentUsername: TextView = itemView.findViewById(R.id.commentUsername)
    var commentText: TextView = itemView.findViewById(R.id.commentText)
}