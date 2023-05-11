package org.classapp.moodpic.Activities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.classapp.moodpic.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val profileImg = view.findViewById<ImageView>(R.id.profileSecImg)
        val usernameTxt = view.findViewById<TextView>(R.id.profileUsernameTxt)
        val emailTxt = view.findViewById<TextView>(R.id.profileEmailTxt)

        var db = Firebase.firestore
        var auth = Firebase.auth

        val email = auth.currentUser?.email
        emailTxt.text = email
        db.collection("users").whereEqualTo("id", auth.currentUser?.uid).get().addOnSuccessListener {
            if (!it.isEmpty) {
                usernameTxt.text = it.documents[0].getString("username")
                var imageUrl = it.documents[0].getString("profileImageUrl")
                if (imageUrl != null) {
                    Glide.with(this).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(profileImg)
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            Log.e("getCommentFromFirebase", "Error getting comments", exception)
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}