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
import scala.reflect.ClassTag

import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.InterConceptRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.ParentChildRelationshipPath
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.PresentationRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ParentChildRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.PresentationRelationship
import eu.cdevreeze.yaidom2.core.EName

/**
 * Implementation of PresentationRelationshipQueryApi. The methods are overridable, which is needed in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultPresentationRelationshipQueryApi extends PresentationRelationshipQueryApi with InterConceptRelationshipQueryApi {

  // Finding and filtering relationships without looking at source or target concept

  def findAllPresentationRelationshipsOfType[A <: PresentationRelationship](relationshipType: ClassTag[A]): Seq[A] = {
    findAllInterConceptRelationshipsOfType(relationshipType)
  }

  def filterPresentationRelationshipsOfType[A <: PresentationRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {

    filterInterConceptRelationshipsOfType(relationshipType)(p)
  }

  def findAllParentChildRelationships: Seq[ParentChildRelationship] = {
    findAllInterConceptRelationshipsOfType(classTag[ParentChildRelationship])
  }

  def filterParentChildRelationships(p: ParentChildRelationship => Boolean): Seq[ParentChildRelationship] = {
    filterInterConceptRelationshipsOfType(classTag[ParentChildRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  def findAllOutgoingParentChildRelationships(sourceConcept: EName): Seq[ParentChildRelationship] = {
    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[ParentChildRelationship])
  }

  def filterOutgoingParentChildRelationships(
    sourceConcept: EName)(p: ParentChildRelationship => Boolean): Seq[ParentChildRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[ParentChildRelationship])(p)
  }

  def filterOutgoingParentChildRelationshipsOnElr(sourceConcept: EName, elr: String): Seq[ParentChildRelationship] = {
    filterOutgoingParentChildRelationships(sourceConcept)(_.elr == elr)
  }

  def findAllConsecutiveParentChildRelationships(relationship: ParentChildRelationship): Seq[ParentChildRelationship] = {
    filterOutgoingParentChildRelationships(relationship.targetConcept) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  // Finding and filtering incoming relationships

  def findAllIncomingParentChildRelationships(targetConcept: EName): Seq[ParentChildRelationship] = {
    findAllIncomingInterConceptRelationshipsOfType(targetConcept, classTag[ParentChildRelationship])
  }

  def filterIncomingParentChildRelationships(
    targetConcept: EName)(p: ParentChildRelationship => Boolean): Seq[ParentChildRelationship] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, classTag[ParentChildRelationship])(p)
  }

  // Filtering outgoing and incoming relationship paths

  def findAllOutgoingConsecutiveParentChildRelationshipPaths(sourceConcept: EName): Seq[ParentChildRelationshipPath] = {
    filterOutgoingConsecutiveParentChildRelationshipPaths(sourceConcept)(_ => true)
  }

  def filterOutgoingConsecutiveParentChildRelationshipPaths(
    sourceConcept: EName)(
    p: ParentChildRelationshipPath => Boolean): Seq[ParentChildRelationshipPath] = {

    filterOutgoingConsecutiveInterConceptRelationshipPaths(sourceConcept, classTag[ParentChildRelationship])(p)
  }

  def findAllIncomingConsecutiveParentChildRelationshipPaths(targetConcept: EName): Seq[ParentChildRelationshipPath] = {
    filterIncomingConsecutiveParentChildRelationshipPaths(targetConcept)(_ => true)
  }

  def filterIncomingConsecutiveParentChildRelationshipPaths(
    targetConcept: EName)(p: ParentChildRelationshipPath => Boolean): Seq[ParentChildRelationshipPath] = {

    filterIncomingConsecutiveInterConceptRelationshipPaths(targetConcept, classTag[ParentChildRelationship])(p)
  }
}
