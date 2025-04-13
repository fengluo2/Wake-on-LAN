package com.thirdworlds.wakeonlan.type

@JvmInline
value class EncryptedString(private val data: String?) {
    fun getData(): String? = data
}
