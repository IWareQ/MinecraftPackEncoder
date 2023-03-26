package me.iwareq.mpe

import com.fasterxml.jackson.databind.ObjectMapper
import me.iwareq.mpe.data.Contents
import me.iwareq.mpe.util.Aes256Cfb8.decrypt
import me.iwareq.mpe.util.FileUtils.write
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

object ResourcePackDecryptor {

	private val JSON_MAPPER = ObjectMapper()

	@Throws(Exception::class)
	@JvmStatic
	fun execute(inputPath: Path) {
		val packName = inputPath.fileName
		val passwordFile = inputPath.resolve("password.txt").toFile()
		if (!passwordFile.exists() || passwordFile.length().toInt() == 0) {
			println("Key for \"$packName\" not found")

			passwordFile.createNewFile()
			println("password.txt created! Insert key in this file!")
			return
		}

		val keyForContent = Files.readAllLines(passwordFile.toPath())[0]
		var contents: Contents
		try {
			FileInputStream(inputPath.resolve("contents.json").toFile()).use {
				var buffer = ByteArray(it.available())
				it.skip(0x100)
				it.read(buffer)

				val keyForContentBytes = keyForContent.toByteArray()
				buffer = decrypt(keyForContentBytes, buffer)
				contents = JSON_MAPPER.readValue(buffer, Contents::class.java)
			}
		} catch (exception: Exception) {
			exception.printStackTrace()
			exitProcess(1)
		}

		println("Copy all files")

		val outputPath = inputPath.resolve("decrypt")
		Files.walk(inputPath).filter(Files::isRegularFile).forEach {
			val targetFile = outputPath.resolve(inputPath.relativize(it))
			try {
				Files.createDirectories(targetFile.parent)
				Files.copy(it, targetFile, StandardCopyOption.REPLACE_EXISTING)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		for (entry in contents.entries) {
			val inputEntryPath = inputPath.resolve(entry.path)
			if (!Files.isRegularFile(inputEntryPath)) {
				continue
			}

			val outputEntryPath = outputPath.resolve(entry.path)
			Files.createDirectories(outputEntryPath.parent)
			val key = entry.key
			if (!key.isNullOrEmpty()) {
				if (!write(JSON_MAPPER, inputEntryPath, { buffer: ByteArray ->
						try {
							return@write decrypt(key.toByteArray(), buffer)
						} catch (exception: Exception) {
							throw RuntimeException(exception)
						}
					}, outputEntryPath)) {
					break
				}
			}
		}

		val contentsFile = outputPath.resolve("contents.json").toFile()
		contentsFile.createNewFile()
		JSON_MAPPER.writeValue(contentsFile, contents)

		println("Decryption finished for: $packName")
	}
}
