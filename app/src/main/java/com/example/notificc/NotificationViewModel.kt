package com.example.notificc
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class NotificationViewModel : ViewModel() {
    val notifications = mutableStateListOf<NotificationItem>()

    fun addNotification(title: String, message: String) {
        notifications.add(0, NotificationItem(title, message)) // add newest first
    }
}