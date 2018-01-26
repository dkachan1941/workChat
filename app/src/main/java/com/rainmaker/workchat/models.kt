package com.rainmaker.workchat

/**
 * models
 */
//data class ChatModel(var id: String? = null, var name: String, var status: Int)

class ChatModel {

    var id: String? = null
    var name: String? = null
    var status: Int? = null
    var messageCount: Int? = null
    var creator: String? = null
    var date: String? = null
    var users: ArrayList<String?> = arrayListOf()

    constructor() {}

    constructor(name: String, status: Int, date: String, creator: String, users: ArrayList<String?>) {
        this.name = name
        this.status = status
        this.date = date
        this.creator = creator
        this.users = users
    }
}

class MessageModel {

    var id: String? = null
    var text: String? = null
    var name: String? = null
    var photoUrl: String? = null
    var imageUrl: String? = null

    constructor() {}

    constructor(text: String?, name: String?, photoUrl: String?, imageUrl: String?) {
        this.text = text
        this.name = name
        this.photoUrl = photoUrl
        this.imageUrl = imageUrl
    }
}
