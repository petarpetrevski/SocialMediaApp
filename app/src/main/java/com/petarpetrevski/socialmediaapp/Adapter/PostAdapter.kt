package com.petarpetrevski.socialmediaapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import androidx.annotation.NonNull
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
import com.petarpetrevski.socialmediaapp.MainActivity
import com.petarpetrevski.socialmediaapp.Model.Post
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_account_settings.*

class PostAdapter(private val mContext: Context, private val mPost: List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {

        return mPost.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        if (post.getDescription().equals("")) {

            holder.description.visibility == View.GONE

        } else {

            holder.description.visibility == View.VISIBLE
            holder.description.text = post.getDescription()

        }

        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())

        isLikes(post.getPostid(), holder.likeButton)

        numberOflikes(holder.likes, post.getPostid())

        holder.likeButton.setOnClickListener {

            if (holder.likeButton.tag == "Like") {

                database.reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .setValue(true)

            } else {

                database.reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .removeValue()

//                val intent = Intent(mContext, MainActivity::class.java)
//                mContext.startActivity(intent)

            }

            numberOflikes(holder.likes, post.getPostid())

        }

    }

    private fun numberOflikes(likes: TextView, postid: String) {

        val likesRef = database.reference
            .child("Likes")
            .child(postid)

        likesRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists() && snapshot.childrenCount.toInt() == 1) {

                    likes.visibility = View.VISIBLE
                    likes.text = snapshot.childrenCount.toString() + " like"

                } else if (snapshot.exists()) {

                    likes.visibility = View.VISIBLE
                    likes.text = snapshot.childrenCount.toString() + " likes"

                } else {

                    likes.visibility = View.GONE

                }

            }

            override fun onCancelled(error: DatabaseError) {



            }

        })

    }

    private fun isLikes(postid: String, likeButton: ImageView) {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val likesRef = database.reference
            .child("Likes")
            .child(postid)

        likesRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(firebaseUser!!.uid).exists()) {

                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"

                } else {

                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"

                }

            }

            override fun onCancelled(error: DatabaseError) {



            }

        })

    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {


        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var userName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            userName = itemView.findViewById(R.id.user_name_post)
            likes = itemView.findViewById(R.id.post_likes)
            publisher = itemView.findViewById(R.id.post_publisher)
            description = itemView.findViewById(R.id.post_description)
            comments = itemView.findViewById(R.id.post_comments)
        }

    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {

        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(publisherID)

        usersRef.addSnapshotListener(object : EventListener<DocumentSnapshot> {

            override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                if (value!!.exists()) {
                    val user = value.toObject(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    userName.text = user!!.getUsername()
                    publisher.text = user!!.getFullname()
                }

            }

        })

    }

}