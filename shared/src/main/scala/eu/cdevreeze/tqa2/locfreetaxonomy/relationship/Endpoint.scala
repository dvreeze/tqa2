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

package eu.cdevreeze.tqa2.locfreetaxonomy.relationship

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XLinkResource

/**
 * Endpoint of a relationship, so the source or the target of that relationship.
 *
 * For resolving prohibition/overriding and finding networks of relationships, a "taxonomy" is needed as context, and this
 * taxonomy should have fast indexes on the taxonomy element key of the endpoint.
 *
 * There are 3 kinds of endpoints: keys (other than those to XLink resources), non-key XLink resources, and keys to non-key
 * XLink resources. In terms of standard XBRL taxonomies, they represent XLink locators (other than those to XLink resources), XLink
 * resources, and locators to XLink resources, respectively.
 *
 * Type Endpoint and its sub-types strike a balance between keeping too much state on the one hand, and too little state to
 * be meaningful or useful on the other hand. For example, endpoints that are concept keys convey semantics without needing any context,
 * and endpoints that are regular resources for standard labels in a standard label link contain those standard labels. All
 * endpoints have in common that they contain at least a taxonomy element key (that either is the endpoint, or that is the
 * regular resource pointed to by the taxonomy element key).
 *
 * @author Chris de Vreeze
 */
sealed trait Endpoint {

  /**
   * The endpoint as key, either because the endpoint is a key, or because it is a resource which (like all taxonomy elements) has a key.
   */
  def taxonomyElemKey: TaxonomyElemKeys.TaxonomyElemKey

  /**
   * Returns None for key endpoints, and returns the resource (wrapped in an Option) for regular resources.
   */
  def targetResourceOption: Option[XLinkResource]
}

object Endpoint {

  /**
   * An endpoint that is a key, even if it is a key to a resource.
   */
  sealed trait Key extends Endpoint

  /**
   * Endpoint that is a key to an element that is not a resource, like a concept key.
   *
   * The taxonomy element key type hierarchy is mirrored in this key endpoint type hierarchy, for ease of use, even if
   * this means code duplication and less orthogonality.
   */
  sealed trait KeyEndpoint extends Key {

    type TaxoElemKeyType <: TaxonomyElemKeys.TaxonomyElemKey

    def taxonomyElemKey: TaxoElemKeyType

    final def targetResourceOption: Option[XLinkResource] = None
  }

  sealed trait SchemaComponentKeyEndpoint extends KeyEndpoint {

    type TaxoElemKeyType <: TaxonomyElemKeys.SchemaComponentKey
  }

  sealed trait AppinfoContentKeyEndpoint extends KeyEndpoint {

    type TaxoElemKeyType <: TaxonomyElemKeys.AppinfoContentKey
  }

  final case class ConceptKeyEndpoint(taxonomyElemKey: TaxonomyElemKeys.ConceptKey) extends SchemaComponentKeyEndpoint {

    type TaxoElemKeyType = TaxonomyElemKeys.ConceptKey
  }

  final case class ElementKeyEndpoint(taxonomyElemKey: TaxonomyElemKeys.ElementKey) extends SchemaComponentKeyEndpoint {

    type TaxoElemKeyType = TaxonomyElemKeys.ElementKey
  }

  final case class TypeKeyEndpoint(taxonomyElemKey: TaxonomyElemKeys.TypeKey) extends SchemaComponentKeyEndpoint {

    type TaxoElemKeyType = TaxonomyElemKeys.TypeKey
  }

  final case class RoleKeyEndpoint(taxonomyElemKey: TaxonomyElemKeys.RoleKey) extends AppinfoContentKeyEndpoint {

    type TaxoElemKeyType = TaxonomyElemKeys.RoleKey
  }

  final case class ArcroleKeyEndpoint(taxonomyElemKey: TaxonomyElemKeys.ArcroleKey) extends AppinfoContentKeyEndpoint {

    type TaxoElemKeyType = TaxonomyElemKeys.ArcroleKey
  }

  final case class AnyElementKeyEndpoint(taxonomyElemKey: TaxonomyElemKeys.AnyElementKey) extends KeyEndpoint {

    type TaxoElemKeyType = TaxonomyElemKeys.AnyElementKey
  }

  object KeyEndpoint {

    def apply(taxonomyElemKey: TaxonomyElemKeys.TaxonomyElemKey): KeyEndpoint = {
      import TaxonomyElemKeys._

      taxonomyElemKey match {
        case k: ConceptKey => ConceptKeyEndpoint(k)
        case k: ElementKey => ElementKeyEndpoint(k)
        case k: TypeKey => TypeKeyEndpoint(k)
        case k: RoleKey => RoleKeyEndpoint(k)
        case k: ArcroleKey => ArcroleKeyEndpoint(k)
        case k: AnyElementKey => AnyElementKeyEndpoint(k)
      }
    }
  }

  /**
   * Endpoint that is a non-key resource or a key to a non-key resource.
   */
  sealed trait RegularResource[+A <: XLinkResource] extends Endpoint {

    def resource: A

    final def targetResourceOption: Option[XLinkResource] = Some(resource)
  }

  /**
   * Local regular resource, corresponding to an XLink resource in a standard XBRL taxonomy document.
   * Note that this case class may have poor value equality, due to poor equality on the resource elements.
   */
  final case class LocalResource[+A <: XLinkResource](
    taxonomyElemKey: TaxonomyElemKeys.TaxonomyElemKey,
    resource: A
  ) extends RegularResource[A] with Key

  /**
   * Remote regular resource, corresponding to an XLink locator to an XLink resource in a standard XBRL taxonomy document.
   * Note that this case class may have poor value equality, due to poor equality on the resource elements.
   */
  final case class RemoteResource[+A <: XLinkResource](
    taxonomyElemKey: TaxonomyElemKeys.TaxonomyElemKey,
    resource: A
  ) extends RegularResource[A]
}
