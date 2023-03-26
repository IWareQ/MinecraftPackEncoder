package me.iwareq.mpe

import com.fasterxml.jackson.databind.ObjectMapper
import me.iwareq.mpe.data.Contents
import me.iwareq.mpe.util.Aes256Cfb8.encrypt
import me.iwareq.mpe.util.FileUtils.copyOrWrite
import me.iwareq.mpe.util.FileUtils.write
import me.iwareq.mpe.util.KeyGenerator.generateKey
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.name

object ResourcePackEncryptor {

	private val JSON_MAPPER = ObjectMapper()

	@Throws(IOException::class)
	@JvmStatic
	fun execute(inputPath: Path, excludeFiles: Set<String>) {
		val jsonNode = JSON_MAPPER.readTree(inputPath.resolve("manifest.json").toFile())
		val uuid = jsonNode["header"]["uuid"].asText()
		val packName = inputPath.name

		val outputPath = inputPath.resolve("encrypt")
		Files.walk(inputPath).use { paths ->
			val contents = Contents()
			val filteredPaths =
				paths.filter { path: Path? -> Files.isRegularFile(path!!) }.collect(Collectors.toList())
			for (path in filteredPaths) {
				val relativePath = inputPath.relativize(path).toString()
				val outputEntryPath = outputPath.resolve(relativePath)
				Files.createDirectories(outputEntryPath.parent)
				var key = generateKey()
				if (excludeFiles.contains(path.toFile().name)) {
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
				pathString = pathString.replace(outputPath.toString() + File.separator, "")
				println(pathString)

				contents.addEntry(pathString, key)
			}

			val contentsFile = outputPath.resolve("contents.json").toFile()
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
					FileWriter(outputPath.resolve("password.txt").toFile()).use { file ->
						file.write(keyForContents)
					}
				}
			} catch (exception: Exception) {
				exception.printStackTrace()
			}
		}

		println("Encryption finished for: $packName")
	}
}
