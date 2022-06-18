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
import dniel.forwardauth.infrastructure.siren.EmbeddedRepresentation.Companion.newBuilder
import dniel.forwardauth.infrastructure.siren.internal.util.skipNulls
import io.micronaut.serde.annotation.Serdeable
import java.io.Serializable
import java.util.Collections.emptyList
import java.util.Collections.emptyMap

/**
 * Embedded sub-entity representations retain all the characteristics of a
 * [standard entity][Root], but MUST also contain a rel attribute describing
 * the relationship of the sub-entity to its parent.
 *
 * **See also:** [Embedded Representation specification](https://github.com/kevinswiber/siren.embedded-representation)
 *
 * @see Embedded
 * @see EmbeddedLink
 *
 * Prefer using [newBuilder] to create a new instance instead of using the
 * constructor, as the constructor has some relaxed checking which should
 * only be used when deserializing an existing representation.
 */
@Serdeable
data class EmbeddedRepresentation(
    /**
     * Describes the nature of an entity's content based on the current
     * representation. Possible values are implementation-dependent and should
     * be documented.
     *
     * @return the value of class attribute or an empty list if it is missing
     */
    @JsonProperty("class")
    override val clazz: List<String> = emptyList(),
    /**
     * Descriptive text about the entity.
     *
     * @return the value of title attribute
     */
    val title: String? = null,
    /**
     * Defines the relationship of the sub-entity to its parent, per Web
     * Linking (RFC5899). Required.
     *
     * @return the value of rel attribute
     */
    override val rel: List<String>,
    /**
     * A set of key-value pairs that describe the state of an entity.
     *
     * @return the value of properties attribute or an empty map if it is
     * missing
     */
    val properties: Map<String, Any?> = mutableMapOf(),
    /**
     * A collection of items that describe navigational links, distinct from
     * entity relationships. Link items should contain a `rel` attribute to
     * describe the relationship and an `href` attribute to point to the target
     * URI. Entities should include a link `rel` to `self`.
     *
     * @return the value of links attribute or an empty list if it is missing
     */
    val links: List<Link> = emptyList(),
    /**
     * A collection of related sub-entities.
     *
     * @return the value of entities attribute or an empty list if it is missing
     */
    val entities: List<Embedded> = emptyList(),
    /**
     * A collection of actions; actions show available behaviors an entity
     * exposes.
     *
     * @return the value of actions attribute or an empty list if it is missing
     */
    val actions: List<Action> = emptyList()
) : Embedded(), Serializable {

    /**
     * Create a new builder using the current data.
     */
    fun toBuilder() = newBuilder(rel)
        .clazz(clazz)
        .title(title)
        .properties(properties)
        .links(links)
        .entities(entities)
        .actions(actions)

    override fun toRaw(): Map<String, Any> =
        LinkedHashMap<String, Any?>().apply {
            this[Siren.CLASS] = if (clazz.isEmpty()) null else clazz
            this[Siren.REL] = if (rel.isEmpty()) null else rel
            this[Siren.PROPERTIES] = if (properties.isEmpty()) null else properties
            this[Siren.LINKS] = if (links.isEmpty()) null else links.map(Link::toRaw)
            this[Siren.ENTITIES] = if (entities.isEmpty()) null else entities.map(Embedded::toRaw)
            this[Siren.ACTIONS] = if (actions.isEmpty()) null else actions.map(Action::toRaw)
            this[Siren.TITLE] = title
        }.skipNulls()

    /**
     * Builder for [EmbeddedRepresentation].
     */
    class Builder internal constructor(private var rel: List<String>) {
        private var clazz: List<String> = emptyList()
        private var title: String? = null
        private var properties: Map<String, Any?> = emptyMap()
        private var links: List<Link> = emptyList()
        private var entities: List<Embedded> = emptyList()
        private var actions: List<Action> = emptyList()

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
         * Set value for title.
         *
         * @param title Descriptive text about the entity.
         * @return builder
         */
        fun title(title: String?) = apply { this.title = title }

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
        fun rel(rel: String) = rel(listOf<String>(rel))

        /**
         * Set value for properties.
         *
         * @param properties A set of key-value pairs that describe the state
         * of an entity.
         * @return builder
         */
        fun properties(properties: Map<String, Any?>?) =
            apply { this.properties = properties ?: emptyMap() }

        fun property(key: String, value: Any) = apply {
            this.properties = this.properties.plus(Pair(key, value))
        }

        /**
         * Set value for links.
         *
         * @param links A collection of items that describe navigational links,
         * distinct from entity relationships. Entities should include a
         * link `rel` to `self`.
         * @return builder
         */
        fun links(links: List<Link>?) = apply { this.links = links ?: emptyList() }

        /**
         * Set value for links.
         *
         * @param links A collection of items that describe navigational links,
         * distinct from entity relationships. Entities should include a
         * link `rel` to `self`.
         * @return builder
         */
        fun links(vararg links: Link) = links(listOf(*links))

        /**
         * Set value for entities.
         *
         * @param entities A collection of related sub-entities.
         * @return builder
         */
        fun entities(entities: List<Embedded>?) =
            apply { this.entities = entities ?: emptyList() }

        /**
         * Set value for entities.
         *
         * @param entities A collection of related sub-entities.
         * @return builder
         */
        fun entities(vararg entities: Embedded) = entities(listOf(*entities))

        /**
         * Set value for actions.
         *
         * @param actions A collection of actions; actions show available
         * behaviors an entity exposes.
         * @return builder
         */
        fun actions(actions: List<Action>?) =
            apply { this.actions = actions ?: emptyList() }

        /**
         * Set value for actions.
         *
         * @param actions A collection of actions; actions show available
         * behaviors an entity exposes.
         * @return builder
         */
        fun actions(vararg actions: Action) = actions(listOf(*actions))

        /**
         * Build the [EmbeddedRepresentation].
         */
        // TODO: Ensure immutability
        fun build() = EmbeddedRepresentation(
            clazz = clazz,
            title = title,
            rel = rel,
            properties = properties,
            links = links,
            entities = entities,
            actions = actions
        )
    }

    /** @suppress */
    companion object {
        private const val serialVersionUID = 82962202068591847L

        /**
         * Create a new [Builder] using the required attributes.
         *
         * @param rel Defines the relationship of the sub-entity to its parent,
         * per Web Linking (RFC5899).
         */
        @JvmStatic
        fun newBuilder(rel: List<String>): Builder = Builder(rel)

        /**
         * Create a new [Builder] using the required attributes.
         *
         * @param rel Defines the relationship of the sub-entity to its parent,
         * per Web Linking (RFC5899).
         */
        @JvmStatic
        fun newBuilder(rel: String): Builder = Builder(listOf(rel))
    }
}