package com.petarpetrevski.socialmediaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.petarpetrevski.socialmediaapp.Adapter.CommentAdapter
import com.petarpetrevski.socialmediaapp.Model.Post
import com.petarpetrevski.socialmediaapp.Model.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*
import org.w3c.dom.Comment

class CommentsActivity : AppCompatActivity() {

    private var postID = ""
    private var publisherID = ""
    private var firebaseUser: FirebaseUser? = null
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")
    private var commentAdapter: CommentAdapter? = null
    private var commentList: MutableList<com.petarpetrevski.socialmediaapp.Model.Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val intent = intent
        postID = intent.getStringExtra("postID")!!
        publisherID = intent.getStringExtra("publisherID")!!

        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.recycler_view_comments)
        val linearLayoutManager = LinearLayoutManager(this)
//        linearLayoutManager.reverseLayout = true
//        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        userInfo()
        retrieveComments()
        retrievePostImage()

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

    private fun retrievePostImage() {

        val postRef = database.reference.child("Posts").child(postID!!).child("postimage")

        postRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    val image = snapshot.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(post_image_comments)

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun retrieveComments() {

        val commentsRef = database.reference
            .child("Comments")
            .child(postID)

        commentsRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    commentList!!.clear()

                    for (snapshot in snapshot.children) {

                        val comment = snapshot.getValue(com.petarpetrevski.socialmediaapp.Model.Comment::class.java)
                        commentList!!.add(comment!!)

                    }

                    commentAdapter!!.notifyDataSetChanged()

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

}