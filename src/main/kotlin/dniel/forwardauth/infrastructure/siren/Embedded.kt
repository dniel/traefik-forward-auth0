package dniel.forwardauth.infrastructure.siren

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.net.URI
import java.util.Collections.emptyList
import dniel.forwardauth.infrastructure.siren.internal.util.asList
import dniel.forwardauth.infrastructure.siren.internal.util.asMap
import dniel.forwardauth.infrastructure.siren.internal.util.asNonNullStringList

/**
 * Represents a sub-entity in the Siren specification. Sub-entities can be
 * expressed as either an [EmbeddedLink] or an [EmbeddedRepresentation].
 * In JSON Siren, sub-entities are represented by an entities array, such
 * as `{ "entities": [{ ... }] }`.
 *
 * **See also:** [Sub-entity specification](https://github.com/kevinswiber/siren.sub-entities)
 */
abstract class Embedded : Serializable {
    @get:JsonProperty("class")
    abstract val clazz: List<String>

    abstract val rel: List<String>

    internal abstract fun toRaw(): Map<String, Any>

    /** @suppress */
    companion object {
        private const val serialVersionUID = 8856776314875482332L

        internal fun fromRaw(map: Any?): Embedded = fromRaw(map!!.asMap())

        private fun fromRaw(map: Map<String, Any?>): Embedded {
            val clazz = map[Siren.CLASS]?.asNonNullStringList() ?: emptyList()
            val rel = map.getValue(Siren.REL)!!.asNonNullStringList()

            return if (map[Siren.HREF] != null) {
                EmbeddedLink(
                        clazz = clazz,
                        rel = rel,
                        href = URI.create(map[Siren.HREF].toString()),
                        type = map[Siren.TYPE] as String?,
                        title = map[Siren.TITLE] as String?
                )
            } else {
                EmbeddedRepresentation(
                        clazz = clazz,
                        title = map[Siren.TITLE] as String?,
                        rel = rel,
                        properties = map[Siren.PROPERTIES]?.asMap()
                                ?: emptyMap(),
                        links = map[Siren.LINKS]?.asList()?.map { Link.fromRaw(it) }
                                ?: emptyList(),
                        entities = map[Siren.ENTITIES]?.asList()?.map { fromRaw(it) }
                                ?: emptyList(),
                        actions = map[Siren.ACTIONS]?.asList()?.map { Action.fromRaw(it) }
                                ?: emptyList()
                )
            }
        }
    }
}
