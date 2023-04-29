package org.classapp.moodpic.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.classapp.moodpic.Activities.PostDetailActivity
import org.classapp.moodpic.R
import java.text.SimpleDateFormat

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
        var auth = Firebase.auth
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
                holder.itemView.setOnClickListener{
                    val intent = Intent(mContext, PostDetailActivity::class.java)
                    intent.putExtra("txtPostShortText", document.data!!.get("shortText") as String)
                    intent.putExtra("imgPost", document.data!!.get("imageUrl") as String)
                    intent.putExtra("txtPostUsername", postUsername)
                    intent.putExtra("imgUserPost", postUserImageUrl)
                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    intent.putExtra("postCreateAt", sdf.format((document.data!!.get("createAt") as Timestamp).toDate()))
                    intent.putExtra("postId", document.id)
                    db.collection("users").whereEqualTo("email", auth.currentUser?.email).get().addOnSuccessListener { doc2 ->
                        intent.putExtra("imgCurrentUser", doc2.documents[0].getString("profileImageUrl"))
                        mContext.startActivity(intent)
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