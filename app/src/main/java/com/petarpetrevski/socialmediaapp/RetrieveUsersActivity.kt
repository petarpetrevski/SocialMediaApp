package com.petarpetrevski.socialmediaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.auth.User
import com.petarpetrevski.socialmediaapp.Adapter.UserAdapter
import kotlinx.android.synthetic.main.fragment_profile.view.*

class RetrieveUsersActivity : AppCompatActivity() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")
    private var firestoreDatabase : FirebaseFirestore? = null

    var id: String = ""
    var title: String = ""

    var userAdapter: UserAdapter? = null
    var userList: List<User>? = null
    var userIdList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrieve_users)

        val intent = intent
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!

        val toolbarRetrieveUsers: Toolbar = findViewById(R.id.toolbar_retrieve_users)
        setSupportActionBar(toolbarRetrieveUsers)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbarRetrieveUsers.setNavigationOnClickListener { finish() }

        var recyclerView: RecyclerView? = null
        recyclerView = findViewById(R.id.recycler_view_retrieve_users)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userIdList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<com.petarpetrevski.socialmediaapp.Model.User>, false)
        recyclerView.adapter = userAdapter


        when(title) {

            "likes" -> retrieveLikes()
            "following" -> retrieveFollowing()
            "followers" -> retrieveFollowers()
            "views" -> retrieveViews()

        }

    }



    private fun retrieveLikes() {


        Log.d("TAG", "PERO PROBVA VLEGOV VO retrieveLikes")

            val likesRef = database.reference
                .child("Likes")
                .child(id)

            likesRef.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        (userIdList as ArrayList<String>).clear()

                        for (snapshot in snapshot.children) {

                            (userIdList as ArrayList<String>).add(snapshot.key!!)

                        }

                        retrieveUserInfo()

                    }

                }

                override fun onCancelled(error: DatabaseError) {



                }

            })

    }



    private fun retrieveFollowing() {

        Log.d("TAG", "PERO PROBVA VLEGOV VO RETRIEVEFOLLOWING")

        val followingsRef = database.reference
            .child("Follow").child(id)
            .child("Following")



        followingsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                (userIdList as ArrayList<String>).clear()

                for (snapshot in snapshot.children) {

                    (userIdList as ArrayList<String>).add(snapshot.key!!)

                }


                Log.d("TAG", "PERO PROBVA VLEGOV VO RETRIEVEFOLLOWING $userIdList ")

                retrieveUserInfo()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }



    private fun retrieveFollowers() {

        val followersRef = database.reference
            .child("Follow").child(id)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                (userIdList as ArrayList<String>).clear()

                for (snapshot in snapshot.children) {

                    (userIdList as ArrayList<String>).add(snapshot.key!!)

                }

                retrieveUserInfo()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }



    private fun retrieveViews() {

    }



    private fun retrieveUserInfo() {

        firestoreDatabase = FirebaseFirestore.getInstance()

        firestoreDatabase!!.collection("Users").
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }



                (userList as ArrayList<com.petarpetrevski.socialmediaapp.Model.User>).clear()

                for (dc : DocumentChange in value?.documentChanges!!){

                    val user = dc.document.toObject(com.petarpetrevski.socialmediaapp.Model.User::class.java)

                    for (id in userIdList!!) {

                        if (user!!.getUid() == id) {


                            Log.d("TAG", "PERO PROBVA VLEGOV VO retrieveUserInfo")

                            (userList as ArrayList<com.petarpetrevski.socialmediaapp.Model.User>).add(user)

                        }

                    }

                }

                userAdapter?.notifyDataSetChanged()

            }
        })

    }



}