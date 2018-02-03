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
    var users:  HashMap<String, String> = HashMap()

    constructor() {}

    constructor(name: String, status: Int, date: String, creator: String, users: HashMap<String, String>) {
        this.name = name
        this.status = status
        this.date = date
        this.creator = creator
        this.users = users
    }
}

class ChatModel1 {

    var id: String? = null
    var name: String? = null
    var status: Int? = null
    var messageCount: Int? = null
    var creator: String? = null
    var date: String? = null
    var users:  HashMap<String, String> = HashMap()
    var key: String? = null

    constructor() {}

    constructor(name: String, status: Int, date: String, creator: String, users: HashMap<String, String>, id: String, key: String) {
        this.name = name
        this.status = status
        this.date = date
        this.creator = creator
        this.users = users
        this.id = id
        this.key = key
    }
}

class RoomUser{
    var id: String? = null
    var value: String? = null

    constructor() {}

    constructor(value: String?) {
        this.value = value
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
