package me.iwareq.mcperpencrdec.util

import java.security.SecureRandom

object KeyGenerator {

	private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

	private const val KEY_LENGTH = 32

	private val RANDOM = SecureRandom()

	@JvmStatic
	fun generateKey(): String {
		val keyBuilder = StringBuilder(KEY_LENGTH)
		for (i in 0 until KEY_LENGTH) keyBuilder.append(CHARACTERS[RANDOM.nextInt(CHARACTERS.length)])

		return keyBuilder.toString()
	}
}
