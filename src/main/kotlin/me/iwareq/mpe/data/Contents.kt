package me.iwareq.mpe.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
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
