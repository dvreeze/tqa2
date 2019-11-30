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

package eu.cdevreeze.tqa2.locfreetaxonomy.queryapi

import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Linkbase
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Relationship

/**
 * Purely abstract trait offering a taxonomy query API.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyQueryApi
  extends TaxonomySchemaQueryApi
    with RelationshipQueryApi
    with StandardRelationshipQueryApi
    with NonStandardRelationshipQueryApi
    with InterConceptRelationshipQueryApi
    with PresentationRelationshipQueryApi
    with ConceptLabelRelationshipQueryApi
    with ConceptReferenceRelationshipQueryApi
    with ElementLabelRelationshipQueryApi
    with ElementReferenceRelationshipQueryApi
    with DimensionalRelationshipQueryApi {

  /**
   * Returns all relationships in the taxonomy
   */
  def relationships: Seq[Relationship]

  /**
   * Returns all (document) root elements. To find certain taxonomy elements across the taxonomy,
   * in taxonomy schemas and linkbases, the following pattern can be used:
   * {{{
   * rootElems.flatMap(_.filterElemsOrSelfOfType(classTag[E])(pred))
   * }}}
   */
  def rootElems: Seq[TaxonomyElem]

  /**
   * Returns the linkbase elements. To find certain extended links, the following pattern can be used:
   * {{{
   * findAllLinkbases.flatMap(_.findAllExtendedLinks.filter(pred))
   * }}}
   */
  def findAllLinkbases: Seq[Linkbase]

}
