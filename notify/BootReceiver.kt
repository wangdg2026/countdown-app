package com.example.countdown.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.countdown.data.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val repo = EventRepository(context)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repo.events.first().forEach { ReminderScheduler.schedule(context, it) }
            } finally {
                pending.finish()
            }
        }
    }
}