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

import dniel.forwardauth.infrastructure.siren.Action.Companion.newBuilder
import dniel.forwardauth.infrastructure.siren.internal.util.asList
import dniel.forwardauth.infrastructure.siren.internal.util.asMap
import dniel.forwardauth.infrastructure.siren.internal.util.asNonNullStringList
import dniel.forwardauth.infrastructure.siren.internal.util.skipNulls
import java.io.Serializable
import java.net.URI
import java.util.Collections.emptyList

/**
 * Actions show available behaviors an entity exposes and are used for executing
 * state transitions. Represented in JSON Siren as an array such as
 * `{ "actions": [{ ... }] }`.
 *
 * **See also:** [Action specification](https://github.com/kevinswiber/siren.actions-1)
 *
 * Prefer using [newBuilder] to create a new instance instead of using the
 * constructor, as the constructor has some relaxed checking which should
 * only be used when deserializing an existing representation.
 */
data class Action(
    /**
     * A string that identifies the action to be performed. Action names MUST be
     * unique within the set of actions for an entity. The behaviour of clients
     * when parsing a Siren document that violates this constraint is undefined.
     * Required.
     *
     * @return the value of name attribute
     */
    val name: String,
    /**
     * Describes the nature of an action based on the current representation.
     * Possible values are implementation-dependent and should be documented.
     *
     * @return the value of class attribute or an empty list if it is missing
     */
    val clazz: List<String> = emptyList(),
    /**
     * An enumerated attribute mapping to a protocol method. For HTTP, these
     * values may be GET, PUT, POST, DELETE, or PATCH. As new methods are
     * introduced, this list can be extended. If this attribute is
     * omitted, GET should be assumed.
     *
     * @return the value of method attribute
     */
    val method: String? = null,
    /**
     * The URI of the action. Required.
     *
     * @return the value of href attribute
     */
    val href: URI,
    /**
     * Descriptive text about the action.
     *
     * @return the value of title attribute
     */
    val title: String? = null,
    /**
     * The encoding type for the request. When omitted and the fields attribute
     * exists, the default value is `application/x-www-form-urlencoded`.
     *
     * @return the value of type attribute
     */
    val type: String? = null,
    /**
     * A collection of fields.
     *
     * @return the value of fields attribute or an empty list if it is missing
     */
    val fields: List<Field> = emptyList()
) : Serializable {

    /**
     * Create a new builder using the current data.
     */
    fun toBuilder(): Builder = newBuilder(name, href)
        .clazz(clazz)
        .method(method)
        .title(title)
        .type(type)
        .fields(fields)

    internal fun toRaw(): Map<String, Any?> =
        LinkedHashMap<String, Any?>().apply {
            this[Siren.NAME] = name
            this[Siren.TITLE] = title
            this[Siren.CLASS] = if (clazz.isEmpty()) null else clazz
            this[Siren.METHOD] = method
            this[Siren.HREF] = href
            this[Siren.TYPE] = type
            this[Siren.FIELDS] = if (fields.isEmpty()) null else fields.map(Field::toRaw)
        }.skipNulls()

    /**
     * Builder for [Action].
     */
    class Builder internal constructor(private var name: String, private var href: URI) {
        private var clazz: List<String> = emptyList()
        private var method: String? = null
        private var title: String? = null
        private var type: String? = null
        private var fields: List<Field> = emptyList()

        /**
         * Set value for name.
         *
         * @param name A string that identifies the action to be performed.
         * Action names MUST be unique within the set of actions for an entity.
         * The behaviour of clients when parsing a Siren document that violates
         * this constraint is undefined. Required.
         * @return builder
         */
        fun name(name: String) = apply { this.name = name }

        /**
         * Set value for href.
         *
         * @param href The URI of the action. Required.
         */
        fun href(href: URI) = apply { this.href = href }

        /**
         * Set value for class.
         *
         * @param clazz Describes the nature of an action based on the current
         * representation. Possible values are implementation-dependent and
         * should be documented.
         * @return builder
         */
        fun clazz(clazz: List<String>?) = apply { this.clazz = clazz ?: emptyList() }

        /**
         * Set value for class.
         *
         * @param clazz Describes the nature of an action based on the current
         * representation. Possible values are implementation-dependent and
         * should be documented.
         * @return builder
         */
        fun clazz(vararg clazz: String) = clazz(listOf(*clazz))

        /**
         * Set value for method.
         *
         * @param method An enumerated attribute mapping to a protocol method.
         * For HTTP, these values may be GET, PUT, POST, DELETE, or PATCH. As
         * new methods are introduced, this list can be extended. If this
         * attribute is omitted, GET should be assumed.
         * @return builder
         */
        fun method(method: String?) = apply { this.method = method }

        /**
         * Set value for method.
         *
         * @param method An enumerated attribute mapping to a protocol method.
         * For HTTP, these values may be GET, PUT, POST, DELETE, or PATCH. As
         * new methods are introduced, this list can be extended. If this
         * attribute is omitted, GET should be assumed.
         * @return builder
         */
        fun method(method: Method?) = apply { this.method = method?.name }

        /**
         * Set value for title.
         *
         * @param title Descriptive text about the action.
         * @return builder
         */
        fun title(title: String?) = apply { this.title = title }

        /**
         * Set value for type.
         *
         * @param type The encoding type for the request. When omitted and the
         * fields attribute exists, the default value is
         * `application/x-www-form-urlencoded`.
         * @return builder
         */
        fun type(type: String?) = apply { this.type = type }

        /**
         * Set value for fields.
         *
         * @param fields A collection of fields.
         * @return builder
         */
        fun fields(fields: List<Field>?) = apply { this.fields = fields ?: emptyList() }

        /**
         * Set value for fields.
         *
         * @param fields A collection of fields.
         * @return builder
         */
        fun fields(vararg fields: Field) = fields(listOf(*fields))

        /**
         * Build the [Action].
         */
        // TODO: Ensure immutability
        fun build() = Action(
                name = name,
                clazz = clazz,
                method = method,
                href = href,
                title = title,
                type = type,
                fields = fields
        )
    }

    /**
     * An enumerated attribute mapping to a protocol method. For HTTP, these
     * values may be GET, PUT, POST, DELETE, or PATCH. As new methods are
     * introduced, this list can be extended.
     */
    enum class Method {
        HEAD,
        GET,
        PUT,
        POST,
        OPTIONS,
        DELETE
    }

    /** @suppress */
    companion object {
        private const val serialVersionUID = -8092791402843123679L

        internal fun fromRaw(map: Any?): Action = fromRaw(map!!.asMap())

        private fun fromRaw(map: Map<String, Any?>): Action = Action(
                name = map[Siren.NAME] as String,
                clazz = map[Siren.CLASS]?.asNonNullStringList() ?: emptyList(),
                method = map[Siren.METHOD] as String?,
                title = map[Siren.TITLE] as String?,
                href = URI.create(map[Siren.HREF].toString()),
                type = map[Siren.TYPE] as String?,
                fields = map[Siren.FIELDS]?.asList()?.map { Field.fromRaw(it) }
                        ?: emptyList()
        )

        /**
         * Create a new builder using the required attributes.
         *
         * @param name A string that identifies the action to be performed.
         * Action names MUST be unique within the set of actions for an entity.
         * The behaviour of clients when parsing a Siren document that violates
         * this constraint is undefined.
         * @param href The URI of the action.
         * @return a new builder
         */
        @JvmStatic
        fun newBuilder(name: String, href: URI): Builder = Builder(name, href)
    }
}
