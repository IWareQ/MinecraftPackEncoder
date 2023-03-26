package me.iwareq.mcperpencrdec.util

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

object FileUtils {

	@JvmStatic
	fun getResourcePacks(input: Path): List<Path> {
		try {
			Files.createDirectories(input)
		} catch (ignored: IOException) {
			/**/
		}

		var resourcePacks: List<Path> = ArrayList()
		val files = input.toFile().listFiles()
		if (files != null) {
			resourcePacks = Arrays.stream(files).map { obj: File -> obj.toPath() }.collect(Collectors.toList())
		}

		return resourcePacks
	}

	@JvmStatic
	fun copyOrWrite(jsonMapper: ObjectMapper, input: Path, output: Path) = try {
		if (input.endsWith(".json")) {
			val node = jsonMapper.readTree(input.toFile())
			val prettyJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)
			Files.write(output, prettyJson.toByteArray())
		} else {
			Files.copy(input, output)
		}

		true
	} catch (exception: Exception) {
		if (exception !is FileAlreadyExistsException) {
			exception.printStackTrace()
		}

		false
	}

	@JvmStatic
	fun write(jsonMapper: ObjectMapper, input: Path, consumer: Function<ByteArray, ByteArray>, output: Path) = try {
		FileInputStream(input.toFile()).use { inputStream ->
			var buffer = ByteArray(inputStream.available())
			inputStream.read(buffer)

			buffer = consumer.apply(buffer)
			if (input.endsWith(".json")) {
				try {
					val node = jsonMapper.readTree(buffer)
					val prettyJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)
					Files.write(output, prettyJson.toByteArray())
				} catch (exception: IOException) {
					Files.write(output, buffer)
				}
			} else {
				Files.write(output, buffer)
			}

			true
		}
	} catch (exception: Exception) {
		exception.printStackTrace()
		false
	}
}
