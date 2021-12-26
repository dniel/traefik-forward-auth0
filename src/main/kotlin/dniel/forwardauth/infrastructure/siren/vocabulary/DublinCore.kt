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

package dniel.forwardauth.infrastructure.siren.vocabulary

/**
 * Class provides constants related to the Dublin Core Schema.
 *
 * **See also:** [Dublin Core](https://en.wikipedia.org/wiki/Dublin_Core)
 */
object DublinCore {

    /**
     * Lowercase version of "title" for string search simplicity
     */
    const val ALTERNATIVE = "alternative"

    /**
     * Denotes first available datetime observation found in the corresponding
     * data set of a part
     */
    const val AVAILABLE = "available"

    /**
     * Identifies the user who last modified an entity
     */
    const val CONTRIBUTOR = "contributor"

    /**
     * Denotes when an entity was created
     */
    const val CREATED = "created"

    /**
     * Identifies the user who created an entity
     */
    const val CREATOR = "creator"

    /**
     * Description of an entity
     */
    const val DESCRIPTION = "description"

    const val EXTENT = "extent"

    /**
     * Denotes the current version of an entity or part
     */
    const val HAS_VERSION = "hasVersion"

    /**
     * Unique identifier of an entity, part or version. Required when updating
     * an entity
     */
    const val IDENTIFIER = "identifier"

    /**
     * Denotes the parent entity of a part
     */
    const val IS_PART_OF = "isPartOf"

    const val IS_REFERENCED_BY = "isReferencedBy"

    /**
     * Denotes the parent entity or part of a version
     */
    const val IS_VERSION_OF = "isVersionOf"

    /**
     * Denotes when an entity was issued (locked)
     */
    const val ISSUED = "issued"

    const val MEDIATOR = "mediator"

    /**
     * Denotes when an entity was last modified
     */
    const val MODIFIED = "modified"

    /**
     * Identifies the change request command applied to an entity, part and/or
     * version
     */
    const val PROVENANCE = "provenance"

    /**
     * Identifies the user who issued (locked) an entity
     */
    const val PUBLISHER = "publisher"

    /**
     * Uniquely identifies an entity or version by external relation (third
     * party identifier). Can not be changed on an existing entity
     */
    const val REFERENCES = "references"

    const val RELATION = "relation"

    /**
     * Identifies the previous version of a version
     */
    const val REPLACES = "replaces"

    /**
     * Identifies the source (namespace) of an entity
     */
    const val SOURCE = "source"

    /**
     * Unique identifier (name) of the attribute of an entity. Optional subject
     * of an entity
     */
    const val SUBJECT = "subject"

    /**
     * Identifies the searchable name of an entity. Required when creating an
     * entity
     */
    const val TITLE = "title"

    /**
     * Identifies the type of an entity
     */
    const val TYPE = "type"

    /**
     * Denotes when an entity was no longer valid (deleted).
     * Also denotes the last available datetime observation found in the
     * corresponding data set of a part
     */
    const val VALID = "valid"
}