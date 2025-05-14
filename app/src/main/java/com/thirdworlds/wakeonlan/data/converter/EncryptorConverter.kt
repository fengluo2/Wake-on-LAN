package com.thirdworlds.wakeonlan.data.converter

import androidx.room.TypeConverter
import com.thirdworlds.wakeonlan.type.EncryptedString
import com.thirdworlds.wakeonlan.util.EncryptorUtil

class EncryptorConverter {

    @TypeConverter
    fun fromEncrypted(value: String?): EncryptedString {
        // 解密操作
        val data = EncryptorUtil.decrypt(value)
        return EncryptedString(data)
    }

    @TypeConverter
    fun toEncrypted(value: EncryptedString?): String? {
        // 加密操作
        return EncryptorUtil.encrypt(value?.getData())
    }
}