package me.iwareq.mcperpencrdec

import com.fasterxml.jackson.databind.ObjectMapper
import me.iwareq.mcperpencrdec.data.Contents
import me.iwareq.mcperpencrdec.util.Aes256Cfb8.decrypt
import me.iwareq.mcperpencrdec.util.FileUtils.getResourcePacks
import me.iwareq.mcperpencrdec.util.FileUtils.write
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

object ResourcePackDecryptor {

	private val JSON_MAPPER = ObjectMapper()

	private val INPUT_PATH = Path.of("decrypt" + File.separator + "input")
	private val OUTPUT_PATH = Path.of("decrypt" + File.separator + "output")

	init {
		INPUT_PATH.toFile().mkdirs()
		OUTPUT_PATH.toFile().mkdirs()
	}

	@Throws(Exception::class)
	@JvmStatic
	fun main(args: Array<String>) {
		val resourcePacks = getResourcePacks(INPUT_PATH)
		for (resourcePack in resourcePacks) {
			val packName = resourcePack.fileName
			val passwordFile = resourcePack.resolve("password.txt")
			if (!passwordFile.toFile().exists()) {
				println("Key for \"$packName\" not found")

				passwordFile.toFile().createNewFile()
				println("password.txt created! Insert key in this file!")
				break
			}

			val keyForContent = Files.readString(passwordFile)
			var contents: Contents
			try {
				FileInputStream(resourcePack.resolve("contents.json").toFile()).use {
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

			val outputPath = OUTPUT_PATH.resolve(packName)
			Files.walk(resourcePack).filter(Files::isRegularFile).forEach {
				val targetFile = outputPath.resolve(resourcePack.relativize(it))
				try {
					Files.createDirectories(targetFile.parent)
					Files.copy(it, targetFile, StandardCopyOption.REPLACE_EXISTING)
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}

			for (entry in contents.entries) {
				val inputEntryPath = resourcePack.resolve(entry.path)
				if (!Files.isRegularFile(inputEntryPath)) {
					continue
				}

				val outputEntryPath = OUTPUT_PATH.resolve(packName).resolve(entry.path)
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

			val contentsFile = OUTPUT_PATH.resolve(packName).resolve("contents.json").toFile()
			contentsFile.createNewFile()
			JSON_MAPPER.writeValue(contentsFile, contents)

			println("Decryption finished for: $packName")
		}
	}
}
