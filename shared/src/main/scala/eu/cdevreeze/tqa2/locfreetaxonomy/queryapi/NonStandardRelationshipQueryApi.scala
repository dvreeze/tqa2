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

import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Endpoint
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.NonStandardRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.NonStandardRelationshipPath

/**
 * Purely abstract trait offering a non-standard relationship query API, typically for generic relationships.
 *
 * @author Chris de Vreeze
 */
trait NonStandardRelationshipQueryApi {

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
    source: Endpoint): Seq[NonStandardRelationship]

  /**
   * Filters non-standard relationships that are outgoing from the given XML element.
   */
  def filterOutgoingNonStandardRelationships(
    source: Endpoint)(p: NonStandardRelationship => Boolean): Seq[NonStandardRelationship]

  /**
   * Finds all non-standard relationships of the given type that are outgoing from the given XML element.
   */
  def findAllOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    source: Endpoint,
    relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters non-standard relationships of the given type that are outgoing from the given XML element.
   */
  def filterOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    source: Endpoint,
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Finds all non-standard relationships that are incoming to the given XML element.
   */
  def findAllIncomingNonStandardRelationships(
    target: Endpoint): Seq[NonStandardRelationship]

  /**
   * Filters non-standard relationships that are incoming to the given XML element.
   */
  def filterIncomingNonStandardRelationships(
    target: Endpoint)(p: NonStandardRelationship => Boolean): Seq[NonStandardRelationship]

  /**
   * Finds all non-standard relationships of the given type that are incoming to the given XML element.
   */
  def findAllIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    target: Endpoint,
    relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters non-standard relationships of the given type that are incoming to the given XML element.
   */
  def filterIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    target: Endpoint,
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Filters the non-standard relationship paths that are outgoing from the given XML element and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "inits"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on encountering a cycle in a path it stops growing.
   */
  def filterOutgoingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    source: Endpoint,
    relationshipType: ClassTag[A])(p: NonStandardRelationshipPath[A] => Boolean): Seq[NonStandardRelationshipPath[A]]

  /**
   * Filters the non-standard relationship paths that are incoming to the given XML element and
   * whose relationships are of the given type. Only relationship paths for which all (non-empty) "tails"
   * pass the predicate are accepted by the filter! The relationship paths are as long as possible,
   * but on encountering a cycle in a path it stops growing.
   */
  def filterIncomingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    target: Endpoint,
    relationshipType: ClassTag[A])(p: NonStandardRelationshipPath[A] => Boolean): Seq[NonStandardRelationshipPath[A]]
}
