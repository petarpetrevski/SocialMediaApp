package com.petarpetrevski.socialmediaapp

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.petarpetrevski.socialmediaapp.Model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        logout_button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        save_profile_info_button.setOnClickListener {

            if (checker == "clicked") {



            } else {

                updateUserInfoOnly()

            }

        }

        userInfo()

    }

    private fun updateUserInfoOnly() {

        when {

            TextUtils.isEmpty(full_name_profile_fragment.text.toString()) -> Toast.makeText(this, "Name is required.", Toast.LENGTH_LONG)
            TextUtils.isEmpty(username_profile_fragment.text.toString()) -> Toast.makeText(this, "Username is required.", Toast.LENGTH_LONG)

            else -> {

                val usersRef = FirebaseFirestore.getInstance().collection("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = full_name_profile_fragment.text.toString().lowercase()
                userMap["username"] = username_profile_fragment.text.toString().lowercase()
                userMap["bio"] = bio_profile_fragment.text.toString().lowercase()

                usersRef.document(firebaseUser.uid).update(userMap)

                Toast.makeText(this, "Account information updated successfully.", Toast.LENGTH_LONG).show()

                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            }

        }
//        if (full_name_profile_fragment.text.toString() == "") {
//
//            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_LONG)
//
//        } else if (username_profile_fragment.text.toString() == "") {
//
//            Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_LONG)
//
//        } else {
//
//            val usersRef = FirebaseFirestore.getInstance().collection("Users")
//
//            val userMap = HashMap<String, Any>()
//            userMap["fullname"] = full_name_profile_fragment.text.toString().lowercase()
//            userMap["username"] = username_profile_fragment.text.toString().lowercase()
//            userMap["bio"] = bio_profile_fragment.text.toString().lowercase()
//
//        }

    }


    private fun userInfo() {
        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)

        usersRef.addSnapshotListener(object : EventListener<DocumentSnapshot> {

            override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                if (value!!.exists()) {
                    val user = value.toObject(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_info_image_profile_fragment)
                    username_profile_fragment.setText(user!!.getUsername())
                    full_name_profile_fragment.setText(user!!.getFullname())
                    bio_profile_fragment.setText(user!!.getBio())
                }

            }

        })

    }

}