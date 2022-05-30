package com.petarpetrevski.socialmediaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.petarpetrevski.socialmediaapp.Model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {

    private var postID = ""
    private var publisherID = ""
    private var firebaseUser: FirebaseUser? = null
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val intent = intent
        postID = intent.getStringExtra("postID")!!
        publisherID = intent.getStringExtra("publisherID")!!

        firebaseUser = FirebaseAuth.getInstance().currentUser

        userInfo()

        post_comment_text.setOnClickListener(View.OnClickListener {

            if (add_comment_text.text.toString() == "") {

                Toast.makeText(this@CommentsActivity, "Please enter your comment.", Toast.LENGTH_LONG).show()

            } else {

                postComment()

            }

        })


    }

    private fun postComment() {

        val commentsRef = database.reference
            .child("Comments")
            .child(postID!!)

        val commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = add_comment_text!!.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        add_comment_text!!.text.clear()

    }

    private fun userInfo() {
        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser!!.uid)

        usersRef.addSnapshotListener(object : EventListener<DocumentSnapshot> {

            override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                if (value!!.exists()) {
                    val user = value.toObject(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_comments)
                }

            }

        })

    }



}