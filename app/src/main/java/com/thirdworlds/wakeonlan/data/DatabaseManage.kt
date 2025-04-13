package com.thirdworlds.wakeonlan.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thirdworlds.wakeonlan.content.ContextHolder

object DatabaseManage {
    private var appDatabase: AppDatabase? = null
    private val lock = Any()

    @Synchronized
    fun getDataBase(context: Context): AppDatabase {
        if (appDatabase == null) {
            synchronized(lock) {
                if (appDatabase == null) {
                    appDatabase = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "app_database"
                    ).addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            ContextHolder.init(context) // 初始化
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            if (!isContextInitialized()) {
                                ContextHolder.init(context)
                            }
                        }

                        private fun isContextInitialized(): Boolean {
                            return try {
                                ContextHolder.get()
                                true
                            } catch (e: Exception) {
                                when (e) {
                                    is IllegalStateException, is UninitializedPropertyAccessException -> {
                                        return false
                                    }

                                    else -> throw e
                                }
                            }
                        }
                    }).build()
                }
            }
        }
        return appDatabase!!
    }
}
