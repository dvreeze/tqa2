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

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.TaxonomyElemKey
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.NonStandardRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.NonStandardRelationshipPath

/**
 * Purely abstract trait offering a non-standard relationship query API, typically for generic relationships.
 *
 * @author Chris de Vreeze
 */
trait NonStandardRelationshipQueryApi {

  // Input strategies, used by relationship path query methods

  /**
   * Strategy used by methods like filterOutgoingUnrestrictedNonStandardRelationshipPaths to stop appending relationships
   * to relationship paths when desired. Typically this method is used as stop condition when cycles are found.
   * If a cycle is allowed in the path, but it should stop growing beyond that, the second method parameter can be
   * ignored.
   */
  def stopAppending[A <: NonStandardRelationship](path: NonStandardRelationshipPath[A], next: A): Boolean

  /**
   * Strategy used by methods like filterIncomingUnrestrictedNonStandardRelationshipPaths to stop prepending relationships
   * to relationship paths when desired. Typically this method is used as stop condition when cycles are found.
   * If a cycle is allowed in the path, but it should stop growing beyond that, the second method parameter can be
   * ignored.
   */
  def stopPrepending[A <: NonStandardRelationship](path: NonStandardRelationshipPath[A], prev: A): Boolean

  // Query API methods

  def findAllNonStandardRelationships: Seq[NonStandardRelationship]

  def filterNonStandardRelationships(
    p: NonStandardRelationship => Boolean): Seq[NonStandardRelationship]

  def findAllNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    relationshipType: ClassTag[A]): Seq[A]

  def filterNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Finds all non-standard relationships that are outgoing from the given XML element.
   */
  def findAllOutgoingNonStandardRelationships(
    sourceKey: TaxonomyElemKey): Seq[NonStandardRelationship]

  /**
   * Filters non-standard relationships that are outgoing from the given XML element.
   */
  def filterOutgoingNonStandardRelationships(
    sourceKey: TaxonomyElemKey)(p: NonStandardRelationship => Boolean): Seq[NonStandardRelationship]

  /**
   * Finds all non-standard relationships of the given type that are outgoing from the given XML element.
   */
  def findAllOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    sourceKey: TaxonomyElemKey,
    relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters non-standard relationships of the given type that are outgoing from the given XML element.
   */
  def filterOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    sourceKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Finds all non-standard relationships that are incoming to the given XML element.
   */
  def findAllIncomingNonStandardRelationships(
    targetKey: TaxonomyElemKey): Seq[NonStandardRelationship]

  /**
   * Filters non-standard relationships that are incoming to the given XML element.
   */
  def filterIncomingNonStandardRelationships(
    targetKey: TaxonomyElemKey)(p: NonStandardRelationship => Boolean): Seq[NonStandardRelationship]

  /**
   * Finds all non-standard relationships of the given type that are incoming to the given XML element.
   */
  def findAllIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    targetKey: TaxonomyElemKey,
    relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters non-standard relationships of the given type that are incoming to the given XML element.
   */
  def filterIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    targetKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Filters the non-standard relationship paths that are outgoing from the given XML element and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "inits"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on method stopAppending returning true it stops growing.
   */
  def filterOutgoingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    sourceKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: NonStandardRelationshipPath[A] => Boolean): Seq[NonStandardRelationshipPath[A]]

  /**
   * Filters the non-standard relationship paths that are incoming to the given XML element and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "tails"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on method stopPrepending returning true it stops growing.
   */
  def filterIncomingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    targetKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: NonStandardRelationshipPath[A] => Boolean): Seq[NonStandardRelationshipPath[A]]
}
