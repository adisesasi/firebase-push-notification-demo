package com.example.notificc

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(viewModel: NotificationViewModel) {
    val context = LocalContext.current
    var token by remember { mutableStateOf("Loading...") }
    var expanded by remember { mutableStateOf(false) }

    val clipboard = LocalClipboardManager.current

    // Ambil FCM Token
    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            token = if (task.isSuccessful) task.result else "Gagal mendapatkan token"
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 24.dp, end = 16.dp, start = 16.dp)
    ) {
        Text("Your Device Token:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(token))
                    Toast
                        .makeText(context, "Token disalin", Toast.LENGTH_SHORT)
                        .show()
                }
                .background(Color(0xFFEFEFEF))
                .padding(12.dp)
        ) {
            Text(token, maxLines = if (expanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis)
        }
        TextButton(
            onClick = { expanded = !expanded }
        ) {
            Text(if (expanded) "Collapse" else "Expand")
        }

        Spacer(Modifier.height(6.dp))
        NotificationHistoryList()
    }
}

@Composable
fun NotificationHistoryList() {
    val context = LocalContext.current
    val history = remember { mutableStateListOf<NotificationEntry>() }

    // Bisa refresh kalau kamu punya tombol refresh
    LaunchedEffect(Unit) {
        history.clear()
        history.addAll(NotificationStorage.getNotificationHistory(context))
    }

    DisposableEffect(Unit) {
        val listener: (String, String) -> Unit = { title, message ->
            val updated = NotificationStorage.getNotificationHistory(context)
            history.clear()
            history.addAll(updated)
        }

        NotificationReceiver.onNotificationReceived = listener

        onDispose {
            NotificationReceiver.onNotificationReceived = null
        }
    }

    Column() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Riwayat Notifikasi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = {
                NotificationStorage.clearNotificationHistory(context)
                history.clear()
            }) {
                Text("Clear")
            }
        }

        if (history.isEmpty()) {
            Text("Belum ada notifikasi.")
        } else {
            LazyColumn {
                items(history) { entry ->
                    val formattedTime = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                        .format(Date(entry.receivedAt))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(shape = RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFEFEF))
                            .padding(12.dp)

                    ) {
                        Text(entry.title, fontWeight = FontWeight.Bold)
                        Text(entry.message)
                        Text(formattedTime, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

