package com.petarpetrevski.socialmediaapp.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.petarpetrevski.socialmediaapp.Fragments.PostDetailsFragment
import com.petarpetrevski.socialmediaapp.Fragments.ProfileFragment
import com.petarpetrevski.socialmediaapp.Model.Notification
import com.petarpetrevski.socialmediaapp.Model.Post
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class NotificationAdapter(
    private val mContext: Context,
    private val mNotification: List<Notification>)
    : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {


    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(mContext).inflate(R.layout.notifications_item_layout, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {

        return mNotification.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val notification = mNotification[position]

        if (notification.getText().equals("started following you.")) {

            holder.text.text = "started following you."

        } else if (notification.getText().equals("liked your post.")) {

            holder.text.text = "liked your post."

        } else if (notification.getText().contains("commented:")) {

            holder.text.text = notification.getText().replace("commented:", "commented: ")

        } else {

            holder.text.text = notification.getText()

        }



        retrieveUserInfo(holder.profileImage, holder.userName, notification.getUserID())

        if (notification.isIsPost()) {

            holder.postImage.visibility = View.VISIBLE
            retrievePostImage(holder.postImage, notification.getPostID())

        } else {

            holder.postImage.visibility = View.GONE

        }


        holder.itemView.setOnClickListener {

            if (notification.isIsPost()) {

                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

                editor.putString("postID", notification.getPostID())

                editor.apply()

                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, PostDetailsFragment())
                    .commit()

            } else {

                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

                editor.putString("profileID", notification.getUserID())

                editor.apply()

                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .commit()

            }

        }

    }



    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        var profileImage: CircleImageView
        var postImage:ImageView
        var userName: TextView
        var text: TextView

        init {

            profileImage = itemView.findViewById(R.id.profile_image_notification)
            postImage = itemView.findViewById(R.id.post_image_notification)
            userName = itemView.findViewById(R.id.username_notification)
            text = itemView.findViewById(R.id.comment_notification)

        }

    }

    private fun retrieveUserInfo(imageView: ImageView, userName: TextView, publisherID: String) {
//        val usersRef = database.getReference().child("Users").child(profileID)
        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(publisherID)

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

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imageView)
                    userName.text = user.getUsername()
                }

            }

        })




    }

    private fun retrievePostImage(imageView: ImageView, postid: String) {

        val postRef = database.reference
            .child("Posts")
            .child(postid)

        postRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    val post = snapshot.getValue<Post>(Post::class.java)

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.profile).into(imageView)

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

}