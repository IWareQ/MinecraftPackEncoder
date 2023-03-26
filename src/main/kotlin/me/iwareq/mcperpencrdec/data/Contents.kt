package me.iwareq.mcperpencrdec.data

import com.fasterxml.jackson.annotation.JsonProperty

class Contents {

	@JsonProperty("content")
	val entries = mutableListOf<ContentEntry>()

	fun addEntry(path: String, key: String?) {
		if ("contents.json" != path) this.entries.add(ContentEntry(path.replace("\\", "/"), key))
	}

	class ContentEntry(
		@JsonProperty("path") val path: String,
		@JsonProperty("key") val key: String?
	)
}
