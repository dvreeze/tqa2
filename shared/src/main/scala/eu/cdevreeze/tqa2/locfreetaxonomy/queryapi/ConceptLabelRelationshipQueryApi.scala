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

import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ConceptLabelRelationship
import eu.cdevreeze.yaidom2.core.EName

/**
 * Purely abstract trait offering a (standard) concept-label relationship query API.
 *
 * @author Chris de Vreeze
 */
trait ConceptLabelRelationshipQueryApi {

  // Finding and filtering relationships without looking at source concept

  def findAllConceptLabelRelationships: Seq[ConceptLabelRelationship]

  def filterConceptLabelRelationships(
    p: ConceptLabelRelationship => Boolean): Seq[ConceptLabelRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all concept-label relationships that are outgoing from the given concept.
   */
  def findAllOutgoingConceptLabelRelationships(
    sourceConcept: EName): Seq[ConceptLabelRelationship]

  /**
   * Filters concept-label relationships that are outgoing from the given concept.
   */
  def filterOutgoingConceptLabelRelationships(
    sourceConcept: EName)(p: ConceptLabelRelationship => Boolean): Seq[ConceptLabelRelationship]
}
