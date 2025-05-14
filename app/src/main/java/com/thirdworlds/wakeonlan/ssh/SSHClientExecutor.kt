package com.thirdworlds.wakeonlan.ssh

import android.content.Context
import android.util.Log
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.util.OsUtils
import org.apache.sshd.common.util.io.PathUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.security.KeyPair
import java.security.Security
import java.time.Duration
import java.util.EnumSet


class SSHClientExecutor {
    private var client: SshClient? = null
    private var session: ClientSession? = null

    fun init(context: Context) {
        if (client != null) return
        updateBcLibrary()
        setWorkPath(context)
        client = SshClient.setUpDefaultClient().apply {}
        client?.start()
    }

    private fun updateBcLibrary() {
        Security.getProvider("BC")?.let {
            Security.removeProvider("BC")
        }
        val bcProvider = BouncyCastleProvider()
        Security.addProvider(bcProvider)
    }

    private fun setWorkPath(context: Context) {
        val filesDir: File = context.filesDir
        val filesPath: Path = filesDir.toPath()
        System.setProperty("user.home", filesPath.toString()) // just in case
        PathUtils.setUserHomeFolderResolver { filesPath }
        System.setProperty("user.dir", filesPath.toString()) // just in case
        OsUtils.setCurrentWorkingDirectoryResolver { filesPath }
    }

    /**
     * 密码登录连接
     */
    fun connectPasswd(host: String, port: Int, username: String, password: String) {
        session = client?.connect(username, host, port)
            ?.verify(3000)
            ?.session
        session?.addPasswordIdentity(password)
        session?.auth()?.verify(3000)
    }

    /**
     * 私钥登录连接
     */
    fun connectKey(host: String, port: Int, username: String, privateKey: String) {
        session = client?.connect(username, host, port)
            ?.verify(3000)
            ?.session
        val keyPair: KeyPair = PemKeyLoader.loadKeyPair(privateKey)
        session?.addPublicKeyIdentity(keyPair)
        session?.auth()?.verify(3000)
    }

    // 执行单个命令并返回命令输出
    fun executeCommand(command: String): String {
        ByteArrayOutputStream().use { out ->
            ByteArrayOutputStream().use { err ->
                session!!.createExecChannel(command).use { channel ->
                    channel.out = out
                    channel.setErr(err)

                    channel.open().verify(Duration.ofSeconds(10))
                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), Duration.ofSeconds(30))

                    val stdout = out.toString().trim()
                    val stderr = err.toString().trim()

                    if (stderr.isNotEmpty()) {
                        return "[stderr] $stderr\n[stdout] $stdout"
                    }
                    return stdout
                }
            }
        }
    }

    // 显式关闭连接
    fun close() {
        try {
            // 关闭 session
            session?.close()
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "关闭 Session 时发生异常: ${e.message}")
        }

        try {
            // 关闭 client
            client?.stop()
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "关闭 SshClient 时发生异常: ${e.message}")
        }
    }
}