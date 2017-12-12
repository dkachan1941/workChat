package com.rainmaker.workchat

/**
 * models
 */
//data class ChatModel(var id: String? = null, var name: String, var status: Int)

class ChatModel {

    var id: String? = null
    var name: String? = null
    var status: Int? = null

    constructor() {}

    constructor(name: String, status: Int) {
        this.name = name
        this.status = status
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
