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

import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ElementLabelRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Endpoint

/**
 * Purely abstract trait offering an element-label (non-standard) relationship query API.
 *
 * @author Chris de Vreeze
 */
trait ElementLabelRelationshipQueryApi {

  // Finding and filtering relationships without looking at source element

  def findAllElementLabelRelationships: Seq[ElementLabelRelationship]

  def filterElementLabelRelationships(
    p: ElementLabelRelationship => Boolean): Seq[ElementLabelRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all element-label relationships that are outgoing from the given XML element.
   */
  def findAllOutgoingElementLabelRelationships(
    source: Endpoint): Seq[ElementLabelRelationship]

  /**
   * Filters element-label relationships that are outgoing from the given XML element.
   */
  def filterOutgoingElementLabelRelationships(
    source: Endpoint)(p: ElementLabelRelationship => Boolean): Seq[ElementLabelRelationship]
}
