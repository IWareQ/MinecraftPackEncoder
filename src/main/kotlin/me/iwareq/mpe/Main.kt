package me.iwareq.mpe

import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
	if (args.size < 2) {
		printHelp()

		exitProcess(1)
	}

	val inputPath = args[1]
	when (args[0]) {
		"decrypt" -> {
			ResourcePackDecryptor.execute(Paths.get(inputPath))
		}

		"encrypt" -> {
			// manifest, pack_icon, bug_pack_icon and contents required!
			val excludedFiles = mutableSetOf(
				"manifest.json", "pack_icon.png", "bug_pack_icon.png"
			)

			if (args.size == 3) {
				val excludeFiles = args[2].split(',')
				excludeFiles.forEach { excludedFiles.add(it.trim()) }
			}

			ResourcePackEncryptor.execute(Paths.get(inputPath), excludedFiles)
		}

		else -> {
			printHelp()
		}
	}
}

fun printHelp() {
	println("Unknown parameters! Use:")
	println("For Encrypting: java -jar MinecraftPackEncoder.jar encrypt \"path/to/resource\"")
	println("For Decrypting: java -jar MinecraftPackEncoder.jar decrypt \"path/to/resource\"")
}
