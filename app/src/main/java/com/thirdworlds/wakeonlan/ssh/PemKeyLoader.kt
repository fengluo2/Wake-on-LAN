package com.thirdworlds.wakeonlan.ssh

import org.apache.sshd.common.NamedResource
import org.apache.sshd.common.config.keys.FilePasswordProvider
import org.apache.sshd.common.util.security.SecurityUtils
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.StringReader
import java.security.KeyPair
import java.security.Security

object PemKeyLoader {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @Throws(Exception::class)
    fun loadKeyPair(pemString: String): KeyPair {
        val header = pemString.trim().lineSequence().firstOrNull()
            ?: throw IllegalArgumentException("PEM content is empty or invalid")

        return when {
            header.contains("OPENSSH PRIVATE KEY") -> loadOpenSSHKey(pemString)
            header.contains("RSA PRIVATE KEY") || header.contains("PRIVATE KEY") -> loadBouncyCastleKey(
                pemString
            )

            else -> throw IllegalArgumentException("Unsupported key format: $header")
        }
    }

    private fun loadOpenSSHKey(pemString: String): KeyPair {
        val inputStream = pemString.byteInputStream()
        val keyPairs = SecurityUtils.loadKeyPairIdentities(
            null, // SessionContext 可为 null
            NamedResource.ofName("openssh-key"), // 简单的 resourceKey 实现
            inputStream,
            FilePasswordProvider.EMPTY // 如果需要支持加密的 PEM，可替换
        )

        return keyPairs.firstOrNull()
            ?: throw IllegalArgumentException("No key pair found in OpenSSH key")
    }

    private fun loadBouncyCastleKey(pemString: String): KeyPair {
        PEMParser(StringReader(pemString)).use { pemParser ->
            val obj = pemParser.readObject()
            val converter = JcaPEMKeyConverter().setProvider("BC")

            return when (obj) {
                is PEMKeyPair -> converter.getKeyPair(obj)
                is PrivateKeyInfo -> KeyPair(null, converter.getPrivateKey(obj))
                else -> throw IllegalArgumentException("Unsupported or unknown PEM object: ${obj?.javaClass}")
            }
        }
    }
}
