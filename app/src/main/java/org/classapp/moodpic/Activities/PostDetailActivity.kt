package org.classapp.moodpic.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.classapp.moodpic.Adapters.CommentAdapter
import org.classapp.moodpic.Models.Comment
import org.classapp.moodpic.Models.Emotion
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

    private var commentRecyclerView: RecyclerView? = null
    private var commentAdapter: CommentAdapter? = null
    private var commentLayoutManager: RecyclerView.LayoutManager? = null

    private lateinit var happyValTxt: TextView
    private lateinit var funnyValTxt: TextView
    private lateinit var sadValTxt: TextView

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

        happyValTxt = findViewById(R.id.happyVal)
        funnyValTxt = findViewById(R.id.funnyVal)
        sadValTxt = findViewById(R.id.sadVal)

        val postImage = intent.extras?.getString("imgPost")
        val postedUserImage = intent.extras?.getString("imgUserPost")
        val currentUserImage = intent.extras?.getString("imgCurrentUser")
        val postShortText = intent.extras?.getString("txtPostShortText")
        val postUsername = intent.extras?.getString("txtPostUsername")
        val postCreateAt = intent.extras?.getString("postCreateAt")
        val postId = intent.extras?.getString("postId")

        var auth = Firebase.auth
        var db = Firebase.firestore

        Glide.with(this).load(postImage).into(imgPost)
        if (postedUserImage != null) {
            Glide.with(this).load(postedUserImage).apply(RequestOptions.circleCropTransform()).into(imgUserPost)
        }
        if (currentUserImage != null) {
            Glide.with(this).load(currentUserImage).apply(RequestOptions.circleCropTransform()).into(imgCurrentUser)
        }
        txtPostShortText.text = postShortText
        txtPostUsername.text = "$postUsername posted at $postCreateAt"

        addCommentBtn.setOnClickListener {
            val commentText = editTextComment.text.toString()
            val userId = auth.currentUser?.uid
            val commentCreateAt = FieldValue.serverTimestamp()
            val commentObj = Comment(
                commentText = commentText,
                postId = postId!!,
                userId = userId!!,
                createAt = commentCreateAt
            )
            db.collection("comments").add(commentObj.toMap()).addOnSuccessListener {
                editTextComment.text.clear()
                getCommentFromFirebase(postId!!)
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }

        commentRecyclerView = findViewById(R.id.commentRV)
        commentRecyclerView?.setHasFixedSize(true)
        commentLayoutManager = LinearLayoutManager(this)
        commentRecyclerView?.layoutManager = commentLayoutManager

        getCommentFromFirebase(postId!!)

        // set emotion button
        happyBtn.setOnClickListener {
            val userId = auth.currentUser?.uid
            setEmotionType("happy", postId!!, userId!!)
            getEmotionFromFirebase(postId!!)
        }
        funnyBtn.setOnClickListener {
            val userId = auth.currentUser?.uid
            setEmotionType("funny", postId!!, userId!!)
            getEmotionFromFirebase(postId!!)
        }
        sadBtn.setOnClickListener {
            val userId = auth.currentUser?.uid
            setEmotionType("sad", postId!!, userId!!)
            getEmotionFromFirebase(postId!!)
        }
        getEmotionFromFirebase(postId!!)
    }

    private fun getCommentFromFirebase(postId: String) {
        val db = Firebase.firestore
        db.collection("comments").whereEqualTo("postId", postId).orderBy("createAt", Query.Direction.DESCENDING).get().addOnSuccessListener {
            if (!it.isEmpty) {
                commentAdapter = CommentAdapter(it, this)
                commentRecyclerView?.adapter = commentAdapter
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            Log.e("getCommentFromFirebase", "Error getting comments", exception)
        }
    }

    private fun getEmotionFromFirebase(postId: String) {
        val db = Firebase.firestore
        db.collection("emotions").whereEqualTo("postId", postId).get().addOnSuccessListener {
            var happyCount = 0
            var funnyCount = 0
            var sadCount = 0
            if (!it.isEmpty) {
                for (document in it) {
                    val emotionType = document.data.get("emotionType") as String
                    if (emotionType.equals("happy")) {
                        happyCount++
                    } else if (emotionType.equals("funny")) {
                        funnyCount++
                    } else if (emotionType.equals("sad")) {
                        sadCount++
                    }
                }
            }
            happyValTxt.text = happyCount.toString()
            funnyValTxt.text = funnyCount.toString()
            sadValTxt.text = sadCount.toString()
        }.addOnFailureListener { exception ->
            Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            Log.e("getCommentFromFirebase", "Error getting comments", exception)
        }
    }

    private fun setEmotionType(emotionType: String, postId: String, userId: String) {
        var db = Firebase.firestore
        val emotionCreateAt = FieldValue.serverTimestamp()
        val emotionObj = Emotion(
            emotionType = emotionType,
            postId = postId!!,
            userId = userId!!,
            createAt = emotionCreateAt
        )
        db.collection("emotions").add(emotionObj.toMap()).addOnSuccessListener {

        }.addOnFailureListener { exception ->
            Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
        }
    }
}