package com.petarpetrevski.socialmediaapp.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.petarpetrevski.socialmediaapp.Fragments.ProfileFragment
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import javax.annotation.Nonnull

class UserAdapter(private var mContext: Context,
                  private var mUser: ArrayList<User>,
                  private var isFragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {


    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")

    // Getting current user data
    private var firebaseUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]


        holder.userNameTextView.text = user.getUsername()
        holder.fullNameTextView.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userProfileImage)

        checkFollowingStatus(user.getUid(), holder.followButton)

        holder.itemView.setOnClickListener(View.OnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileID", user.getUid())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        })

        holder.followButton.setOnClickListener {

            Log.i("PERO", "HERE IT IS: ${firebaseUser?.uid}")

            if(holder.followButton.text.toString() == "Follow") {

                firebaseUser?.uid.let { it1 ->
                    database.reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUid())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it ->
                                    database.reference
                                        .child("Follow").child(user.getUid())
                                        .child("Followers").child(it.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            } else {
                firebaseUser?.uid.let { it1 ->
                    database.reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUid())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    database.reference
                                        .child("Follow").child(user.getUid())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTextView: TextView = itemView.findViewById(R.id.user_name_search)
        var fullNameTextView: TextView = itemView.findViewById(R.id.full_name_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
        var followButton: Button = itemView.findViewById(R.id.follow_button_search)
    }

    private fun checkFollowingStatus(uid: String, followButton: Button) {
        val followingRef = firebaseUser?.uid.let { it1 ->
            database.reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (datasnapshot.child(uid).exists()) {
                    followButton.text = "Following"
                } else {
                    followButton.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}