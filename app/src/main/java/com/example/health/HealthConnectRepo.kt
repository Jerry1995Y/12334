package com.example.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.model.DaySteps
import com.example.model.HealthSummary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class HealthConnectRepo(private val context: Context) {

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    fun getClient(): HealthConnectClient? {
        return try {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                HealthConnectClient.getOrCreate(context)
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = getClient() ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(requiredPermissions)
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun getGrantedPermissions(): Set<String> {
        val client = getClient() ?: return emptySet()
        return try {
            client.permissionController.getGrantedPermissions()
        } catch (_: Throwable) {
            emptySet()
        }
    }

    suspend fun readTodayHealth(): HealthSummary? {
        val client = getClient() ?: return null
        if (!hasAllPermissions()) return null

        try {
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val startOfDay = today.atStartOfDay(zone).toInstant()
            val now = Instant.now()

            // 1. Aggregate Steps, Distance, Calories
            val aggRequest = AggregateRequest(
                metrics = setOf(
                    StepsRecord.COUNT_TOTAL,
                    DistanceRecord.DISTANCE_TOTAL,
                    TotalCaloriesBurnedRecord.ENERGY_TOTAL
                ),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
            )
            val aggResult = client.aggregate(aggRequest)
            val steps = aggResult[StepsRecord.COUNT_TOTAL] ?: 0L
            val distanceMeters = aggResult[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
            val distanceKm = (distanceMeters / 1000.0 * 10.0).roundToInt() / 10.0
            val calories = aggResult[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toLong() ?: 0L

            // 2. Read Sleep Session in last 24h
            val sleep24hAgo = now.minus(24, ChronoUnit.HOURS)
            val sleepRecords = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(sleep24hAgo, now)
                )
            ).records

            var sleepHours = 0.0
            var sleepStart = ""
            var sleepEnd = ""
            if (sleepRecords.isNotEmpty()) {
                val lastSleep = sleepRecords.last()
                val durationSec = ChronoUnit.SECONDS.between(lastSleep.startTime, lastSleep.endTime)
                sleepHours = ((durationSec / 3600.0) * 10.0).roundToInt() / 10.0
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(zone)
                sleepStart = timeFormatter.format(lastSleep.startTime)
                sleepEnd = timeFormatter.format(lastSleep.endTime)
            }

            // 3. Read Heart Rate in last 12h
            val heart12hAgo = now.minus(12, ChronoUnit.HOURS)
            val heartRecords = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(heart12hAgo, now)
                )
            ).records

            var heartRateBpm = 0
            if (heartRecords.isNotEmpty()) {
                val allSamples = heartRecords.flatMap { it.samples }
                if (allSamples.isNotEmpty()) {
                    heartRateBpm = allSamples.map { it.beatsPerMinute }.average().roundToInt()
                }
            }

            // 4. Read last 7 days steps
            val weekSteps = readWeekSteps(client, zone)

            return HealthSummary(
                steps = steps,
                distanceKm = distanceKm,
                caloriesKcal = calories,
                sleepHours = sleepHours,
                sleepStart = sleepStart,
                sleepEnd = sleepEnd,
                heartRateBpm = heartRateBpm,
                weekSteps = weekSteps,
                sourceLabel = "Health Connect（系统健康数据）"
            )
        } catch (_: Throwable) {
            return null
        }
    }

    private suspend fun readWeekSteps(client: HealthConnectClient, zone: ZoneId): List<DaySteps> {
        val list = mutableListOf<DaySteps>()
        val today = LocalDate.now(zone)
        val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val start = date.atStartOfDay(zone).toInstant()
            val end = date.plusDays(1).atStartOfDay(zone).toInstant()
            val label = dateFormatter.format(date)

            val count = try {
                val result = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(start, end)
                    )
                )
                result[StepsRecord.COUNT_TOTAL] ?: 0L
            } catch (_: Throwable) {
                0L
            }
            list.add(DaySteps(dateLabel = label, steps = count))
        }
        return list
    }
}
