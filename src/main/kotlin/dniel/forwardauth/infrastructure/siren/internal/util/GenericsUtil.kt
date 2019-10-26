package dniel.forwardauth.infrastructure.siren.internal.util

internal fun Any?.asList(): List<Any?> {
    require(this is List<*>) { "Casting to List failed. Found type ${this?.javaClass}" }
    return this
}

internal fun Any?.asNonNullStringList(): List<String> {
    require(this is List<*>) { "Casting to List failed. Found type ${this?.javaClass}" }
    this.forEach { item ->
        require(item is String) { "Casting to List of Strings. Found item ${item?.javaClass}" }
    }

    @Suppress("UNCHECKED_CAST")
    return this as List<String>
}

internal fun Any?.asMap(): Map<String, Any?> {
    require(this is Map<*, *>) { "Casting to Map failed. Found type ${this?.javaClass}" }

    @Suppress("UNCHECKED_CAST")
    return this as Map<String, Any>
}
