package com.example.countdown.data

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val title: String,
    val note: String = "",
    val targetTime: Long,
    val remindDays: Int = 0
)