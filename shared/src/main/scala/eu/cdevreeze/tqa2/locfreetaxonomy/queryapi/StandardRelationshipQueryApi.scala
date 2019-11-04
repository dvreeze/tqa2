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

import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.StandardRelationship
import eu.cdevreeze.yaidom2.core.EName

/**
 * Purely abstract trait offering a standard relationship query API.
 *
 * @author Chris de Vreeze
 */
trait StandardRelationshipQueryApi {

  def findAllStandardRelationships: Seq[StandardRelationship]

  def filterStandardRelationships(
    p: StandardRelationship => Boolean): Seq[StandardRelationship]

  def findAllStandardRelationshipsOfType[A <: StandardRelationship](
    relationshipType: ClassTag[A]): Seq[A]

  def filterStandardRelationshipsOfType[A <: StandardRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Finds all standard relationships that are outgoing from the given concept.
   */
  def findAllOutgoingStandardRelationships(
    sourceConcept: EName): Seq[StandardRelationship]

  /**
   * Filters standard relationships that are outgoing from the given concept.
   */
  def filterOutgoingStandardRelationships(
    sourceConcept: EName)(p: StandardRelationship => Boolean): Seq[StandardRelationship]

  /**
   * Finds all standard relationships of the given type that are outgoing from the given concept.
   */
  def findAllOutgoingStandardRelationshipsOfType[A <: StandardRelationship](
    sourceConcept:    EName,
    relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters standard relationships of the given type that are outgoing from the given concept.
   */
  def filterOutgoingStandardRelationshipsOfType[A <: StandardRelationship](
    sourceConcept:    EName,
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  // TODO Methods to validate some closure properties, such as closure under DTS discovery rules
}
