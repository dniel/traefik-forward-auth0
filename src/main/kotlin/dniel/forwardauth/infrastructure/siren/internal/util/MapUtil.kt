package dniel.forwardauth.infrastructure.siren.internal.util

@Suppress("UNCHECKED_CAST")
internal fun <K, V> Map<K, V?>.skipNulls(): Map<K, V> =
    filterNot { it.value == null } as Map<K, V>
