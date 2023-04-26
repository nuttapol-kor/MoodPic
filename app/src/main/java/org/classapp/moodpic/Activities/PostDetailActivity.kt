package org.classapp.moodpic.Activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.classapp.moodpic.R

class PostDetailActivity : AppCompatActivity() {

    private lateinit var imgPost: ImageView
    private lateinit var imgUserPost: ImageView
    private lateinit var imgCurrentUser: ImageView
    private lateinit var txtPostShortText: TextView
    private lateinit var txtPostUsername: TextView
    private lateinit var editTextComment: EditText
    private lateinit var addCommentBtn: ImageView
    private lateinit var happyBtn: ImageView
    private lateinit var funnyBtn: ImageView
    private lateinit var sadBtn: ImageView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        imgPost = findViewById(R.id.postDetailImg)
        imgUserPost = findViewById(R.id.imageView2)
        imgCurrentUser = findViewById(R.id.postDetailUserImg)

        txtPostShortText = findViewById(R.id.postDetailShortText)
        txtPostUsername = findViewById(R.id.postDetailUsername)

        editTextComment = findViewById(R.id.postDetailComment)

        addCommentBtn = findViewById(R.id.postDetailSendCommentBtn)
        happyBtn = findViewById(R.id.postDetailHappyBtn)
        funnyBtn = findViewById(R.id.postDetailFunnyBtn)
        sadBtn = findViewById(R.id.postDetailSadBtn)

        val postImage = intent.extras?.getString("imgPost")
        val postedUserImage = intent.extras?.getString("imgUserPost")
        val currentUserImage = intent.extras?.getString("imgCurrentUser")
        val postShortText = intent.extras?.getString("txtPostShortText")
        val postUsername = intent.extras?.getString("txtPostUsername")
        val postCreateAt = intent.extras?.getString("postCreateAt")

        Glide.with(this).load(postImage).into(imgPost)
        if (postedUserImage != null) {
            Glide.with(this).load(postedUserImage).apply(RequestOptions.circleCropTransform()).into(imgUserPost)
        }
        if (currentUserImage != null) {
            Glide.with(this).load(currentUserImage).apply(RequestOptions.circleCropTransform()).into(imgCurrentUser)
        }
        txtPostShortText.text = postShortText
        txtPostUsername.text = "$postUsername posted at $postCreateAt"




    }
}