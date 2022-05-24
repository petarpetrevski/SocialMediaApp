package com.petarpetrevski.socialmediaapp.Fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.petarpetrevski.socialmediaapp.Adapter.UserAdapter
import com.petarpetrevski.socialmediaapp.Model.User
import com.petarpetrevski.socialmediaapp.R
import kotlinx.android.synthetic.main.fragment_search.view.*


class SearchFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: ArrayList<User>? = null
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter

        view.search_fragment_edit_text.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (view.search_fragment_edit_text.text.toString() == "") {
                    recyclerView?.visibility = View.GONE
                } else {
                    recyclerView?.visibility = View.VISIBLE

                    retrieveUsers()
                    searchUser(s.toString().lowercase())
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        return view
    }

    private fun searchUser(input: String) {

        FirebaseFirestore.getInstance().collection("Users")
            .orderBy("username").startAt(input).endAt(input + "\uf8ff")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {

                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    mUser?.clear()

                    for (dc : DocumentChange in value?.documentChanges!!){

                        mUser?.add(dc.document.toObject(User::class.java))

                    }

                    userAdapter?.notifyDataSetChanged()
                }

            })

    }

    private fun retrieveUsers() {
        db = FirebaseFirestore.getInstance()

        db.collection("Users").
            addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    mUser?.clear()

                    for (dc : DocumentChange in value?.documentChanges!!){
                        mUser?.add(dc.document.toObject(User::class.java))

                        if (context != null) {
                            userAdapter = UserAdapter(context!!, mUser!!)
                            recyclerView!!.adapter = userAdapter
                        }
                    }
                }
            })
    }
}