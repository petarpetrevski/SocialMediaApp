package com.petarpetrevski.socialmediaapp

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class SignInActivity : AppCompatActivity() {


    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private var loginWith : String = ""

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest


    companion object {
        const val RC_SIGN_IN = 1001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        FacebookSdk.sdkInitialize(applicationContext)

        mAuth = FirebaseAuth.getInstance()



//        oneTapClient = Identity.getSignInClient(this)
//        signInRequest = BeginSignInRequest.builder()
//            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
//                .setSupported(true)
//                .build())
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(getString(R.string.web_client_id))
//                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(true)
//                    .build())
//            // Automatically sign in when exactly one credential is retrieved.
//            .setAutoSelectEnabled(true)
//            .build()



        //facebook login

        val facebookBtn : com.facebook.login.widget.LoginButton = findViewById(R.id.sign_in_facebook_button)


        sign_in_fb_button.setOnClickListener {
            loginWith = "facebook"
            facebookBtn.performClick()
        }
        callbackManager = CallbackManager.Factory.create()
        facebookBtn.setReadPermissions("email", "public_profile")
        facebookBtn.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("FACEBOOK_LOGIN", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("FACEBOOK_LOGIN", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d("FACEBOOK_LOGIN", "facebook:onError", error)
            }
        })




        // guest login


        sign_in_guest_button.setOnClickListener {
            mAuth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser

                        updateUIGuest(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SIGNIN_X", "signInAnonymously:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }



        // google login

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope(Scopes.PLUS_ME))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        sign_in_google_button.setOnClickListener {
            signInGoogle()
        }



        sign_up_link_button.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        sign_in_button.setOnClickListener {
            loginUser()
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("FACEBOOK_SIGNIN", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FACEBOOK_SIGNIN", "signInWithCredential:success")
                    val user = mAuth.currentUser

                    updateUIGoogle(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FACEBOOK_SIGNIN", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(
            signInIntent, RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //facebook
        if (loginWith == "facebook") {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        } else {

            //google
            if (requestCode == RC_SIGN_IN) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("GOOGLE_SIGNIN", "Google sign in failed", e)
                }
            }

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("GOOGLE_SIGNIN", "signInWithCredential:success")
                    val user = mAuth.currentUser

                    updateUIGoogle(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("GOOGLE_SIGNIN", "signInWithCredential:failure", task.exception)
                    updateUIGoogle(null)
                }
            }
    }

    //google save info
    private fun updateUIGoogle(user: FirebaseUser?) {

        if (user != null) {

            val currentUserID = mAuth.currentUser!!.uid
            val db = Firebase.firestore
            val dbRealtime = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")

            var usersRef = FirebaseFirestore.getInstance().collection("Users").document(currentUserID)

            val userMap = HashMap<String, Any>()
            userMap["uid"] = currentUserID
            userMap["username"] = user.email.toString()
            userMap["fullname"] = user.displayName.toString()
            userMap["email"] = user.email.toString()
            userMap["bio"] = "test"
//            userMap["image"] = user.photoUrl.toString()
            userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/socialmediaapp-4e61a.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=4ff557a8-4d95-474c-8df0-818616dac64d"



            usersRef.addSnapshotListener(object : EventListener<DocumentSnapshot> {

                override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    if (!value!!.exists()) {


                        Log.d(TAG, "VLEGOV VO IFOT ETE GO VALUE $value")

                        db.collection("Users")
                            .document(currentUserID).set(userMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@SignInActivity, "Signed In successfully.", Toast.LENGTH_LONG).show()

                                    dbRealtime.reference
                                        .child("Follow").child(currentUserID)
                                        .child("Following").child(currentUserID)
                                        .setValue(true)

                                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val errorMessage = task.exception!!.toString()
                                    Log.d(TAG, "PERO $errorMessage")
                                    Toast.makeText(this@SignInActivity, "Error:  $errorMessage", Toast.LENGTH_LONG).show()
                                    FirebaseAuth.getInstance().signOut()
                                }
                            }

                    } else {

                        Log.d(TAG, "VLEGOV VO ELSOT ETE GO VALUE $value")

                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    }

                }

            })

        } else {
            Log.i("FACEBOOK_SIGNIN", "null e")
        }
    }


    // guest info
    private fun updateUIGuest(user: FirebaseUser?) {

        if (user != null) {

            val currentUserID = mAuth.currentUser!!.uid
            val db = Firebase.firestore
            val dbRealtime = FirebaseDatabase.getInstance("https://socialmediaapp-4e61a-default-rtdb.europe-west1.firebasedatabase.app")
            val currentTimestamp : Long = java.sql.Timestamp(System.currentTimeMillis()).time

            var usersRef = FirebaseFirestore.getInstance().collection("Users").document(currentUserID)

            val userMap = HashMap<String, Any>()
            userMap["uid"] = currentUserID
            userMap["username"] = "u$currentTimestamp"
            userMap["fullname"] = "u$currentTimestamp"
            userMap["email"] = "u$currentTimestamp"
            userMap["bio"] = "test"
//            userMap["image"] = user.photoUrl.toString()
            userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/socialmediaapp-4e61a.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=4ff557a8-4d95-474c-8df0-818616dac64d"



            usersRef.addSnapshotListener(object : EventListener<DocumentSnapshot> {

                override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    if (!value!!.exists()) {


                        Log.d(TAG, "VLEGOV VO IFOT ETE GO VALUE $value")

                        db.collection("Users")
                            .document(currentUserID).set(userMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@SignInActivity, "Signed In successfully.", Toast.LENGTH_LONG).show()

                                    dbRealtime.reference
                                        .child("Follow").child(currentUserID)
                                        .child("Following").child(currentUserID)
                                        .setValue(true)

                                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val errorMessage = task.exception!!.toString()
                                    Log.d(TAG, "PERO $errorMessage")
                                    Toast.makeText(this@SignInActivity, "Error:  $errorMessage", Toast.LENGTH_LONG).show()
                                    FirebaseAuth.getInstance().signOut()
                                }
                            }

                    } else {

                        Log.d(TAG, "VLEGOV VO ELSOT ETE GO VALUE $value")

                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    }

                }

            })

        } else {
            Log.i("FACEBOOK_SIGNIN", "null e")
        }
    }





    private fun loginUser() {
        val email = email_sign_in.text.toString()
        val password = password_sign_in.text.toString()

        when {
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required.", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this@SignInActivity)
                progressDialog.setTitle("Signing In")
                progressDialog.setMessage("Please wait...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressDialog.dismiss()

                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
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
    }


    override fun onStart() {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}