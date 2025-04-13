package com.thirdworlds.wakeonlan.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thirdworlds.wakeonlan.data.converter.EncryptorConverter
import com.thirdworlds.wakeonlan.data.converter.ZonedDateTimeConverter
import com.thirdworlds.wakeonlan.data.dao.LinkDao
import com.thirdworlds.wakeonlan.data.domain.Link

@Database(entities = [Link::class], version = 1, exportSchema = true)
@TypeConverters(ZonedDateTimeConverter::class, EncryptorConverter::class) // 注册 TypeConverter
abstract class AppDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao
}