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

import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ConceptReferenceRelationship
import eu.cdevreeze.yaidom2.core.EName

/**
 * Purely abstract trait offering a (standard) concept-reference relationship query API.
 *
 * @author Chris de Vreeze
 */
trait ConceptReferenceRelationshipQueryApi {

  // Finding and filtering relationships without looking at source concept

  def findAllConceptReferenceRelationships: Seq[ConceptReferenceRelationship]

  def filterConceptReferenceRelationships(
    p: ConceptReferenceRelationship => Boolean): Seq[ConceptReferenceRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all concept-reference relationships that are outgoing from the given concept.
   */
  def findAllOutgoingConceptReferenceRelationships(
    sourceConcept: EName): Seq[ConceptReferenceRelationship]

  /**
   * Filters concept-reference relationships that are outgoing from the given concept.
   */
  def filterOutgoingConceptReferenceRelationships(
    sourceConcept: EName)(p: ConceptReferenceRelationship => Boolean): Seq[ConceptReferenceRelationship]
}
