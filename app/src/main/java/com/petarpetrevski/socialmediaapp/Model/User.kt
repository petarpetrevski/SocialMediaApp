package com.petarpetrevski.socialmediaapp.Model

class User {
    private var uid: String = ""
    private var fullname: String = ""
    private var username: String = ""
    private var email: String = ""
    private var bio: String = ""
    private var image: String = ""

    constructor()

    constructor(uid: String, fullname: String, username: String, email: String, bio: String, image: String) {
        this.uid = uid
        this.fullname = fullname
        this.username = username
        this.email = email
        this.bio = bio
        this.image = image
    }

    // GETTERS

    fun getUid(): String {
        return uid
    }

    fun getFullname(): String {
        return fullname
    }

    fun getUsername(): String {
        return username
    }

    fun getEmail(): String {
        return email
    }

    fun getBio(): String {
        return bio
    }

    fun getImage(): String {
        return image
    }


    // SETTERS

    fun setUid(uid: String) {
        this.uid = uid
    }

    fun setFullname(fullname: String) {
        this.fullname = fullname
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun setBio(bio: String) {
        this.bio = bio
    }

    fun setImage(image: String) {
        this.image = image
    }
}