/*
 * Copyright (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dniel.forwardauth.infrastructure.siren

import com.fasterxml.jackson.annotation.JsonProperty
import dniel.forwardauth.infrastructure.siren.EmbeddedLink.Companion.newBuilder
import dniel.forwardauth.infrastructure.siren.internal.util.skipNulls
import io.micronaut.serde.annotation.Serdeable
import java.io.Serializable
import java.net.URI
import java.util.Collections

/**
 * Represents an embedded sub-entity that contains a URI link.
 *
 * **See also:** [Embedded Link specification](https://github.com/kevinswiber/siren.embedded-link)
 *
 * @see Embedded
 * @see EmbeddedRepresentation
 *
 * Prefer using [newBuilder] to create a new instance instead of using the
 * constructor, as the constructor has some relaxed checking which should
 * only be used when deserializing an existing representation.
 */
@Serdeable
data class EmbeddedLink(
    /**
     * Describes the nature of an entity's content based on the current
     * representation. Possible values are implementation-dependent and should
     * be documented.
     *
     * @return the value of class attribute or an empty list if it is missing
     */
    @JsonProperty("class")
    override val clazz: List<String> = Collections.emptyList(),
    /**
     * Defines the relationship of the sub-entity to its parent, per Web
     * Linking (RFC5899). Required.
     *
     * @return the value of rel attribute
     */
    override val rel: List<String>,
    /**
     * The URI of the linked sub-entity. Required.
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
    val type: String? = null,
    /**
     * Descriptive text about the entity.
     *
     * @return the value of title attribute
     */
    val title: String? = null
) : Embedded(), Serializable {

    /**
     * Create a new builder using the current data.
     */
    fun toBuilder() = newBuilder(rel, href)
        .clazz(clazz)
        .type(type)
        .title(title)

    override fun toRaw(): Map<String, Any> =
        LinkedHashMap<String, Any?>().apply {
            this[Siren.CLASS] = if (clazz.isEmpty()) null else clazz
            this[Siren.REL] = if (rel.isEmpty()) null else rel
            this[Siren.HREF] = href
            this[Siren.TYPE] = type
            this[Siren.TITLE] = title
        }.skipNulls()

    /**
     * Builder for [EmbeddedLink].
     */
    class Builder internal constructor(private var rel: List<String>, private var href: URI) {
        private var clazz: List<String> = emptyList()
        private var type: String? = null
        private var title: String? = null

        /**
         * Set value for class.
         *
         * @param clazz Describes the nature of an entity's content based on
         * the current representation. Possible values are
         * implementation-dependent and should be documented.
         * @return builder
         */
        fun clazz(clazz: List<String>?) = apply { this.clazz = clazz ?: emptyList() }

        /**
         * Set value for class.
         *
         * @param clazz Describes the nature of an entity's content based on
         * the current representation. Possible values are
         * implementation-dependent and should be documented.
         * @return builder
         */
        fun clazz(vararg clazz: String) = clazz(listOf(*clazz))

        /**
         * Set value for rel.
         *
         * @param rel Defines the relationship of the sub-entity to its parent,
         * per Web Linking (RFC5899). Required.
         * @return builder
         */
        fun rel(rel: List<String>) = apply { this.rel = rel }

        /**
         * Set value for rel.
         *
         * @param rel Defines the relationship of the sub-entity to its parent,
         * per Web Linking (RFC5899). Required.
         * @return builder
         */
        fun rel(rel: String) = rel(listOf(rel))

        /**
         * Set value for href.
         *
         * @param href The URI of the linked sub-entity. Required.
         */
        fun href(href: URI) = apply { this.href = href }

        /**
         * Set value for type.
         *
         * @param type Defines media type of the linked resource, per Web
         * Linking (RFC5988). For the syntax, see RFC2045 (section 5.1),
         * RFC4288 (section 4.2), RFC6838 (section 4.2)
         * @return builder
         */
        fun type(type: String?) = apply { this.type = type }

        /**
         * Set value for title.
         *
         * @param title Descriptive text about the entity.
         * @return builder
         */
        fun title(title: String?) = apply { this.title = title }

        /**
         * Build the [EmbeddedLink].
         */
        // TODO: Ensure immutability
        fun build() = EmbeddedLink(
            clazz = clazz,
            rel = rel,
            href = href,
            type = type,
            title = title
        )
    }

    /** @suppress */
    companion object {
        private const val serialVersionUID = 7663303509287365613L

        /**
         * Create a new builder using the required attributes.
         *
         * @param rel Defines the relationship of the sub-entity to its parent,
         * per Web Linking (RFC5899).
         * @param href The URI of the linked sub-entity.
         * @return a new builder
         */
        @JvmStatic
        fun newBuilder(rel: List<String>, href: URI): Builder = Builder(rel, href)

        /**
         * Create a new builder using the required attributes.
         *
         * @param rel Defines the relationship of the sub-entity to its parent,
         * per Web Linking (RFC5899).
         * @param href The URI of the linked sub-entity.
         * @return a new builder
         */
        @JvmStatic
        fun newBuilder(rel: String, href: URI): Builder = Builder(listOf(rel), href)
    }
}