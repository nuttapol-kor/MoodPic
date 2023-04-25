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

class PostAdapter(
    var documents: QuerySnapshot,
    var mContext: Context,
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val entryView: View = inflater.inflate(R.layout.row_post_item, parent, false)
        val entryViewHolder = PostItemViewHolder(entryView)
        return entryViewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val document = documents.documents.get(position)
        var db = Firebase.firestore
        if (holder is PostItemViewHolder) {
            val postUserId = document.data!!.get("userId") as String
            var postUsername: String? = ""
            var postUserImageUrl: String? = ""
            db.collection("users").whereEqualTo("id", postUserId).get().addOnSuccessListener {
                if (!it.isEmpty) {
                    postUsername = it.documents[0].getString("username")
                    postUserImageUrl = it.documents[0].getString("profileImageUrl")
                    holder.tvShortText.text = document.data!!.get("shortText") as String
                    holder.userCreatedPost.text = postUsername
                    Glide.with(mContext).load(document.data!!.get("imageUrl") as String).into(holder.imgBlogPost)
                    if (postUserImageUrl != null) {
                        Glide.with(mContext).load(postUserImageUrl).apply(RequestOptions.circleCropTransform()).into(holder.imgUserPost)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return documents.size()
    }
}

class PostItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
    var tvShortText: TextView = itemView.findViewById(R.id.rowPostShortText)
    var userCreatedPost: TextView = itemView.findViewById(R.id.rowPostUsername)
    var imgUserPost: ImageView = itemView.findViewById(R.id.rowPostUserImg)
    var imgBlogPost: ImageView = itemView.findViewById(R.id.rowPostBlogImg)
}