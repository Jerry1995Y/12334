package com.example.model

data class TaskItem(
    val id: Long,
    val text: String,
    val done: Boolean = false
)

data class ExpenseItem(
    val id: Long,
    val amount: Double,
    val cat: String,
    val note: String,
    val ts: Long
)

data class ShoppingItem(
    val id: Long,
    val text: String,
    val group: String, // "食材" or "日用品"
    val done: Boolean = false
)

data class CommutePlanItem(
    val id: Long,
    val text: String,
    val done: Boolean = false
)

data class DaySteps(
    val dateLabel: String, // e.g. "10/16"
    val steps: Long
)

data class HealthSummary(
    val steps: Long,
    val distanceKm: Double,
    val caloriesKcal: Long,
    val sleepHours: Double,
    val sleepStart: String,
    val sleepEnd: String,
    val heartRateBpm: Int,
    val weekSteps: List<DaySteps>,
    val sourceLabel: String
)
