package com.example.countdown.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdown.data.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    events: List<Event>,
    isVip: Boolean,
    now: Long,
    onAdd: () -> Unit,
    onEdit: (Event) -> Unit,
    onDelete: (Event) -> Unit,
    onVip: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("倒计时") },
                actions = {
                    TextButton(onClick = onVip) {
                        Text(if (isVip) "VIP 已激活" else "升级 VIP")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("还没有倒计时，点击右下角 + 添加", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events, key = { it.id }) { e ->
                    EventCard(e, now, onEdit = { onEdit(e) }, onDelete = { onDelete(e) })
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, now: Long, onEdit: () -> Unit, onDelete: () -> Unit) {
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    Card(Modifier.fillMaxWidth().clickable { onEdit() }) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (event.note.isNotBlank()) {
                    Text(event.note, color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    formatCountdown(event.targetTime, now),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "目标：${fmt.format(Date(event.targetTime))}" +
                        if (event.remindDays > 0) " · 提前${event.remindDays}天提醒" else "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

private fun formatCountdown(target: Long, now: Long): String {
    val diff = target - now
    if (diff <= 0) return "已到期"
    val days = diff / (24 * 3600 * 1000)
    val hours = (diff % (24 * 3600 * 1000)) / (3600 * 1000)
    val minutes = (diff % (3600 * 1000)) / (60 * 1000)
    val seconds = (diff % (60 * 1000)) / 1000
    return if (days > 0) "剩余 ${days}天 ${hours}小时 ${minutes}分"
    else "剩余 ${hours}小时 ${minutes}分 ${seconds}秒"
}