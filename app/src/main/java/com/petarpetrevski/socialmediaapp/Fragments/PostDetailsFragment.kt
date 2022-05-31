package com.petarpetrevski.socialmediaapp.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.petarpetrevski.socialmediaapp.Adapter.PostAdapter
import com.petarpetrevski.socialmediaapp.Model.Post
import com.petarpetrevski.socialmediaapp.R

class PostDetailsFragment : Fragment() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")
    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var postID: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_post_details, container, false)

        val prefrences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (prefrences != null) {
            postID = prefrences.getString("postID", "none").toString()
        }

        var recyclerView: RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_post_details)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter


        retrievePosts()

        return view
    }

    private fun retrievePosts() {

        val postRef = database.reference.child("Posts")
            .child(postID)

        postRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                postList?.clear()

                val post = snapshot.getValue(Post::class.java)

                postList!!.add(post!!)

                postAdapter!!.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


}