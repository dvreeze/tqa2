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
 * XLink resources. In terms of XBRL taxonomies, they represent XLink locators (other than those to XLink resources), XLink
 * resources, and locators to XLink resources, respectively.
 *
 * @author Chris de Vreeze
 */
sealed trait Endpoint {

  /**
   * The endpoint as key, either because the endpoint is a key, or because it is a resource which (like all taxonomy elements) has a key.
   */
  def taxonomyElemKey: TaxonomyElemKeys.TaxonomyElemKey
}

object Endpoint {

  /**
   * An endpoint that is a key.
   */
  sealed trait Key extends Endpoint

  /**
   * Endpoint that is a key to an element that is not a resource, like a concept key.
   */
  final case class KeyEndpoint(taxonomyElemKey: TaxonomyElemKeys.TaxonomyElemKey) extends Key

  /**
   * Endpoint that is a non-key resource or a key to a non-key resource.
   */
  sealed trait RegularResource[+E <: XLinkResource] extends Endpoint {

    def resource: E
  }

  final case class LocalResource[+E <: XLinkResource](
    taxonomyElemKey: TaxonomyElemKeys.TaxonomyElemKey,
    resource: E
  ) extends RegularResource[E] with Key

  final case class RemoteResource[+E <: XLinkResource](
    taxonomyElemKey: TaxonomyElemKeys.TaxonomyElemKey,
    resource: E
  ) extends RegularResource[E]
}
