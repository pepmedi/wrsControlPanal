package util

import kotlinx.serialization.json.Json


object SerializationUtils {
    val json = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}

inline fun <reified T> serialize(data: T, json: Json = SerializationUtils.json): String {
    return json.encodeToString(data)
}

inline fun <reified T> deserialize(jsonString: String, json: Json = SerializationUtils.json): T {
    return json.decodeFromString(jsonString)
}

inline fun <reified T> serializeList(data: List<T>, json: Json = SerializationUtils.json): String {
    return json.encodeToString(data)
}

inline fun <reified T> deserializeList(
    jsonString: String,
    json: Json = SerializationUtils.json
): List<T> {
    return json.decodeFromString(jsonString)
}