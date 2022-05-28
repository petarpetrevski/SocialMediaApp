package com.petarpetrevski.socialmediaapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.petarpetrevski.socialmediaapp.AccountSettingsActivity
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment : Fragment() {


    private lateinit var profileID: String
    private lateinit var firebaseUser: FirebaseUser
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")

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
                        view?.profile_fragment_username?.text = user!!.getUsername()
                        view?.full_name_profile_fragment?.text = user!!.getFullname()
                        view?.bio_profile_fragment?.text = user!!.getBio()
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


}