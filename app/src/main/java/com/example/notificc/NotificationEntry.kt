package com.example.notificc

data class NotificationEntry(
    val title: String,
    val message: String,
    val receivedAt: Long, // timestamp
    val imageUrl: String? = null // ← Tambahkan ini
)
