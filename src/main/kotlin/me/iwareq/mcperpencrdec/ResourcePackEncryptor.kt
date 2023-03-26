package me.iwareq.mcperpencrdec

import com.fasterxml.jackson.databind.ObjectMapper
import me.iwareq.mcperpencrdec.data.Contents
import me.iwareq.mcperpencrdec.util.Aes256Cfb8.encrypt
import me.iwareq.mcperpencrdec.util.FileUtils.copyOrWrite
import me.iwareq.mcperpencrdec.util.FileUtils.getResourcePacks
import me.iwareq.mcperpencrdec.util.FileUtils.write
import me.iwareq.mcperpencrdec.util.KeyGenerator.generateKey
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path

object ResourcePackEncryptor {

	private val JSON_MAPPER = ObjectMapper()

	private val INPUT_PATH = Path.of("encrypt" + File.separator + "input")
	private val OUTPUT_PATH = Path.of("encrypt" + File.separator + "output")

	// manifest, pack_icon, bug_pack_icon and contents required!
	private val EXCLUDE_FILES = listOf(
		"manifest.json", "pack_icon.png", "bug_pack_icon.png"
	)

	@Throws(IOException::class)
	@JvmStatic
	fun main(args: Array<String>) {
		val resourcePacks = getResourcePacks(INPUT_PATH)
		for (resourcePack in resourcePacks) {
			val jsonNode = JSON_MAPPER.readTree(resourcePack.resolve("manifest.json").toFile())
			val uuid = jsonNode["header"]["uuid"].asText()
			val packName = resourcePack.getName(2)
			Files.walk(resourcePack).use { paths ->
				val contents = Contents()
				val filteredPaths = paths.filter { path: Path? -> Files.isRegularFile(path!!) }.toList()
				for (path in filteredPaths) {
					val relativePath = INPUT_PATH.relativize(path).toString()
					val outputEntryPath = OUTPUT_PATH.resolve(relativePath)
					Files.createDirectories(outputEntryPath.parent)
					var key = generateKey()
					if (EXCLUDE_FILES.contains(path.toFile().name)) {
						if (!copyOrWrite(JSON_MAPPER, path, outputEntryPath)) {
							break
						}

						key = ""
					} else {
						if (!write(JSON_MAPPER, path, { buffer: ByteArray ->
								try {
									return@write encrypt(key.toByteArray(), buffer)
								} catch (exception: Exception) {
									throw RuntimeException(exception)
								}
							}, outputEntryPath)) {
							break
						}
					}

					var pathString = outputEntryPath.toString()
					pathString = pathString.replace(OUTPUT_PATH.resolve(packName).toString() + File.separator, "")

					contents.addEntry(pathString, key)
				}

				val contentsFile = OUTPUT_PATH.resolve(packName).resolve("contents.json").toFile()
				contentsFile.createNewFile()
				try {
					RandomAccessFile(contentsFile, "rw").use {
						it.write(byteArrayOf(0x00, 0x00, 0x00, 0x00)) // Version
						it.write(byteArrayOf(0xFC.toByte(), 0xB9.toByte(), 0xCF.toByte(), 0x9B.toByte())) // Magic
						it.seek(0x10)

						val idBytes = uuid.toByteArray()
						it.write(byteArrayOf(idBytes.size.toByte()))
						it.write(idBytes)
						it.seek(0x100)

						val keyForContents = generateKey()
						val contentBytes = JSON_MAPPER.writeValueAsBytes(contents)
						it.write(encrypt(keyForContents.toByteArray(), contentBytes))
						Files.writeString(OUTPUT_PATH.resolve(packName).resolve("password.txt"), keyForContents)
					}
				} catch (exception: Exception) {
					exception.printStackTrace()
				}
			}

			println("Encryption finished for: $packName")
		}
	}
}
