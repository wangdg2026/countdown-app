package com.example.countdown.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "countdown")

class EventRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val EVENTS_KEY = stringPreferencesKey("events")
    private val VIP_EXPIRE_KEY = longPreferencesKey("vip_expire_at")
    private val COOLDOWNS_KEY = stringPreferencesKey("code_cooldowns")

    val events: Flow<List<Event>> = context.dataStore.data.map { prefs ->
        val raw = prefs[EVENTS_KEY] ?: "[]"
        runCatching { json.decodeFromString<List<Event>>(raw) }.getOrDefault(emptyList())
    }

    val vipExpireAt: Flow<Long> = context.dataStore.data.map { it[VIP_EXPIRE_KEY] ?: 0L }

    suspend fun saveEvents(list: List<Event>) {
        context.dataStore.edit { it[EVENTS_KEY] = json.encodeToString(list) }
    }

    suspend fun setVipExpireAt(value: Long) {
        context.dataStore.edit { it[VIP_EXPIRE_KEY] = value }
    }

    suspend fun getCooldowns(): Map<String, Long> {
        val prefs = context.dataStore.data.first()
        val raw = prefs[COOLDOWNS_KEY] ?: "{}"
        return runCatching { json.decodeFromString<Map<String, Long>>(raw) }
            .getOrDefault(emptyMap())
    }

    suspend fun setCooldown(code: String, time: Long) {
        val current = getCooldowns().toMutableMap()
        current[code] = time
        context.dataStore.edit { it[COOLDOWNS_KEY] = json.encodeToString(current) }
    }
}