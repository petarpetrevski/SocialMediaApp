package com.petarpetrevski.socialmediaapp.Adapter

import android.app.AlertDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.petarpetrevski.socialmediaapp.AddStoryActivity
import com.petarpetrevski.socialmediaapp.MainActivity
import com.petarpetrevski.socialmediaapp.Model.Story
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.R
import com.petarpetrevski.socialmediaapp.StoryActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.view.*

class StoryAdapter(private val mContext: Context, private val mStory: List<Story>) :
RecyclerView.Adapter<StoryAdapter.ViewHolder>(){

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")

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

        if (holder.adapterPosition !== 0) {

            seenStory(holder, story.getUserID())

        }

        if (holder.adapterPosition === 0) {

            userStories(holder.addStoryText!!, holder.addStoryButton!!, false)

        }

        holder.itemView.setOnClickListener {

            if (holder.adapterPosition === 0) {

                userStories(holder.addStoryText!!, holder.addStoryButton!!, true)

            } else {

                val intent = Intent(mContext, StoryActivity::class.java)
                intent.putExtra("userId", story.getUserID())
                mContext.startActivity(intent)

            }

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

        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(userID)

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


    private fun userStories(textView: TextView, imageView: ImageView, click: Boolean) {

        val storyRef = database.reference
            .child("Stories")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        storyRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                var counter = 0

                val timeCurrent = System.currentTimeMillis()

                for (snapshot in snapshot.children) {

                    val story = snapshot.getValue(Story::class.java)

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story!!.getTimeEnd()) {

                        counter++

                    }

                }

                if (click) {

                    if (counter > 0) {

                        val alertDialog = AlertDialog.Builder(mContext).create()

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story") {

                            dialogInterface, which ->
                            val intent = Intent(mContext, StoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)

                            dialogInterface.dismiss()

                        }

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story") {

                            dialogInterface, which ->

                            val intent = Intent(mContext, AddStoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)

                            dialogInterface.dismiss()

                        }

                        alertDialog.show()

                    } else {

                        val intent = Intent(mContext, AddStoryActivity::class.java)
                        intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                        mContext.startActivity(intent)

                    }

                }  else {

                    if (counter > 0) {

                        textView.text = "My Story"
                        imageView.visibility = View.GONE

                    } else {

                        textView.text = "Add Story"
                        imageView.visibility = View.VISIBLE

                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


    private fun seenStory(viewHolder: ViewHolder, userID: String) {

        val storyRef = database.reference
            .child("Stories")
            .child(userID)

        storyRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                var i = 0
                for (snapshot in snapshot.children) {

                    if (!snapshot.child("views").child(FirebaseAuth.getInstance().currentUser!!.uid).exists()
                        && System.currentTimeMillis() < snapshot.getValue(Story::class.java)!!.getTimeEnd()) {

                        i++

                    }

                }

                if (i > 0) {

                    viewHolder.storyProfilePhoto!!.visibility = View.VISIBLE
                    viewHolder.storyProfilePhotoSeen!!.visibility = View.GONE

                } else {

                    viewHolder.storyProfilePhoto!!.visibility = View.GONE
                    viewHolder.storyProfilePhotoSeen!!.visibility = View.VISIBLE

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

}