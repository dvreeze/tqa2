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

package eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.internal

import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.{ConceptReferenceRelationshipQueryApi, StandardRelationshipQueryApi}
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ConceptReferenceRelationship
import eu.cdevreeze.yaidom2.core.EName

import scala.reflect.classTag

/**
 * Implementation of ConceptReferenceRelationshipQueryApi. The methods are overridable, which is needed in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultConceptReferenceRelationshipQueryApi extends ConceptReferenceRelationshipQueryApi with StandardRelationshipQueryApi {

  // Finding and filtering relationships without looking at source concept

  def findAllConceptReferenceRelationships: Seq[ConceptReferenceRelationship] = {
    findAllStandardRelationshipsOfType(classTag[ConceptReferenceRelationship])
  }

  def filterConceptReferenceRelationships(
    p: ConceptReferenceRelationship => Boolean): Seq[ConceptReferenceRelationship] = {

    filterStandardRelationshipsOfType(classTag[ConceptReferenceRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  /**
   * Finds all concept-reference relationships that are outgoing from the given concept. This must be implemented as a fast
   * method, for example by exploiting a mapping from concept names to outgoing relationships.
   */
  def findAllOutgoingConceptReferenceRelationships(
    sourceConcept: EName): Seq[ConceptReferenceRelationship] = {

    findAllOutgoingStandardRelationshipsOfType(sourceConcept, classTag[ConceptReferenceRelationship])
  }

  /**
   * Filters concept-reference relationships that are outgoing from the given concept. This must be implemented as a fast
   * method, for example by exploiting a mapping from concept names to outgoing relationships.
   */
  def filterOutgoingConceptReferenceRelationships(
    sourceConcept: EName)(p: ConceptReferenceRelationship => Boolean): Seq[ConceptReferenceRelationship] = {

    filterOutgoingStandardRelationshipsOfType(sourceConcept, classTag[ConceptReferenceRelationship])(p)
  }
}
