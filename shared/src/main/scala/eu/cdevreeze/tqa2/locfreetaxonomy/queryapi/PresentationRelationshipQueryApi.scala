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

import scala.reflect.ClassTag

import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ParentChildRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ParentChildRelationshipPath
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.PresentationRelationship
import eu.cdevreeze.yaidom2.core.EName

/**
 * Purely abstract trait offering a (standard) presentation relationship query API.
 *
 * @author Chris de Vreeze
 */
trait PresentationRelationshipQueryApi {

  // Input strategies, used by relationship path query methods

  /**
   * Strategy used by methods like findAllOutgoingConsecutiveParentChildRelationshipPaths to stop appending relationships
   * to relationship paths when desired. Typically this method is used as stop condition when cycles are found.
   * If a cycle is allowed in the path, but it should stop growing beyond that, the second method parameter can be
   * ignored.
   */
  def stopAppending(path: ParentChildRelationshipPath, next: ParentChildRelationship): Boolean

  /**
   * Strategy used by methods like findAllIncomingConsecutiveParentChildRelationshipPaths to stop prepending relationships
   * to relationship paths when desired. Typically this method is used as stop condition when cycles are found.
   * If a cycle is allowed in the path, but it should stop growing beyond that, the second method parameter can be
   * ignored.
   */
  def stopPrepending(path: ParentChildRelationshipPath, prev: ParentChildRelationship): Boolean

  // Query API methods

  // Finding and filtering relationships without looking at source or target concept

  def findAllPresentationRelationshipsOfType[A <: PresentationRelationship](
    relationshipType: ClassTag[A]): Seq[A]

  def filterPresentationRelationshipsOfType[A <: PresentationRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  def findAllParentChildRelationships: Seq[ParentChildRelationship]

  def filterParentChildRelationships(
    p: ParentChildRelationship => Boolean): Seq[ParentChildRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all parent-child relationships that are outgoing from the given concept.
   */
  def findAllOutgoingParentChildRelationships(
    sourceConcept: EName): Seq[ParentChildRelationship]

  /**
   * Filters parent-child relationships that are outgoing from the given concept.
   */
  def filterOutgoingParentChildRelationships(
    sourceConcept: EName)(p: ParentChildRelationship => Boolean): Seq[ParentChildRelationship]

  /**
   * Filters parent-child relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingParentChildRelationshipsOnElr(
    sourceConcept: EName, elr: String): Seq[ParentChildRelationship]

  /**
   * Finds all "following" ("consecutive") parent-child relationships.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingParentChildRelationships(relationship.targetConceptEName) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveParentChildRelationships(
    relationship: ParentChildRelationship): Seq[ParentChildRelationship]

  // Finding and filtering incoming relationships

  /**
   * Finds all parent-child relationships that are incoming to the given concept.
   */
  def findAllIncomingParentChildRelationships(
    targetConcept: EName): Seq[ParentChildRelationship]

  /**
   * Filters parent-child relationships that are incoming to the given concept.
   */
  def filterIncomingParentChildRelationships(
    targetConcept: EName)(p: ParentChildRelationship => Boolean): Seq[ParentChildRelationship]

  // Filtering outgoing and incoming relationship paths

  /**
   * Returns `filterOutgoingConsecutiveParentChildRelationshipPaths(sourceConcept)(_ => true)`.
   */
  def findAllOutgoingConsecutiveParentChildRelationshipPaths(
    sourceConcept: EName): Seq[ParentChildRelationshipPath]

  /**
   * Filters the consecutive (!) parent-child relationship paths that are outgoing from the given concept.
   * Only relationship paths for which all (non-empty) "inits" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on method stopAppending returning true it stops growing.
   */
  def filterOutgoingConsecutiveParentChildRelationshipPaths(
    sourceConcept: EName)(
    p: ParentChildRelationshipPath => Boolean): Seq[ParentChildRelationshipPath]

  /**
   * Returns `filterIncomingConsecutiveParentChildRelationshipPaths(targetConcept)(_ => true)`.
   */
  def findAllIncomingConsecutiveParentChildRelationshipPaths(
    targetConcept: EName): Seq[ParentChildRelationshipPath]

  /**
   * Filters the consecutive (!) parent-child relationship paths that are incoming to the given concept.
   * Only relationship paths for which all (non-empty) "tails" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on method stopPrepending returning true it stops growing.
   */
  def filterIncomingConsecutiveParentChildRelationshipPaths(
    targetConcept: EName)(p: ParentChildRelationshipPath => Boolean): Seq[ParentChildRelationshipPath]
}
