package com.example.notificc

object NotificationReceiver {
    var onNotificationReceived: ((String, String) -> Unit)? = null
}