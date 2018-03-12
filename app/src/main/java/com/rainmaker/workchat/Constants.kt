package com.rainmaker.workchat

/**
 * Created by dmitry on 2/4/18.
 *
 */

// config
const val MY_MESSAGE_COLOR = R.color.green_light
const val FOREIGN_MESSAGE_COLOR = R.color.white
const val TAG = "WorkChat"

// chat
const val DEFAULT_MSG_LENGTH_LIMIT = 35

// codes
const val CODE_SELECT_USER = 3456
const val CODE_CHANGE_USERS = 3457
const val REQUEST_INVITE = 3458
const val REQUEST_IMAGE = 3459

// id's
const val USER_NAME = "user_name"
const val USER_ID = "user_id"
const val USER_UID = "user_uid"
const val CHAT_ID = "chat_id"
const val CHAT_NAME = "chatName"


// fireBase db
const val CHILD_ROOMS = "rooms"
const val CHILD_USERS = "users"
const val FIELD_IS_PUBLIC = "private"
const val CHILD_MESSAGES = "messages"
const val CHILD_NEW_MESSAGES = "newMessages"

// fireBase
const val RC_SIGN_IN = 9001
const val NOTIFICATIONS_TOPIC = "notifications"

// auth
const val ANONYMOUS = "anonymous"

// links
const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"

// notifications
const val PUSH_NOTIFICATION = "pushNotification"
const val NOTIFICATION_ID_BIG_IMAGE = 101
const val NOTIFICATION_ID = 100
const val NOTIFICATION_CHANNEL_ONE_ID = "com.rainmaker.WorkChat.ONE"
const val NOTIFICATION_CHANNEL_ONE_NAME = "WorkChat"
const val NOTIFICATION_TITLE = "notification_title"
const val NOTIFICATION_JOB_TAG = "notification-job-tag"

// shared preferences
const val PREFERENCES_NAME = "safePrefsName"

