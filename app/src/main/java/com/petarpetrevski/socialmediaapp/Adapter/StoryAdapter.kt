package com.petarpetrevski.socialmediaapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.petarpetrevski.socialmediaapp.Model.Story
import com.petarpetrevski.socialmediaapp.R
import de.hdodenhof.circleimageview.CircleImageView

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

}