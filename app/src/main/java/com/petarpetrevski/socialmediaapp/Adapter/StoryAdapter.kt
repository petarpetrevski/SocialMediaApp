package com.petarpetrevski.socialmediaapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.petarpetrevski.socialmediaapp.AddStoryActivity
import com.petarpetrevski.socialmediaapp.MainActivity
import com.petarpetrevski.socialmediaapp.Model.Story
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.view.*

class StoryAdapter(private val mContext: Context, private val mStory: List<Story>) :
RecyclerView.Adapter<StoryAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return if (viewType == 0) {

            val view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item_layout, parent, false)
            ViewHolder(view)

        } else {

            val view = LayoutInflater.from(mContext).inflate(R.layout.story_item_layout, parent, false)
            return ViewHolder(view)

        }

    }

    override fun getItemCount(): Int {

        return mStory.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val story = mStory[position]

        userInfo(holder, story.getUserID(), position)

        holder.itemView.setOnClickListener {

            val intent = Intent(mContext, AddStoryActivity::class.java)
            intent.putExtra("userid", story.getStoryID())
            mContext.startActivity(intent)

        }

    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        //StoryItem
        var storyProfilePhoto: CircleImageView? = null
        var storyProfilePhotoSeen: CircleImageView? = null
        var storyUserName: TextView? = null


        //AddStoryItem
        var addStoryButton: ImageView? = null
        var addStoryText: TextView? = null

        init {
            //StoryItem
            storyProfilePhoto = itemView.findViewById(R.id.story_profile_photo)
            storyProfilePhotoSeen = itemView.findViewById(R.id.story_profile_photo_seen)
            storyUserName = itemView.findViewById(R.id.story_user_name)


            //AddStoryItem
            addStoryButton = itemView.findViewById(R.id.story_add)
            addStoryText = itemView.findViewById(R.id.add_story_text)
        }

    }


    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            //add story item
            return  0
        }
        //story item
        return 1
    }

    private fun userInfo(viewHolder: ViewHolder, userID: String, position: Int) {

//        val usersRef = database.getReference().child("Users").child(profileID)

        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(userID)

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

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(viewHolder.storyProfilePhoto)

                    if (position != 0) {

                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(viewHolder.storyProfilePhotoSeen)
                        viewHolder.storyUserName!!.text = user.getUsername()

                    }

                }

            }

        })




    }

}