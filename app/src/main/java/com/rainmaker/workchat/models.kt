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
    var password: String? = null
    var encryptionPw: String? = null
    var isPrivate: Boolean? = null

    constructor() {}

    constructor(name: String, status: Int, date: String, creator: String, users: HashMap<String, String>, password: String, encryptionPw: String, isPrivate: Boolean) {
        this.name = name
        this.status = status
        this.date = date
        this.creator = creator
        this.users = users
        this.password = password
        this.encryptionPw = encryptionPw
        this.isPrivate = isPrivate
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
    var visible: Boolean = true
    var isPrivate: Boolean? = null

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

class User{
    var id: String? = null
    var value: String? = null
    var name: String? = null
    var provider: String? = null
    var email: String? = null
    var uuid: String? = null

    constructor() {}

    constructor(uuid: String?, name: String?) {
        this.name = name
        this.uuid = uuid
    }
}

class MessageModel {

    var id: String? = null
    var text: String? = null
    var name: String? = null
    var photoUrl: String? = null
    var imageUrl: String? = null
    var userUid: String? = null

    constructor() {}

    constructor(text: String?, name: String?, photoUrl: String?, imageUrl: String?, userUid: String?) {
        this.text = text
        this.name = name
        this.photoUrl = photoUrl
        this.imageUrl = imageUrl
        this.userUid = userUid
    }
}
