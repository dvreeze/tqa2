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

import scala.reflect.classTag

import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.ConceptLabelRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.StandardRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ConceptLabelRelationship
import eu.cdevreeze.yaidom2.core.EName

/**
 * Implementation of ConceptLabelRelationshipQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultConceptLabelRelationshipQueryApi extends ConceptLabelRelationshipQueryApi with StandardRelationshipQueryApi {

  // Finding and filtering relationships without looking at source concept

  def findAllConceptLabelRelationships: Seq[ConceptLabelRelationship] = {
    findAllStandardRelationshipsOfType(classTag[ConceptLabelRelationship])
  }

  def filterConceptLabelRelationships(
    p: ConceptLabelRelationship => Boolean): Seq[ConceptLabelRelationship] = {

    filterStandardRelationshipsOfType(classTag[ConceptLabelRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  /**
   * Finds all concept-label relationships that are outgoing from the given concept. This must be implemented as a fast
   * method, for example by exploiting a mapping from concept names to outgoing relationships.
   */
  def findAllOutgoingConceptLabelRelationships(
    sourceConcept: EName): Seq[ConceptLabelRelationship] = {

    findAllOutgoingStandardRelationshipsOfType(sourceConcept, classTag[ConceptLabelRelationship])
  }

  /**
   * Filters concept-label relationships that are outgoing from the given concept. This must be implemented as a fast
   * method, for example by exploiting a mapping from concept names to outgoing relationships.
   */
  def filterOutgoingConceptLabelRelationships(
    sourceConcept: EName)(p: ConceptLabelRelationship => Boolean): Seq[ConceptLabelRelationship] = {

    filterOutgoingStandardRelationshipsOfType(sourceConcept, classTag[ConceptLabelRelationship])(p)
  }
}
