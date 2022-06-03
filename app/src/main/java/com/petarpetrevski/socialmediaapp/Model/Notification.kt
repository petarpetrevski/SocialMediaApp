package com.petarpetrevski.socialmediaapp.Model

class Notification {

    private var userID: String = ""
    private var text: String = ""
    private var postID: String = ""
    private var isPost = false

    constructor()
    constructor(userID: String, text: String, postID: String, isPost: Boolean) {
        this.userID = userID
        this.text = text
        this.postID = postID
        this.isPost = isPost
    }

    // GETTERS

    fun getUserID(): String {
        return userID
    }

    fun getText(): String {
        return text
    }

    fun getPostID(): String {
        return postID
    }

    fun isIsPost(): Boolean {
        return isPost
    }


    // SETTERS

    fun setUserID(userID: String) {
        this.userID = userID
    }

    fun setText(text: String) {
        this.text = text
    }

    fun setPostID(postID: String) {
        this.postID = postID
    }

    fun setIsPost(isPost: Boolean) {
        this.isPost = isPost
    }

}