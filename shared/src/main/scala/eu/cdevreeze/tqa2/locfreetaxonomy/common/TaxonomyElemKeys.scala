/*
 * Copyright 2019-2019 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.tqa2.locfreetaxonomy.common

import java.net.URI

import eu.cdevreeze.yaidom2.core.EName

/**
 * Taxonomy element keys in a locator-free taxonomy as case class instances. It is important that they are immutable
 * objects with well-defined efficient equality, which makes them good Map keys.
 *
 * It is also important that for each taxonomy element, its "canonical" key can easily be determined, without needing
 * any taxonomy as context. That's why there is no separate concept key, because that would require reasoning about
 * substitution groups, which may require analysis of multiple taxonomy documents.
 *
 * @author Chris de Vreeze
 */
object TaxonomyElemKeys {

  /**
   * Taxonomy element key in a locator-free taxonomy.
   *
   * Note that taxonomy elements may have more than one kind of taxonomy element key, although only 1 of them will
   * be considered the "canonical" one for the kind of element. For example, a concept declaration in the taxonomy
   * has an element key as canonical key, but it can also be referred to by an "any element key".
   */
  sealed trait TaxonomyElemKey {

    type KeyType

    def key: KeyType
  }

  // General categories of taxonomy element keys

  sealed trait SchemaComponentKey extends TaxonomyElemKey {

    type KeyType = EName
  }

  sealed trait AppinfoContentKey extends TaxonomyElemKey

  // Specific taxonomy element keys

  /**
   * ElementKey element, holding the target EName. The target is a global element declaration. In most cases, it is
   * a concept declaration.
   */
  final case class ElementKey(key: EName) extends SchemaComponentKey

  /**
   * TypeKey element, holding the target EName. The target is a named type definition.
   */
  final case class TypeKey(key: EName) extends SchemaComponentKey

  /**
   * RoleKey element, holding the role URI of the role type.
   */
  final case class RoleKey(key: String) extends AppinfoContentKey {

    type KeyType = String
  }

  /**
   * ArcroleKey element, holding the arcrole URI of the arcrole type.
   */
  final case class ArcroleKey(key: String) extends AppinfoContentKey {

    type KeyType = String
  }

  /**
   * Taxonomy element key (anyElemKey) for anything that is not a role, arcrole or schema component. Its key is an absolute URI with
   * fragment. The fragment is XPointer (as restricted by XBRL) pointing to some XML element in the document pointed to
   * by the URI (when ignoring the fragment).
   *
   * The fragment in the URI is typically an ID, which is stable, and is the same ID as in the corresponding regular taxonomy
   * file. It could also be an element scheme XPointer, however.
   *
   * TODO How stable is such a key with element scheme XPointer as URI fragment?
   */
  final case class AnyElementKey(key: URI) extends TaxonomyElemKey {

    type KeyType = URI
  }
}
