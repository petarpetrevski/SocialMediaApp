package com.petarpetrevski.socialmediaapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.toObject
import com.petarpetrevski.socialmediaapp.AccountSettingsActivity
import com.petarpetrevski.socialmediaapp.Adapter.UserPostsAdapter
import com.petarpetrevski.socialmediaapp.Model.Post
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.R
import com.petarpetrevski.socialmediaapp.RetrieveUsersActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {


    private lateinit var profileID: String
    private lateinit var firebaseUser: FirebaseUser
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")

    var postList: List<Post>? = null
    var userPostsAdapter: UserPostsAdapter? = null

    var collectionList: List<Post>? = null
    var userCollectionsAdapter: UserPostsAdapter? = null
    var userCollectionPostsList: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileID = pref.getString("profileID", "none").toString()
        }

        if (profileID == firebaseUser.uid) {

            view.edit_account_settings_button.text = "Edit Profile"

        } else if (profileID != firebaseUser.uid) {

            checkFollowAndFollowingButtonStatus()

        }


        // Recycler View for Uploaded Posts
        var recyclerViewUploadedPosts: RecyclerView
        recyclerViewUploadedPosts = view.findViewById(R.id.recycler_view_uploaded_posts)
        recyclerViewUploadedPosts.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadedPosts.layoutManager = linearLayoutManager

        postList = ArrayList()
        userPostsAdapter = context?.let { UserPostsAdapter(it, postList as ArrayList<Post>) }
        recyclerViewUploadedPosts.adapter = userPostsAdapter



        // Recycler View for Saved Posts
        var recyclerViewSavedPosts: RecyclerView
        recyclerViewSavedPosts = view.findViewById(R.id.recycler_view_saved_posts)
        recyclerViewSavedPosts.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewSavedPosts.layoutManager = linearLayoutManager2

        collectionList = ArrayList()
        userCollectionsAdapter = context?.let { UserPostsAdapter(it, collectionList as ArrayList<Post>) }
        recyclerViewSavedPosts.adapter = userCollectionsAdapter


        recyclerViewSavedPosts.visibility = View.GONE
        recyclerViewUploadedPosts.visibility = View.VISIBLE


        var uploadedPostsButton: ImageButton
        uploadedPostsButton = view.findViewById(R.id.posts_grid_view_button)
        uploadedPostsButton.setOnClickListener {
            recyclerViewSavedPosts.visibility = View.GONE
            recyclerViewUploadedPosts.visibility = View.VISIBLE
        }

        var savedPostsButton: ImageButton
        savedPostsButton = view.findViewById(R.id.saved_posts_button)
        savedPostsButton.setOnClickListener {
            recyclerViewSavedPosts.visibility = View.VISIBLE
            recyclerViewUploadedPosts.visibility = View.GONE
        }



        view.layout_followers.setOnClickListener{

            val intent = Intent(context, RetrieveUsersActivity::class.java)

            intent.putExtra("id", profileID)
            intent.putExtra("title", "followers")
            startActivity(intent)

        }

        view.layout_followings.setOnClickListener{

            val intent = Intent(context, RetrieveUsersActivity::class.java)

            intent.putExtra("id", profileID)
            intent.putExtra("title", "following")
            startActivity(intent)

        }



        view.edit_account_settings_button.setOnClickListener{
//            startActivity(Intent(context, AccountSettingsActivity::class.java))

            val getButtonText = view.edit_account_settings_button.text.toString()

            when {
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        database.reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileID)
                            .setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        database.reference
                            .child("Follow").child(profileID)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }
                }

                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        database.reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileID)
                            .removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        database.reference
                            .child("Follow").child(profileID)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }
                }
            }



        }

        getFollowers()
        getFollowings()
        userInfo()
        userPosts()
        getTotalNumberOfPosts()
        userCollection()

        return view
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser?.uid.let { it1 ->
            database.reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(profileID).exists()) {
                        view?.edit_account_settings_button?.text = "Following"
                    } else {
                        view?.edit_account_settings_button?.text = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

    }

    private fun getFollowers() {
        val followersRef = database.reference
                .child("Follow").child(profileID)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    view?.total_followers?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowings() {


        val followingsRef = database.reference
                .child("Follow").child(profileID)
                .child("Following")



        followingsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    view?.total_following?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun userPosts() {

        val postRef = database.reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    (postList as ArrayList<Post>).clear()

                    for (snapshot in snapshot.children) {

                        val post = snapshot.getValue(Post::class.java)!!

                        if (post.getPublisher().equals(profileID)) {

                            (postList as ArrayList<Post>).add(post)

                        }

                        Collections.reverse(postList)
                        userPostsAdapter!!.notifyDataSetChanged()

                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


    private fun userInfo() {
//        val usersRef = database.getReference().child("Users").child(profileID)
        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(profileID)

//        usersRef.addValueEventListener(object : ValueEventListener {
//
//            override fun onDataChange(snapshot: DataSnapshot) {
////                if (context != null) {
////                    return
////                }
//
//                if (snapshot.exists()) {
//                    val user = snapshot.getValue<User>(User::class.java)
//
//                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into((view?.profile_image_profile_fragment))
//                    view?.profile_fragment_username?.text = user!!.getUsername()
//                    view?.full_name_profile_fragment?.text = user!!.getFullname()
//                    view?.bio_profile_fragment?.text = user!!.getBio()
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })


            usersRef.addSnapshotListener(object : EventListener<DocumentSnapshot> {

                override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    if (value!!.exists()) {
                        val user = value.toObject(User::class.java)

                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into((view?.profile_image_profile_fragment))
                        view?.profile_fragment_username?.text = user.getUsername()
                        view?.full_name_profile_fragment?.text = user.getFullname()
                        view?.bio_profile_fragment?.text = user.getBio()
                    }

                }

            })




    }


    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileID", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileID", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileID", firebaseUser.uid)
        pref?.apply()
    }


    private fun getTotalNumberOfPosts() {

        val postRef = database.reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    var postCounter = 0

                    for (snapshot in snapshot.children) {

                        var post = snapshot.getValue(Post::class.java)!!
                        if (post.getPublisher() == profileID) {

                            postCounter++

                        }

                    }

                    total_posts.text = postCounter.toString()

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun userCollection() {

        userCollectionPostsList = ArrayList()

        val collectionRef = database.reference
            .child("Collections")
            .child(firebaseUser.uid)

        collectionRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    for (snapshot in snapshot.children) {

                        (userCollectionPostsList as ArrayList<String>).add(snapshot.key!!)

                    }

                    retrieveCollectionPostsData()

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun retrieveCollectionPostsData() {

        val postRef = database.reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    (collectionList as ArrayList<Post>).clear()

                    for (snapshot in snapshot.children) {

                        val post = snapshot.getValue(Post::class.java)

                        for (key in userCollectionPostsList!!) {

                            if (post!!.getPostid() == key) {

                                (collectionList as ArrayList<Post>).add(post!!)
                                (collectionList as ArrayList<Post>).reverse()

                            }

                        }

                    }

                    userCollectionsAdapter!!.notifyDataSetChanged()

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


}