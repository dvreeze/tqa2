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

package eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy

import java.net.URI

import eu.cdevreeze.tqa2.locfreetaxonomy.dom.ExtendedLink
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XLinkArc
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XLinkResource
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Relationship

/**
 * Relationship factory, extracting the relationships from a TaxonomyBase.
 *
 * @author Chris de Vreeze
 */
trait RelationshipFactory {

  /**
   * Returns all relationships in the given TaxonomyBase passing the provided arc filter.
   */
  def extractRelationships(taxonomy: TaxonomyBase, arcFilter: XLinkArc => Boolean): Seq[Relationship]

  /**
   * Returns all relationships in the given document in the given TaxonomyBase passing the provided arc filter.
   */
  def extractRelationshipsFromDocument(docUri: URI, taxonomy: TaxonomyBase, arcFilter: XLinkArc => Boolean): Seq[Relationship]

  /**
   * Returns all relationships in the given extended link in the given TaxonomyBase passing the provided arc filter.
   */
  def extractRelationshipsFromExtendedLink(
      extendedLink: ExtendedLink,
      taxonomy: TaxonomyBase,
      arcFilter: XLinkArc => Boolean): Seq[Relationship]

  /**
   * Returns all relationships (typically one) having the given underlying XLink arc in the given TaxonomyBase.
   * For performance a mapping from XLink labels to XLink locators and resources must be provided, and this mapping
   * should be computed only once per extended link. For performance the optional parent base URI is passed as well.
   *
   * This method must respect the configuration of this RelationshipFactory.
   */
  def extractRelationshipsFromArc(
      arc: XLinkArc,
      labeledResourceMap: Map[String, Seq[XLinkResource]],
      parentBaseUriOption: Option[URI],
      taxonomy: TaxonomyBase): Seq[Relationship]
}

object RelationshipFactory {

  /**
   * Arc filter that returns true for each arc.
   */
  val AnyArc: XLinkArc => Boolean = (_ => true)
}
