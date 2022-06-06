package com.petarpetrevski.socialmediaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.petarpetrevski.socialmediaapp.Adapter.StoryAdapter
import com.petarpetrevski.socialmediaapp.Model.Story
import com.petarpetrevski.socialmediaapp.Model.User
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")

    private var currentUserID: String = ""
    private var userID: String = ""
    var counter = 0
    var pressTime = 0L
    var limit = 500L

    var imagesList: List<String>? = null
    var storyIDsList: List<String>? = null

    var storiesProgressView: StoriesProgressView? = null

    private val onTouchListener = View.OnTouchListener { view, motionEvent ->

        when(motionEvent.action) {

            MotionEvent.ACTION_DOWN -> {

                pressTime = System.currentTimeMillis()
                storiesProgressView!!.pause()
                return@OnTouchListener false

            }

            MotionEvent.ACTION_UP -> {

                val now = System.currentTimeMillis()
                storiesProgressView!!.resume()
                return@OnTouchListener limit < now - pressTime

            }

        }

        false

    }



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        userID = intent.getStringExtra("userId").toString()

        storiesProgressView = findViewById(R.id.progress_story)

        layout_seen_story.visibility = View.GONE
        delete_story.visibility = View.GONE

        if (userID == currentUserID) {

            layout_seen_story.visibility = View.VISIBLE
            delete_story.visibility = View.VISIBLE

        }

        retrieveStories(userID)
        userInfo(userID)

        val previousStory: View = findViewById(R.id.previous_story)
        previousStory.setOnClickListener { storiesProgressView!!.reverse() }
        previousStory.setOnTouchListener(onTouchListener)

        val skipStory: View = findViewById(R.id.skip_story)
        skipStory.setOnClickListener { storiesProgressView!!.skip() }
        skipStory.setOnTouchListener(onTouchListener)


        layout_seen_story.setOnClickListener {

            val intent = Intent(this@StoryActivity, RetrieveUsersActivity::class.java)
            intent.putExtra("id", userID)
            intent.putExtra("storyid", storyIDsList!![counter])
            intent.putExtra("title", "views")
            startActivity(intent)

        }

        delete_story.setOnClickListener {

            val ref = database.reference
                .child("Stories")
                .child(userID!!)
                .child(storyIDsList!![counter])

            ref.removeValue().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    Toast.makeText(this@StoryActivity, "Story deleted.", Toast.LENGTH_LONG).show()
                }

            }

            finish()

        }

    }


    private fun retrieveStories(userID: String) {

        imagesList = ArrayList()
        storyIDsList = ArrayList()

        val ref = database.reference
            .child("Stories")
            .child(userID)


        Log.d("TAG", "PERO TUKA VLEZE YEY $userID")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {


                Log.d("TAG", "PERO TUKA VLEZE be $p0")

                (imagesList as ArrayList<String>).clear()
                (storyIDsList as ArrayList<String>).clear()



                for (snapshot in p0.children) {


                    Log.d("TAG", "PERO TUKA VLEZE YEYEEYEY")

                    var story: Story? = snapshot.getValue<Story>(Story::class.java)
                    var timeCurrent = System.currentTimeMillis()

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()) {

                        (imagesList as ArrayList<String>).add(story.getImageUrl())
                        (storyIDsList as ArrayList<String>).add(story.getStoryID())

                    }

                }

                storiesProgressView!!.setStoriesCount((imagesList as ArrayList<String>).size)
                Log.d("TAG", "PERO TUKA PROBA " + (imagesList as ArrayList<String>).size)
                storiesProgressView!!.setStoryDuration(5000L)
                storiesProgressView!!.setStoriesListener(this@StoryActivity)
                storiesProgressView!!.startStories(counter)
                Picasso.get().load(imagesList!!.get(counter)).placeholder(R.drawable.profile).into(image_story)

                addViewToStory(storyIDsList!!.get(counter))
                seenNumber(storyIDsList!!.get(counter))

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


    private fun userInfo(userID: String) {

        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(userID)

        usersRef.addSnapshotListener(object : EventListener<DocumentSnapshot> {

            override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                if (value!!.exists()) {

                    val user = value.toObject(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_story)

                    username_story.text = user.getUsername()

                }

            }

        })

    }


    private fun addViewToStory(storyID: String) {

        val ref = database.reference.child("Stories")
            .child(userID!!)
            .child(storyID)
            .child("views")
            .child(currentUserID)
            .setValue(true)

    }


    private fun seenNumber(storyID: String) {

        val ref = database.reference.child("Stories")
            .child(userID!!)
            .child(storyID)
            .child("views")

        ref.addListenerForSingleValueEvent(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                seen_number_story.text = "" + snapshot.childrenCount

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    override fun onNext() {

        Picasso.get().load(imagesList!![++counter]).placeholder(R.drawable.profile).into(image_story)
        addViewToStory(storyIDsList!![counter])
        seenNumber(storyIDsList!![counter])

    }

    override fun onPrev() {

        if (counter - 1 < 0) return
        Picasso.get().load(imagesList!![--counter]).placeholder(R.drawable.profile).into(image_story)
        seenNumber(storyIDsList!![counter])

    }

//    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
//        return super.onKeyLongPress(keyCode, event)
//    }

    override fun onComplete() {

        finish()

    }

    override fun onDestroy() {

        super.onDestroy()
        storiesProgressView!!.destroy()

    }

    override fun onResume() {

        super.onResume()
        storiesProgressView!!.resume()

    }

    override fun onPause() {

        super.onPause()
        storiesProgressView!!.pause()

    }

}