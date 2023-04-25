package org.classapp.moodpic.Activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.classapp.moodpic.R

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        // set current user
        auth = Firebase.auth
        db = Firebase.firestore
        var user: FirebaseUser? = auth.currentUser
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val headerUsername = headerView.findViewById<TextView>(R.id.navHeaderUsername)
        val headerEmail = headerView.findViewById<TextView>(R.id.navHeaderEmail)
        val profileImage = headerView.findViewById<ImageView>(R.id.navHeaderImage)
        if (user != null) {
            var userId = user.uid
            db.collection("users").whereEqualTo("id", userId).get().addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    headerUsername.text = documents.documents[0].getString("username")
                    headerEmail.text = documents.documents[0].getString("email")
                    var imageUrl = documents.documents[0].getString("profileImageUrl")
                    if (imageUrl != null) {
                        Glide.with(this).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(profileImage)
                    }
                }
            }.addOnFailureListener{ exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        }

        // popup dialog
//        popupAddPost.setContentView(R.layout.popup_add_post)
//        popupAddPost.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        popupAddPost.window?.setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT)
//        popupAddPost.window?.attributes?.gravity = Gravity.TOP
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_profile -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
            R.id.nav_logout -> logout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}