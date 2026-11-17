package com.example.countdown.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.countdown.data.Event
import com.example.countdown.vm.EventViewModel
import kotlinx.coroutines.delay

sealed interface Screen {
    data object EventList : Screen
    data class EditEvent(val event: Event?) : Screen
    data object VipCenter : Screen
}

@Composable
fun AppRoot(vm: EventViewModel) {
    var screen by remember { mutableStateOf<Screen>(Screen.EventList) }
    val events by vm.events.collectAsState()
    val vipExpire by vm.vipExpireAt.collectAsState()
    val isVip = System.currentTimeMillis() < vipExpire

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    MaterialTheme {
        when (val s = screen) {
            is Screen.EventList -> EventListScreen(
                events = events,
                isVip = isVip,
                now = now,
                onAdd = { screen = Screen.EditEvent(null) },
                onEdit = { screen = Screen.EditEvent(it) },
                onDelete = { vm.deleteEvent(it) },
                onVip = { screen = Screen.VipCenter }
            )
            is Screen.EditEvent -> EditEventScreen(
                event = s.event,
                canAddMore = isVip || s.event != null || events.size < 5,
                onSave = { vm.saveEvent(it); screen = Screen.EventList },
                onBack = { screen = Screen.EventList }
            )
            is Screen.VipCenter -> VipScreen(
                isVip = isVip,
                vipExpire = vipExpire,
                onActivate = { permanent -> vm.activateVip(permanent) },
                onRedeem = { code -> vm.redeemCode(code) },
                onBack = { screen = Screen.EventList }
            )
        }
    }
}