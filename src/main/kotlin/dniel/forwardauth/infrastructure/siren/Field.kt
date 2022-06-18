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
import dniel.forwardauth.infrastructure.siren.Field.Companion.newBuilder
import dniel.forwardauth.infrastructure.siren.internal.util.asMap
import dniel.forwardauth.infrastructure.siren.internal.util.asNonNullStringList
import dniel.forwardauth.infrastructure.siren.internal.util.skipNulls
import io.micronaut.serde.annotation.Serdeable
import java.io.Serializable
import java.util.Collections.emptyList

/**
 * Fields represent controls inside of [actions][Action].
 *
 * **See also:** [Field specification](https://github.com/kevinswiber/siren.fields-1)
 *
 * Prefer using [newBuilder] to create a new instance instead of using the
 * constructor, as the constructor has some relaxed checking which should
 * only be used when deserializing an existing representation.
 */
@Serdeable
data class Field(
    /**
     * A name describing the control. Field names MUST be unique within the set
     * of fields for an action. The behaviour of clients when parsing a Siren
     * document that violates this constraint is undefined. Required.
     *
     * @return the value of name attribute
     */
    val name: String,
    /**
     * Describes aspects of the field based on the current representation.
     * Possible values are implementation-dependent and should be documented.
     *
     * @return the value of class attribute or an empty list if it is missing
     */
    @JsonProperty("class")
    val clazz: List<String> = emptyList(),
    /**
     * The input type of the field. This is a subset of the input types
     * specified by HTML5.
     *
     * @return the value of type attribute
     */
    val type: String? = null,
    /**
     * Textual annotation of a field. Clients may use this as a label.
     *
     * @return the value of title attribute
     */
    val title: String? = null,
    /**
     * A value assigned to the field. May be a scalar value or a list of value
     * objects.
     *
     * See specification for special values.
     *
     * @return the value of value attribute
     */
    val value: Any? = null
) : Serializable {

    /**
     * Create a new builder using the current data.
     */
    fun toBuilder() = newBuilder(name)
        .clazz(clazz)
        .type(type)
        .title(title)
        .value(value)

    internal fun toRaw(): Map<String, Any> =
        LinkedHashMap<String, Any?>().apply {
            this[Siren.NAME] = name
            this[Siren.CLASS] = if (clazz.isEmpty()) null else clazz
            this[Siren.TYPE] = type
            this[Siren.TITLE] = title
            this[Siren.VALUE] = value
        }.skipNulls()

    /**
     * Builder for [Field].
     */
    class Builder internal constructor(private var name: String) {
        private var clazz: List<String> = emptyList()
        private var type: String? = null
        private var title: String? = null
        private var value: Any? = null

        /**
         * Set value for name.
         *
         * @param name A name describing the control. Field names MUST be
         * unique within the set of fields for an action. Required.
         * @return builder
         */
        fun name(name: String) = apply { this.name = name }

        /**
         * Set value for class.
         *
         * @param clazz Describes aspects of the field based on the current
         * representation. Possible values areimplementation-dependent and
         * should be documented.
         * @return builder
         */
        fun clazz(clazz: List<String>?) = apply { this.clazz = clazz ?: emptyList() }

        /**
         * Set value for class.
         *
         * @param clazz Describes aspects of the field based on the current
         * representation. Possible values areimplementation-dependent and
         * should be documented.
         * @return builder
         */
        fun clazz(vararg clazz: String): Builder = clazz(listOf(*clazz))

        /**
         * Set value for type.
         *
         * @param type The input type of the field. This is a subset of the
         * input types specified by HTML5.
         * @return builder
         */
        fun type(type: String?) = apply { this.type = type }

        /**
         * Set value for type.
         *
         * @param type The input type of the field. This is a subset of the
         * input types specified by HTML5.
         * @return builder
         */
        fun type(type: Type?) = apply { this.type = type?.value }

        /**
         * Set value for title.
         *
         * @param title Textual annotation of a field. Clients may use this as
         * a label.
         * @return builder
         */
        fun title(title: String?) = apply { this.title = title }

        /**
         * Set value for value.
         * @param value A value assigned to the field. May be a scalar value
         * or a list of value objects.
         * @return builder
         */
        fun value(value: Any?) = apply { this.value = value }

        /**
         * Build the [Field].
         */
        // TODO: Ensure immutability
        fun build() = Field(
            name = name,
            clazz = clazz,
            type = type,
            title = title,
            value = value
        )
    }

    enum class Type constructor(
        /**
         * The textual value as defined in Siren specification.
         */
        val value: String
    ) {
        HIDDEN("hidden"),
        TEXT("text"),
        SEARCH("search"),
        TEL("tel"),
        URL("url"),
        EMAIL("email"),
        PASSWORD("password"),
        DATETIME("datetime"),
        DATE("date"),
        MONTH("month"),
        WEEK("week"),
        TIME("time"),
        DATETIME_LOCAL("datetime-local"),
        NUMBER("number"),
        RANGE("range"),
        COLOR("color"),
        CHECKBOX("checkbox"),
        RADIO("radio"),
        FILE("file")
    }

    /** @suppress */
    companion object {
        private const val serialVersionUID = -4600180928453411445L

        internal fun fromRaw(map: Any?): Field = fromRaw(map!!.asMap())

        private fun fromRaw(map: Map<String, Any?>): Field = Field(
            name = map[Siren.NAME] as String,
            clazz = map[Siren.CLASS]?.asNonNullStringList() ?: emptyList(),
            type = map[Siren.TYPE] as String?,
            title = map[Siren.TITLE] as String?,
            value = map[Siren.VALUE]
        )

        /**
         * Create a new builder using the required attributes.
         *
         * @param name A name describing the control. Field names MUST be
         * unique within the set of fields for an action.
         * @return a new builder
         */
        @JvmStatic
        fun newBuilder(name: String): Builder = Builder(name)
    }
}