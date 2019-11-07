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
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Relationship

/**
 * Purely abstract trait offering a (general) relationship query API. This is not only a public query API, but also
 * a trait that should have efficient implementations that form the basis for the implementations of many other query
 * API traits. Hence it is important that all methods of this trait have efficient implementations, especially with respect
 * to execution speed.
 *
 * @author Chris de Vreeze
 */
trait RelationshipQueryApi {

  /**
   * Finds all relationships of the given relationship type. Implementations of this method should be fast, for example
   * by using a mapping from class tags to relationship collections. This must be a fast method.
   */
  def findAllRelationshipsOfType[A <: Relationship](relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters the relationships of the given relationship type obeying the given predicate. Implementations of this method
   * should be fast, for example by using a mapping from class tags to relationship collections. This must be a fast method.
   */
  def filterRelationshipsOfType[A <: Relationship](relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Finds all relationships that are outgoing from the given endpoint. This must be a fast method.
   */
  def findAllOutgoingRelationships(sourceKey: TaxonomyElemKey): Seq[Relationship]

  /**
   * Filters relationships that are outgoing from the given endpoint. This must be a fast method.
   */
  def filterOutgoingRelationships(sourceKey: TaxonomyElemKey)(p: Relationship => Boolean): Seq[Relationship]

  /**
   * Finds all relationships of the given type that are outgoing from the given endpoint. This must be a fast method.
   */
  def findAllOutgoingRelationshipsOfType[A <: Relationship](
    sourceKey: TaxonomyElemKey,
    relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters relationships of the given type that are outgoing from the given endpoint. This must be a fast method.
   */
  def filterOutgoingRelationshipsOfType[A <: Relationship](
    sourceKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Finds all relationships that are incoming to the given endpoint. This must be a fast method.
   */
  def findAllIncomingRelationships(targetKey: TaxonomyElemKey): Seq[Relationship]

  /**
   * Filters relationships that are incoming to the given endpoint. This must be a fast method.
   */
  def filterIncomingRelationships(targetKey: TaxonomyElemKey)(p: Relationship => Boolean): Seq[Relationship]

  /**
   * Finds all relationships of the given type that are incoming to the given endpoint. This must be a fast method.
   */
  def findAllIncomingRelationshipsOfType[A <: Relationship](
    targetKey: TaxonomyElemKey,
    relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters relationships of the given type that are incoming to the given endpoint. This must be a fast method.
   */
  def filterIncomingRelationshipsOfType[A <: Relationship](
    targetKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]
}
