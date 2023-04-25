package org.classapp.moodpic.Activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.classapp.moodpic.Models.User
import org.classapp.moodpic.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val userName = findViewById<EditText>(R.id.registName)
        val userEmail = findViewById<EditText>(R.id.registMail)
        val userPassword = findViewById<EditText>(R.id.resgistPassword)
        val userConfirmPassword = findViewById<EditText>(R.id.registPassword2)
        val loadingProgress = findViewById<ProgressBar>(R.id.progressBar)
        val registBtn = findViewById<Button>(R.id.regBtn)
        val signInOrNot = findViewById<TextView>(R.id.signInBtn)

        signInOrNot.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        auth = Firebase.auth
        storage = Firebase.storage

        registBtn.setOnClickListener {
            registBtn.visibility = View.INVISIBLE
            loadingProgress.visibility = View.VISIBLE
            val name = userName.text.toString()
            val email = userEmail.text.toString()
            val pwd = userPassword.text.toString()
            val confirmPwd = userConfirmPassword.text.toString()
            if (name.isEmpty() || email.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
                registBtn.visibility = View.VISIBLE
                loadingProgress.visibility = View.INVISIBLE
            } else if (!pwd.equals(confirmPwd)) {
                Toast.makeText(this, "Your password and confirm password are not equal", Toast.LENGTH_LONG).show()
                registBtn.visibility = View.VISIBLE
                loadingProgress.visibility = View.INVISIBLE
            } else {
                createUserAccount(name, email, pwd)
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "Permission denied. Please grant the permission to access the gallery.", Toast.LENGTH_LONG).show()
            }
        }

        val userImg = findViewById<ImageView>(R.id.registUserImage)

        userImg.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 22) {
                checkAndRequestForPermission();
            } else {
                openGallery()
            }
        }
    }

    private fun createUserAccount(username: String, email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        updateUserInfo(username, selectedImageUri, user)
                    }
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUserInfo(username: String, imageUri: Uri?, currentUser: FirebaseUser) {
        var storageRef = storage.reference.child("user_images")
        val imageRef = storageRef.child("${imageUri?.lastPathSegment}")
        val db = Firebase.firestore
        if (imageUri == null) {
            val no_image_user = hashMapOf(
                "userId" to currentUser.uid,
                "username" to username,
                "email" to currentUser.email,
                "profile_image" to null
            )
            val noImageUser = User(
                id = currentUser.uid,
                username = username,
                email = currentUser.email,
                profileImageUrl = null
            )
            db.collection("users").add(noImageUser.toMap()).addOnSuccessListener {
                Toast.makeText(this, "Register Complete!!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }.addOnFailureListener {
                Toast.makeText(this, "Cannot update user info to Firestore", Toast.LENGTH_LONG).show()
            }
        }
        imageUri?.let {
            imageRef.putFile(it).addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val user = User(
                            id = currentUser.uid,
                            username = username,
                            email = currentUser.email,
                            profileImageUrl = uri.toString()
                        )
                        db.collection("users").add(user.toMap()).addOnSuccessListener {
                            Toast.makeText(this, "Register Complete!!", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }.addOnFailureListener {
                            Toast.makeText(this, "Cannot update user info to Firestore", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

    }

    private fun checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission using the ActivityResultLauncher
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data

            // display the selected image in the ImageView
            val userImg = findViewById<ImageView>(R.id.registUserImage)
            userImg.setImageURI(selectedImageUri)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 100
    }
}