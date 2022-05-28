package com.petarpetrevski.socialmediaapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.petarpetrevski.socialmediaapp.Model.User
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePictureRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePictureRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        logout_button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        change_image_text_button.setOnClickListener {

            Log.d("TAG", "PERO TUKA SUM")

            checker = "clicked"

            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsActivity)
        }

        save_profile_info_button.setOnClickListener {

            if (checker == "clicked") {

                uploadImageAndUpdateInfo()

            } else {

                updateUserInfoOnly()

            }

        }

        userInfo()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_info_image_profile_fragment.setImageURI(imageUri)

        }
    }


    private fun updateUserInfoOnly() {

        when {

            TextUtils.isEmpty(full_name_profile_fragment.text.toString()) -> Toast.makeText(this, "Name is required.", Toast.LENGTH_LONG)
            TextUtils.isEmpty(username_profile_fragment.text.toString()) -> Toast.makeText(this, "Username is required.", Toast.LENGTH_LONG)

            else -> {

                val usersRef = FirebaseFirestore.getInstance().collection("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = full_name_profile_fragment.text.toString().lowercase()
                userMap["username"] = username_profile_fragment.text.toString().lowercase()
                userMap["bio"] = bio_profile_fragment.text.toString().lowercase()

                usersRef.document(firebaseUser.uid).update(userMap)

                Toast.makeText(this, "Account information updated successfully.", Toast.LENGTH_LONG).show()

                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            }

        }
//        if (full_name_profile_fragment.text.toString() == "") {
//
//            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_LONG)
//
//        } else if (username_profile_fragment.text.toString() == "") {
//
//            Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_LONG)
//
//        } else {
//
//            val usersRef = FirebaseFirestore.getInstance().collection("Users")
//
//            val userMap = HashMap<String, Any>()
//            userMap["fullname"] = full_name_profile_fragment.text.toString().lowercase()
//            userMap["username"] = username_profile_fragment.text.toString().lowercase()
//            userMap["bio"] = bio_profile_fragment.text.toString().lowercase()
//
//        }

    }


    private fun userInfo() {
        val usersRef = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.uid)

        usersRef.addSnapshotListener(object : EventListener<DocumentSnapshot> {

            override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                if (value!!.exists()) {
                    val user = value.toObject(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_info_image_profile_fragment)
                    username_profile_fragment.setText(user!!.getUsername())
                    full_name_profile_fragment.setText(user!!.getFullname())
                    bio_profile_fragment.setText(user!!.getBio())
                }

            }

        })

    }

    private fun uploadImageAndUpdateInfo() {

        when {

            imageUri == null -> Toast.makeText(this, "Profile photo is required.", Toast.LENGTH_LONG)
            TextUtils.isEmpty(full_name_profile_fragment.text.toString()) -> Toast.makeText(this, "Name is required.", Toast.LENGTH_LONG)
            TextUtils.isEmpty(username_profile_fragment.text.toString()) -> Toast.makeText(this, "Username is required.", Toast.LENGTH_LONG)

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Updating profile...")
                progressDialog.show()

                val fileRef = storageProfilePictureRef!!.child(firebaseUser!!.uid + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener (OnCompleteListener<Uri> { task ->

                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseFirestore.getInstance().collection("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = full_name_profile_fragment.text.toString().lowercase()
                        userMap["username"] = username_profile_fragment.text.toString().lowercase()
                        userMap["bio"] = bio_profile_fragment.text.toString().lowercase()
                        userMap["image"] = myUrl

                        ref.document(firebaseUser.uid).update(userMap)

                        Toast.makeText(this, "Account information updated successfully.", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()

                    } else {

                        progressDialog.dismiss()

                    }

                })

            }

        }

    }

}