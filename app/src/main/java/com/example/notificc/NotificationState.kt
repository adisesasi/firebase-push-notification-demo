package com.example.notificc

class NotificationState {
}

data class NotificationItem(
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)