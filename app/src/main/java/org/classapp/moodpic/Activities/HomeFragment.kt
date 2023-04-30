package org.classapp.moodpic.Activities

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.classapp.moodpic.Adapters.PostAdapter
import org.classapp.moodpic.Models.Post
import org.classapp.moodpic.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var popupAddPost: Dialog
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private var postRecyclerView: RecyclerView? = null
    private var postAdapter: PostAdapter? = null
    private var postLayoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(requireActivity(), "Permission denied. Please grant the permission to access the gallery.", Toast.LENGTH_LONG).show()
            }
        }
        storage = Firebase.storage
        auth = Firebase.auth
    }

    private fun initPopup() {
        popupAddPost = Dialog(requireActivity())
        popupAddPost.setContentView(R.layout.popup_add_post)
        popupAddPost.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupAddPost.window?.setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT)
        popupAddPost.window?.attributes?.gravity = Gravity.TOP

        val createPostBtn = popupAddPost.findViewById<ImageView>(R.id.popupCreateBtn)
        val createProgressBar = popupAddPost.findViewById<ProgressBar>(R.id.popupProgressBar)
        val shortTextPost = popupAddPost.findViewById<EditText>(R.id.popupShortText)

        createPostBtn.setOnClickListener {
            createPostBtn.visibility = View.INVISIBLE
            createProgressBar.visibility = View.VISIBLE

            if (shortTextPost.text.toString().isEmpty()) {
                Toast.makeText(requireActivity(), "Please describe your image", Toast.LENGTH_SHORT).show()
                createPostBtn.visibility = View.VISIBLE
                createProgressBar.visibility = View.INVISIBLE
            } else if (shortTextPost.text.toString().length >= 50) {
                Toast.makeText(requireActivity(), "Please give a short description", Toast.LENGTH_SHORT).show()
                createPostBtn.visibility = View.VISIBLE
                createProgressBar.visibility = View.INVISIBLE
            } else if (selectedImageUri == null) {
                Toast.makeText(requireActivity(), "Please upload the image", Toast.LENGTH_SHORT).show()
                createPostBtn.visibility = View.VISIBLE
                createProgressBar.visibility = View.INVISIBLE
            } else {
                var storageRef = storage.reference.child("blog_images")
                val imageRef = storageRef.child("${selectedImageUri?.lastPathSegment}")
                var user: FirebaseUser? = auth.currentUser
                val db = Firebase.firestore
                imageRef.putFile(selectedImageUri!!).addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val post = user?.let { it ->
                                Post(
                                    shortText = shortTextPost.text.toString(),
                                    imageUrl = uri.toString(),
                                    userId = it.uid,
                                    createAt = FieldValue.serverTimestamp()
                                    )
                            }
                            db.collection("posts").add(post!!.toMap()).addOnSuccessListener {
                                Toast.makeText(requireActivity(), "Your post have been created!!", Toast.LENGTH_SHORT).show()
                                createPostBtn.visibility = View.VISIBLE
                                createProgressBar.visibility = View.INVISIBLE
                                getDataFromFirebase()
                                popupAddPost.dismiss()
                            }.addOnFailureListener{ exception ->
                                Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                                createPostBtn.visibility = View.VISIBLE
                                createProgressBar.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initPopup()
        setUpPopupImageClick()
        val floatingPopupBtn = view.findViewById<FloatingActionButton>(R.id.floatingPopup)
        floatingPopupBtn.setOnClickListener {
            popupAddPost.show()
        }
        // recycler view
        postRecyclerView = view.findViewById(R.id.postRV)
        postRecyclerView?.setHasFixedSize(true)
        postLayoutManager = LinearLayoutManager(activity)
        postRecyclerView?.layoutManager = postLayoutManager

        getDataFromFirebase()
        return view
    }

    private fun getDataFromFirebase() {
        val db = Firebase.firestore
        db.collection("posts").orderBy("createAt", Query.Direction.DESCENDING).get().addOnSuccessListener { result ->
            postAdapter = PostAdapter(result, requireContext())
            postRecyclerView?.adapter = postAdapter
        }.addOnFailureListener{ exception -> Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_LONG).show()}
    }

    private fun setUpPopupImageClick() {
        val postedImg = popupAddPost.findViewById<ImageView>(R.id.popupCreatedImage)
        postedImg.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 22) {
                checkAndRequestForPermission();
            } else {
                openGallery()
            }
        }
    }

    private fun checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission using the ActivityResultLauncher
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, HomeFragment.PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == HomeFragment.PICK_IMAGE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            selectedImageUri = data.data

            // display the selected image in the ImageView
            val postedImg = popupAddPost.findViewById<ImageView>(R.id.popupCreatedImage)
            postedImg.setImageURI(selectedImageUri)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        private const val PICK_IMAGE_REQUEST_CODE = 100
    }
}