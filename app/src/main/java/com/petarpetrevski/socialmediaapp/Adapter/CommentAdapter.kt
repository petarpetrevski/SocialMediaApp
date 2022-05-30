package com.petarpetrevski.socialmediaapp.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.petarpetrevski.socialmediaapp.Model.Comment
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(private val mContext: Context,
                     private val mComment: MutableList<Comment>?
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {


    private var firebaseUser: FirebaseUser? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {

        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_item_layout, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return mComment!!.size
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {

        firebaseUser = FirebaseAuth.getInstance().currentUser

        val comment = mComment!![position]
        holder.commentTextView.text = comment.getComment()
        getUserInfo(holder.imageProfile, holder.userNameTextView, comment.getPublisher())

    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imageProfile: CircleImageView
        var userNameTextView: TextView
        var commentTextView: TextView

        init {

            imageProfile = itemView.findViewById(R.id.user_profile_image_comment)
            userNameTextView = itemView.findViewById(R.id.user_name_comment)
            commentTextView = itemView.findViewById(R.id.text_comment)

        }

    }

    private fun getUserInfo(imageProfile: CircleImageView, userNameTextView: TextView, publisher: String) {

        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(publisher)

        usersRef.addSnapshotListener(object : EventListener<DocumentSnapshot> {

            override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                if (value!!.exists()) {
                    val user = value.toObject(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imageProfile)
                    userNameTextView.text = user!!.getUsername()
                }

            }

        })

    }

}