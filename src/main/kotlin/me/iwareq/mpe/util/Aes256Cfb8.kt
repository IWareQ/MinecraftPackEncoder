package me.iwareq.mpe.util

import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.CFBBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Aes256Cfb8 {

	private const val TRANSFORMATION = "AES/CFB8/NoPadding"

	@JvmStatic
	@Throws(Exception::class)
	fun encrypt(key: ByteArray, buffer: ByteArray): ByteArray {
		val cipher = Cipher.getInstance(TRANSFORMATION)
		val ivParameterSpec = this.generateIvParameterSpec(key, true)
		cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), ivParameterSpec)
		return cipher.doFinal(buffer)
	}

	@JvmStatic
	@Throws(Exception::class)
	fun decrypt(key: ByteArray, buffer: ByteArray): ByteArray {
		val cipher = Cipher.getInstance(TRANSFORMATION)
		val ivParameterSpec = this.generateIvParameterSpec(key, false)
		cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), ivParameterSpec)
		return cipher.doFinal(buffer)
	}

	private fun generateIvParameterSpec(key: ByteArray, encrypt: Boolean): IvParameterSpec {
		val cfbCipher = CFBBlockCipher(AESEngine(), 128)

		val keyParam = KeyParameter(key)
		val iv = key.copyOfRange(0, 16)

		val params = ParametersWithIV(keyParam, iv)
		cfbCipher.init(encrypt, params)

		return IvParameterSpec(iv)
	}
}
