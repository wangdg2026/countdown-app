package com.example.countdown.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdown.data.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    event: Event?,
    canAddMore: Boolean,
    onSave: (Event) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf(event?.title ?: "") }
    var note by remember { mutableStateOf(event?.note ?: "") }
    var target by remember {
        mutableStateOf(event?.targetTime ?: (System.currentTimeMillis() + 24L * 3600 * 1000))
    }
    var remindDays by remember { mutableStateOf(event?.remindDays ?: 0) }
    var error by remember { mutableStateOf<String?>(null) }
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    fun pickDateTime() {
        val cal = Calendar.getInstance().apply { timeInMillis = target }
        DatePickerDialog(ctx, { _, y, m, d ->
            cal.set(Calendar.YEAR, y)
            cal.set(Calendar.MONTH, m)
            cal.set(Calendar.DAY_OF_MONTH, d)
            TimePickerDialog(ctx, { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                target = cal.timeInMillis
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (event == null) "添加倒计时" else "编辑倒计时") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!canAddMore) {
                Text(
                    "免费版最多 5 个倒计时，请先升级 VIP",
                    color = MaterialTheme.colorScheme.error
                )
            }
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("标题（必填）") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("备注") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fmt.format(Date(target)),
                onValueChange = {}, readOnly = true,
                label = { Text("目标时间") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { pickDateTime() }, modifier = Modifier.fillMaxWidth()) {
                Text("选择目标日期和时间")
            }
            Text("提前提醒", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 2, 3).forEach { d ->
                    FilterChip(
                        selected = remindDays == d,
                        onClick = { remindDays = d },
                        label = { Text(if (d == 0) "不提前" else "提前${d}天") }
                    )
                }
            }
            if (remindDays > 0) {
                Text(
                    "将在 ${fmt.format(Date(target - remindDays * 24L * 3600 * 1000))} 提醒",
                    fontSize = 12.sp, color = Color.Gray
                )
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (title.isBlank()) { error = "请填写标题"; return@Button }
                    if (!canAddMore) { error = "免费版最多 5 个，请升级 VIP"; return@Button }
                    if (target <= System.currentTimeMillis()) {
                        error = "目标时间必须晚于当前时间"; return@Button
                    }
                    onSave(
                        Event(
                            id = event?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            note = note.trim(),
                            targetTime = target,
                            remindDays = remindDays
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("保存") }
        }
    }
}