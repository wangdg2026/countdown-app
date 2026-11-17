package com.example.countdown.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.countdown.data.Event
import com.example.countdown.data.EventRepository
import com.example.countdown.data.VipCodes
import com.example.countdown.notify.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class RedeemResult {
    data class Success(val message: String) : RedeemResult()
    data class Invalid(val message: String) : RedeemResult()
}

class EventViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = EventRepository(app)
    private val ctx = app.applicationContext

    val events: StateFlow<List<Event>> =
        repo.events.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vipExpireAt: StateFlow<Long> =
        repo.vipExpireAt.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun saveEvent(event: Event) {
        viewModelScope.launch {
            val current = events.value.toMutableList()
            val idx = current.indexOfFirst { it.id == event.id }
            if (idx >= 0) {
                current[idx] = event
            } else {
                val vip = System.currentTimeMillis() < vipExpireAt.value
                if (!vip && current.size >= 5) return@launch
                current.add(event)
            }
            repo.saveEvents(current)
            ReminderScheduler.schedule(ctx, event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repo.saveEvents(events.value.filterNot { it.id == event.id })
            ReminderScheduler.cancel(ctx, event)
        }
    }

    fun activateVip(permanent: Boolean) {
        viewModelScope.launch { applyVip(permanent) }
    }

    suspend fun redeemCode(input: String): RedeemResult {
        val code = input.trim().uppercase()
        if (code.isEmpty()) return RedeemResult.Invalid("请输入兑换码")

        val now = System.currentTimeMillis()
        val tenDays = 10L * 24 * 3600 * 1000
        val cooldowns = repo.getCooldowns()
        val last = cooldowns[code] ?: 0L
        if (now - last < tenDays) {
            val remain = tenDays - (now - last)
            val days = (remain / (24 * 3600 * 1000)).coerceAtLeast(1)
            return RedeemResult.Invalid("该兑换码使用后需间隔 10 天，请 ${days} 天后再试")
        }

        return when {
            VipCodes.isYearCode(code) -> {
                repo.setCooldown(code, now)
                applyVip(false)
                RedeemResult.Success("按年 VIP 兑换成功")
            }
            VipCodes.isPermanentCode(code) -> {
                repo.setCooldown(code, now)
                applyVip(true)
                RedeemResult.Success("永久 VIP 兑换成功")
            }
            else -> RedeemResult.Invalid("兑换码无效")
        }
    }

    private suspend fun applyVip(permanent: Boolean) {
        val now = System.currentTimeMillis()
        val current = vipExpireAt.value
        val newExpire = if (permanent) Long.MAX_VALUE
        else maxOf(current, now + 365L * 24 * 3600 * 1000)
        repo.setVipExpireAt(newExpire)
    }
}