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

package eu.cdevreeze.tqa2.locfreetaxonomy.dom

import java.net.URI

import scala.util.Try

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.common.locfreexlink
import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys
import eu.cdevreeze.yaidom2.core.EName

/**
 * Taxonomy element key dialect in a locator-free taxonomy.
 *
 * @author Chris de Vreeze
 */
object TaxonomyElemKeyDialect {

  /**
   * Taxonomy element key in a locator-free taxonomy.
   *
   * It is assumed that the keys obey their schema(s), or else the query methods below may throw an exception.
   */
  trait TaxonomyElemKey extends locfreexlink.XLinkResource {

    type KeyType

    type TaxoElemKeyType <: TaxonomyElemKeys.TaxonomyElemKey

    /**
     * Returns the key. It is equal to `taxoElemKey.key`.
     */
    def key: KeyType

    def taxoElemKey: TaxoElemKeyType
  }

  // General categories of taxonomy element keys

  trait SchemaComponentKey extends TaxonomyElemKey {

    type KeyType = EName

    final def key: KeyType = {
      attrAsResolvedQNameOption(ENames.KeyEName).getOrElse(sys.error(s"Missing key attribute. Document: $docUri. Element: $name"))
    }
  }

  trait AppinfoContentKey extends TaxonomyElemKey

  // Specific taxonomy element keys

  /**
   * ConceptKey element, holding the target EName (as QName, resolved by the containing element's scope) of the concept in its key attribute.
   */
  trait ConceptKey extends SchemaComponentKey {

    type TaxoElemKeyType = TaxonomyElemKeys.ConceptKey

    final def taxoElemKey: TaxoElemKeyType = TaxonomyElemKeys.ConceptKey(key)
  }

  /**
   * ElementKey element, holding the target EName (as QName, resolved by the containing element's scope) of the element in its key attribute.
   */
  trait ElementKey extends SchemaComponentKey {

    type TaxoElemKeyType = TaxonomyElemKeys.ElementKey

    final def taxoElemKey: TaxoElemKeyType = TaxonomyElemKeys.ElementKey(key)
  }

  /**
   * TypeKey element, holding the target EName (as QName, resolved by the containing element's scope) of the type in its key attribute.
   */
  trait TypeKey extends SchemaComponentKey {

    type TaxoElemKeyType = TaxonomyElemKeys.TypeKey

    final def taxoElemKey: TaxoElemKeyType = TaxonomyElemKeys.TypeKey(key)
  }

  /**
   * RoleKey element, holding the role URI of the role type in its key attribute.
   */
  trait RoleKey extends AppinfoContentKey {

    type KeyType = String

    type TaxoElemKeyType = TaxonomyElemKeys.RoleKey

    final def key: KeyType = {
      attrOption(ENames.KeyEName).getOrElse(sys.error(s"Missing key attribute. Document: $docUri. Element: $name"))
    }

    final def taxoElemKey: TaxoElemKeyType = TaxonomyElemKeys.RoleKey(key)
  }

  /**
   * ArcroleKey element, holding the arcrole URI of the arcrole type in its key attribute.
   */
  trait ArcroleKey extends AppinfoContentKey {

    type KeyType = String

    type TaxoElemKeyType = TaxonomyElemKeys.ArcroleKey

    final def key: KeyType = {
      attrOption(ENames.KeyEName).getOrElse(sys.error(s"Missing key attribute. Document: $docUri. Element: $name"))
    }

    final def taxoElemKey: TaxoElemKeyType = TaxonomyElemKeys.ArcroleKey(key)
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
  trait AnyElementKey extends TaxonomyElemKey {

    type KeyType = URI

    type TaxoElemKeyType = TaxonomyElemKeys.AnyElementKey

    final def key: KeyType = {
      attrOption(ENames.KeyEName).flatMap(u => Try(URI.create(u)).toOption)
        .getOrElse(sys.error(s"Missing key attribute. Document: $docUri. Element: $name"))
    }

    final def taxoElemKey: TaxoElemKeyType = TaxonomyElemKeys.AnyElementKey(key)

    /**
     * Optional element EName (as QName, resolved by the containing element's scope) of the element pointed to by the key.
     */
    final def elementNameOption: Option[EName] = {
      attrAsResolvedQNameOption(ENames.ElementNameEName)
    }
  }
}
