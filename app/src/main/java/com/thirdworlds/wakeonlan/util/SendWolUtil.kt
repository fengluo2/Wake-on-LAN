package com.thirdworlds.wakeonlan.util

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getString
import com.thirdworlds.wakeonlan.R
import com.thirdworlds.wakeonlan.content.LinkType
import com.thirdworlds.wakeonlan.content.LoginType
import com.thirdworlds.wakeonlan.data.domain.Link
import com.thirdworlds.wakeonlan.ssh.SSHClientExecutor
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object SendWolUtil {
    val ipv4Regex = Regex("^((25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})(\\.|$)){4}$")
    val ipv6Regex =
        Regex("^(?:[\\da-fA-F]{1,4}:){7}[\\da-fA-F]{1,4}\$|^(?:[\\da-fA-F]{1,4}:){1,7}:|^(?:[\\da-fA-F]{1,4}:){1,6}:[\\da-fA-F]{1,4}\$")

    fun sendWal(link: Link, context: Context) {
        when (link.type) {
            LinkType.DIRECT.value -> {
                // 目标的广播地址
                val broadcast = InetAddress.getByName(link.directIp!!.getData()!!)
                // 将 MAC 地址转换为字节数组
                val macBytes = link.directMac!!.getData()!!.split(":").map { it.toInt(16).toByte() }.toByteArray()
                // 构建魔法包
                val magicPacket = ByteArray(102) // 魔法包的总大小是 102 字节
                // 前6个字节是 0xFF
                for (i in 0 until 6) {
                    magicPacket[i] = 0xFF.toByte()
                }
                // 后面 16 次重复 MAC 地址
                for (i in 0 until 16) {
                    System.arraycopy(macBytes, 0, magicPacket, 6 + i * 6, macBytes.size)
                }
                // 使用 DatagramSocket 发送魔法包
                val socket = DatagramSocket()
                socket.broadcast = true
                val packet = DatagramPacket(magicPacket, magicPacket.size, broadcast, 9) // 9 是 WOL 的默认端口
                socket.send(packet)
                Log.d(this::class.simpleName, "成功发送 WOL 包到 ${link.directIp} - ${link.directMac}")
                socket.close()
            }

            LinkType.PROXY.value -> {
                val ssh = SSHClientExecutor()
                val ip: String

                if (isIp(link.proxyAddress!!.getData()!!)) {
                    ip = link.proxyAddress!!.getData()!!
                } else {
                    val inetAddress = InetAddress.getByName(link.proxyAddress!!.getData()!!)
                    ip = inetAddress.hostAddress?.toString() ?: ""

                    if (ip.isEmpty()) {
                        ToastUtil.showToast(context, "无法解析域名${link.proxyAddress}")
                        return
                    }
                }

                ssh.init(context)
                when (link.proxyLoginType) {
                    LoginType.PASSWD.value -> {
                        try {
                            ssh.connectPasswd(
                                ip,
                                link.proxyPort!!,
                                link.proxyLoginUser!!.getData()!!,
                                link.proxyLoginPasswd!!.getData()!!
                            )
                        } catch (e: Exception) {
                            Log.e(this::class.simpleName, "ssh连接失败", e)
                        }
                    }

                    LoginType.PRIVATE.value -> {
                        ssh.connectKey(
                            ip,
                            link.proxyPort!!,
                            link.proxyLoginUser!!.getData()!!,
                            link.proxyLoginPrivate!!.getData()!!
                        )
                    }

                    else -> {
                        ToastUtil.showToast(context, "请选择登录方式")
                        ssh.close()
                        return
                    }
                }

                val distro =
                    ssh.executeCommand("grep '^NAME=' /etc/os-release | awk -F'=' '{gsub(/\"/, \"\", $2); print $2}'")
                val version =
                    ssh.executeCommand("grep '^VERSION=' /etc/os-release | awk -F'=' '{gsub(/\"/, \"\", $2); print $2}'")

                Log.d(this::class.simpleName, "${distro}-${version}")
                val packageManage = getLinuxSysPackageManage(distro.lowercase())
                if (packageManage == "unknown") {
                    ssh.close()
                    ToastUtil.showToast(
                        context,
                        String.format(getString(context, R.string.toast_not_support_system_info), distro.lowercase())
                    )
                    return
                }
                val commandMap: Map<String, String> =
                    loadXmlAndParse(context, "linux_${packageManage}")

                if (commandMap.isEmpty() ||
                    !commandMap.containsKey("checkout_installed") ||
                    !commandMap.containsKey("install_wol") ||
                    !commandMap.containsKey("send_wol")
                ) {
                    ToastUtil.showToast(context, getString(context, R.string.toast_not_support_system))
                    ssh.close()
                    return
                }

                val hasWol = ssh.executeCommand(commandMap["checkout_installed"]!!).isNotEmpty()

                if (!hasWol && !commandMap.containsKey("checkout_installed")) {
                    ssh.executeCommand(commandMap["install_wol"]!!)
                }

                val sendCommand =
                    String.format(commandMap["send_wol"]!!, link.directIp!!.getData(), link.directMac!!.getData())
                ssh.executeCommand(sendCommand)

                ssh.close()
            }
        }
    }

    private fun getLinuxSysPackageManage(distro: String): String {
        return when (distro) {
            "ubuntu", "debian" -> "apt"
            "openwrt" -> "opkg"
            else ->
                "unknown"

        }
    }

    private fun isIp(address: String): Boolean {
        return ipv4Regex.matches(address) || ipv6Regex.matches(address)
    }

    private fun loadXmlAndParse(context: Context, system: String): Map<String, String> {
        // 动态生成文件名
        val fileName = "terminal/${system}.xml"

        // 尝试打开文件
        val inputStream: InputStream = try {
            context.assets.open(fileName) // 从 assets 中读取文件
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyMap() // 如果文件加载失败，返回空 Map
        }

        // 使用 XmlPullParser 解析 XML 文件
        val keyValues = mutableMapOf<String, String>()

        try {
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            var key = ""
            var value = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "string") {
                            key = parser.getAttributeValue(null, "name")
                        }
                    }

                    XmlPullParser.TEXT -> {
                        value = parser.text
                    }

                    XmlPullParser.END_TAG -> {
                        if (parser.name == "string") {
                            keyValues[key] = value
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream.close()
        }

        return keyValues
    }
}