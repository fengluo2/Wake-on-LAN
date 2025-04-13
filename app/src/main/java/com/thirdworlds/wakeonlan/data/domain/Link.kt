package com.thirdworlds.wakeonlan.data.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.thirdworlds.wakeonlan.type.EncryptedString
import java.time.ZonedDateTime

@Entity("link")
data class Link(
    /**
     * 唯一标识
     **/
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    /**
     * 名称
     */
    @ColumnInfo var name: String? = null,
    /**
     * 连接类型 直连 代理
     */
    @ColumnInfo var type: Int? = null,
    /**
     * 代理主机地址
     */
    @ColumnInfo var proxyAddress: EncryptedString? = null,
    /**
     * 代理主机端口
     */
    @ColumnInfo var proxyPort: Int? = null,
    /**
     * 代理登录类型
     */
    @ColumnInfo var proxyLoginType: Int? = null,
    /**
     * 代理登录用户
     */
    @ColumnInfo var proxyLoginUser: EncryptedString? = null,
    /**
     * 代理登录密码
     */
    @ColumnInfo var proxyLoginPasswd: EncryptedString? = null,
    /**
     * 代理登录私钥
     */
    @ColumnInfo var proxyLoginPrivate: EncryptedString? = null,
    /**
     * 主机IP
     */
    @ColumnInfo var directIp: EncryptedString? = null,
    /**
     * 主机MAC
     */
    @ColumnInfo var directMac: EncryptedString? = null,
    /**
     * 备注
     */
    @ColumnInfo var remark: String? = null,
    /**
     * 创建时间
     */
    @ColumnInfo var createTime: ZonedDateTime? = null,
    /**
     * 更新时间
     */
    @ColumnInfo var updateTime: ZonedDateTime? = null
)