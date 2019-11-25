package dniel.forwardauth.infrastructure.siren

import java.io.Serializable
import java.net.URI
import java.util.Collections.emptyList
import java.util.LinkedHashMap
import dniel.forwardauth.infrastructure.siren.internal.util.asMap
import dniel.forwardauth.infrastructure.siren.internal.util.asNonNullStringList
import dniel.forwardauth.infrastructure.siren.internal.util.skipNulls

/**
 * Links represent navigational transitions in the Siren specification. In
 * JSON Siren, links are represented as an array inside the entity, such as
 * `{ "links": [{ "rel": [ "self" ], "href": "http://api.x.io/orders/42"}] }`.
 *
 * **See also:** [Link specification](https://github.com/kevinswiber/siren.links-1)
 *
 * Prefer using [newBuilder] to create a new instance instead of using the
 * constructor, as the constructor has some relaxed checking which should
 * only be used when deserializing an existing representation.
 */
data class Link(
        /**
     * Describes aspects of the link based on the current representation.
     * Possible values are implementation-dependent and should be documented.
     *
     * An empty list will be excluded during serialization to JSON as this
     * element is optional.
     *
     * @return the value of class attribute
     */
    val clazz: List<String> = emptyList(),
    /**
     * Text describing the nature of a link.
     *
     * @return the value of title attribute
     */
    val title: String? = null,
        /**
     * Defines the relationship of the link to its entity, per Web
     * Linking (RFC5988). Required.
     *
     * @return the value of rel attribute
     */
    val rel: List<String>,
        /**
     * The URI of the linked resource. Required.
     *
     * @return the value of href attribute
     */
    val href: URI,
        /**
     * Defines media type of the linked resource, per Web Linking (RFC5988).
     * For the syntax, see RFC2045 (section 5.1), RFC4288 (section 4.2),
     * RFC6838 (section 4.2)
     *
     * @return the value of type attribute
     */
    val type: String? = null
) : Serializable {

    /**
     * Create a new builder using the current data.
     */
    fun toBuilder(): Builder = newBuilder(rel, href)
        .title(title)
        .clazz(clazz)
        .type(type)

    internal fun toRaw(): Map<String, Any> =
        LinkedHashMap<String, Any?>().apply {
            this[Siren.CLASS] = if (clazz.isEmpty()) null else clazz
            this[Siren.TITLE] = title
            this[Siren.REL] = if (rel.isEmpty()) null else rel
            this[Siren.HREF] = href
            this[Siren.TYPE] = type
        }.skipNulls()

    /**
     * Builder for [Link].
     */
    class Builder internal constructor(private var rel: List<String>, private var href: URI) {
        private var clazz: List<String> = emptyList()
        private var title: String? = null
        private var type: String? = null

        /**
         * Add value for title.
         *
         * @param title Text describing the nature of a link.
         * @return builder
         */
        fun title(title: String?) = apply { this.title = title }

        /**
         * Set value for rel.
         *
         * @param rel Defines the relationship of the link to its entity, per
         * Web Linking (RFC5988). Required.
         * @return builder
         */
        fun rel(rel: List<String>) = apply { this.rel = rel }

        /**
         * Set value for rel.
         *
         * @param rel Defines the relationship of the link to its entity, per
         * Web Linking (RFC5988). Required.
         * @return builder
         */
        fun rel(rel: String) = rel(listOf<String>(rel))

        /**
         * Set value for href.
         *
         * @param href The URI of the linked resource. Required.
         */
        fun href(href: URI) = apply { this.href = href }

        /**
         * Add value for class.
         *
         * @param clazz Describes aspects of the link based on the current
         * representation. Possible values are implementation-dependent and
         * should be documented.
         * @return builder
         */
        fun clazz(clazz: List<String>?) = apply { this.clazz = clazz ?: emptyList() }

        /**
         * Add value for class.
         *
         * @param clazz Describes aspects of the link based on the current
         * representation. Possible values are implementation-dependent and
         * should be documented.
         * @return builder
         */
        fun clazz(vararg clazz: String) = clazz(listOf(*clazz))

        /**
         * Add value for type.
         *
         * @param type Defines media type of the linked resource, per Web
         * Linking (RFC5988). For the syntax, see RFC2045 (section 5.1),
         * RFC4288 (section 4.2), RFC6838 (section 4.2)
         * @return builder
         */
        fun type(type: String?) = apply { this.type = type }

        /**
         * Build the [Link].
         */
        // TODO: Ensure immutability
        fun build() = Link(
                clazz = clazz,
                title = title,
                rel = rel,
                href = href,
                type = type
        )
    }

    /** @suppress */
    companion object {
        private const val serialVersionUID = -5250035724727313356L

        internal fun fromRaw(map: Any?): Link = fromRaw(map!!.asMap())

        private fun fromRaw(map: Map<String, Any?>): Link = Link(
                clazz = map[Siren.CLASS]?.asNonNullStringList() ?: emptyList(),
                title = map[Siren.TITLE] as String?,
                rel = map.getValue(Siren.REL)!!.asNonNullStringList(),
                href = URI.create(map[Siren.HREF].toString()),
                type = map[Siren.TYPE] as String?
        )

        /**
         * Create a new builder using the required attributes.
         *
         * @param rel Defines the relationship of the link to its entity,
         * per Web Linking (RFC5988).
         * @param href The URI of the linked resource.
         * @return a new builder
         */
        @JvmStatic
        fun newBuilder(rel: List<String>, href: URI): Builder = Builder(rel, href)

        /**
         * Create a new builder using the required attributes.
         *
         * @param rel Defines the relationship of the link to its entity, per
         * Web Linking (RFC5988).
         * @param href The URI of the linked resource.
         * @return a new builder
         */
        @JvmStatic
        fun newBuilder(rel: String, href: URI): Builder = Builder(listOf(rel), href)
    }
}
