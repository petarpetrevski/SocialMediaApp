package com.petarpetrevski.socialmediaapp

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sign_in_link_button.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        sign_up_button.setOnClickListener {
            CreateFirestoreAccount()
            // CreateRealtimeAccount()
        }

        username_sign_up.setFilters(arrayOf<InputFilter>(
            object : AllCaps() {
                override fun filter(
                    source: CharSequence,
                    start: Int,
                    end: Int,
                    dest: Spanned,
                    dstart: Int,
                    dend: Int
                ): CharSequence {
                    return source.toString().toLowerCase().replace(" ", "")
                }
            }
        ))
    }
    private fun CreateRealtimeAccount() {
        val fullName = full_name_sing_up.text.toString()
        val userName = username_sign_up.text.toString()
        val email = email_sign_up.text.toString()
        val password = password_sign_up.text.toString()

        when {
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Name is required.", Toast.LENGTH_LONG)
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "Username is required.", Toast.LENGTH_LONG)
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required.", Toast.LENGTH_LONG)
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required.", Toast.LENGTH_LONG)

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("Creating account")
                progressDialog.setMessage("Please wait...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "PERO PROBA TASK TO CREATE USER AUTH SUCCESSFUL")
                            saveUserInfoRealtime(fullName, userName, email, progressDialog)
                        } else {
                            val errorMessage = task.exception!!.toString()
                            Toast.makeText(this, "Error:  $errorMessage", Toast.LENGTH_LONG)
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfoRealtime(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        Log.d(TAG, "CURRENT USER ID: $currentUserID")
        val db = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")
        val usersRef = db.getReference("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName
        userMap["username"] = userName
        userMap["email"] = email
        userMap["bio"] = "test"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/socialmediaapp-4e61a.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=4ff557a8-4d95-474c-8df0-818616dac64d"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account created successfully.", Toast.LENGTH_LONG)

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = task.exception!!.toString()
                    Toast.makeText(this, "Error:  $errorMessage", Toast.LENGTH_LONG)
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }

    private fun CreateFirestoreAccount() {

        val db = Firebase.firestore

        val fullName = full_name_sing_up.text.toString()
        val userName = username_sign_up.text.toString()
        val email = email_sign_up.text.toString()
        val password = password_sign_up.text.toString()

        when {
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Name is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "Username is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required.", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("Creating account")
                progressDialog.setMessage("Please wait...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                        if (task.isSuccessful) {
                            saveUserInfoFirestore(fullName, userName, email, progressDialog)
                        } else {
                            val errorMessage = task.exception!!.toString()
                            Toast.makeText(this, "Error:  $errorMessage", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfoFirestore(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val db = Firebase.firestore

        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        // val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName
        userMap["username"] = userName
        userMap["email"] = email
        userMap["bio"] = "test"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/socialmediaapp-4e61a.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=4ff557a8-4d95-474c-8df0-818616dac64d"

        db.collection("Users")
            .document(currentUserID).set(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account created successfully.", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = task.exception!!.toString()
                    Toast.makeText(this, "Error:  $errorMessage", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}