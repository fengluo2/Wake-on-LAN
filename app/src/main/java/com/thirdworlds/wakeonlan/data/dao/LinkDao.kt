package com.thirdworlds.wakeonlan.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.thirdworlds.wakeonlan.data.domain.Link
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {
    @Query("select * from link order by createTime desc")
    fun loadAll(): Flow<List<Link>>?

    @Query("select * from link where id = (:id)")
    fun loadById(id: Int): Flow<Link>

    @Insert
    suspend fun insertAll(vararg links: Link)

    @Update
    suspend fun updateAll(vararg links: Link)

    @Query("delete from link where id in (:ids)")
    suspend fun deleteByIds(vararg ids: Int)

    @Query("delete from link")
    suspend fun deleteAll()
}