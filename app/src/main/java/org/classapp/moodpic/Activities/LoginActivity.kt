package org.classapp.moodpic.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.classapp.moodpic.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginBtn: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        val userEmail = findViewById<EditText>(R.id.loginEmail)
        val userPassword = findViewById<EditText>(R.id.loginPassword)
        loginBtn = findViewById<Button>(R.id.loginBtn)
        progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        val toReg = findViewById<TextView>(R.id.createAccBtn)

        toReg.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            loginBtn.visibility = View.INVISIBLE
            val email = userEmail.text.toString()
            val pwd = userPassword.text.toString()

            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
            } else {
                signIn(email, pwd)
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                progressBar.visibility = View.INVISIBLE
                loginBtn.visibility = View.VISIBLE
                Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                progressBar.visibility = View.INVISIBLE
                loginBtn.visibility = View.VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        var user: FirebaseUser? = auth.currentUser
        if (user != null) {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}