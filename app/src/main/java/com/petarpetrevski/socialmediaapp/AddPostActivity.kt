package com.petarpetrevski.socialmediaapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPictureRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)


        storagePostPictureRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        save_add_post_button.setOnClickListener { uploadImage() }

        CropImage.activity()
            .setAspectRatio(1,1)
            .start(this@AddPostActivity)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            image_post.setImageURI(imageUri)

        } else {

            // error toast if user selects a file which is not a picture

        }

    }

    private fun uploadImage() {

        when {

            imageUri == null -> Toast.makeText(this, "Profile photo is required.", Toast.LENGTH_LONG)
            TextUtils.isEmpty(description_post.text.toString()) -> Toast.makeText(this, "Description is required.", Toast.LENGTH_LONG)

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("New Post")
                progressDialog.setMessage("Uploading post...")
                progressDialog.show()

                val fileRef = storagePostPictureRef!!.child(System.currentTimeMillis().toString() + ".jpg")

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

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postID = ref.push().key

                        val postMap = HashMap<String, Any>()
                        postMap["postid"] = postID!!
                        postMap["description"] = description_post.text.toString().lowercase()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUrl

                        ref.child(postID).updateChildren(postMap)

                        Toast.makeText(this, "Post uploaded successfully.", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
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