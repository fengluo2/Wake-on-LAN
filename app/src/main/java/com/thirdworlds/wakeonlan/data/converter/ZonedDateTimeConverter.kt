package com.thirdworlds.wakeonlan.data.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ZonedDateTimeConverter {

    @TypeConverter
    fun fromZonedDateTime(date: ZonedDateTime?): Long? {
        return date?.toInstant()?.toEpochMilli() // 将 ZonedDateTime 转换为时间戳（Long）
    }

    @TypeConverter
    fun toZonedDateTime(timestamp: Long?): ZonedDateTime? {
        return timestamp?.let {
            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC)
        } // 将时间戳转回 ZonedDateTime
    }
}