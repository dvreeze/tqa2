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

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.TaxonomyElemKey
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.NonStandardRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.RelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.NonStandardRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.NonStandardRelationshipPath

/**
 * Implementation of NonStandardRelationshipQueryApi. The methods are overridable, which is needed in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultNonStandardRelationshipQueryApi extends NonStandardRelationshipQueryApi with RelationshipQueryApi {

  def findAllNonStandardRelationships: Seq[NonStandardRelationship] = {
    findAllRelationshipsOfType(classTag[NonStandardRelationship])
  }

  def filterNonStandardRelationships(p: NonStandardRelationship => Boolean): Seq[NonStandardRelationship] = {
    filterRelationshipsOfType(classTag[NonStandardRelationship])(p)
  }

  def findAllNonStandardRelationshipsOfType[A <: NonStandardRelationship](relationshipType: ClassTag[A]): Seq[A] = {
    findAllRelationshipsOfType(relationshipType)
  }

  def filterNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {

    filterRelationshipsOfType(relationshipType)(p)
  }

  def findAllOutgoingNonStandardRelationships(sourceKey: TaxonomyElemKey): Seq[NonStandardRelationship] = {
    findAllOutgoingRelationshipsOfType(sourceKey, classTag[NonStandardRelationship])
  }

  def filterOutgoingNonStandardRelationships(
    sourceKey: TaxonomyElemKey)(p: NonStandardRelationship => Boolean): Seq[NonStandardRelationship] = {

    filterOutgoingRelationshipsOfType(sourceKey, classTag[NonStandardRelationship])(p)
  }

  def findAllOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    sourceKey: TaxonomyElemKey,
    relationshipType: ClassTag[A]): Seq[A] = {

    findAllOutgoingRelationshipsOfType(sourceKey, relationshipType)
  }

  def filterOutgoingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    sourceKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {

    filterOutgoingRelationshipsOfType(sourceKey, relationshipType)(p)
  }

  def findAllIncomingNonStandardRelationships(targetKey: TaxonomyElemKey): Seq[NonStandardRelationship] = {
    findAllIncomingRelationshipsOfType(targetKey, classTag[NonStandardRelationship])
  }

  def filterIncomingNonStandardRelationships(
    targetKey: TaxonomyElemKey)(p: NonStandardRelationship => Boolean): Seq[NonStandardRelationship] = {

    filterIncomingRelationshipsOfType(targetKey, classTag[NonStandardRelationship])(p)
  }

  def findAllIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    targetKey: TaxonomyElemKey,
    relationshipType: ClassTag[A]): Seq[A] = {

    findAllIncomingRelationshipsOfType(targetKey, relationshipType)
  }

  def filterIncomingNonStandardRelationshipsOfType[A <: NonStandardRelationship](
    targetKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {

    filterIncomingRelationshipsOfType(targetKey, relationshipType)(p)
  }

  def filterOutgoingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    sourceKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: NonStandardRelationshipPath[A] => Boolean): Seq[NonStandardRelationshipPath[A]] = {

    val nextRelationships = filterOutgoingNonStandardRelationshipsOfType(sourceKey, relationshipType)(rel => p(NonStandardRelationshipPath(rel)))

    val paths =
      nextRelationships.flatMap(rel => filterOutgoingUnrestrictedNonStandardRelationshipPaths(NonStandardRelationshipPath(rel), relationshipType)(p))
    paths
  }

  def filterIncomingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    targetKey: TaxonomyElemKey,
    relationshipType: ClassTag[A])(p: NonStandardRelationshipPath[A] => Boolean): Seq[NonStandardRelationshipPath[A]] = {

    val prevRelationships = filterIncomingNonStandardRelationshipsOfType(targetKey, relationshipType)(rel => p(NonStandardRelationshipPath(rel)))

    val paths =
      prevRelationships.flatMap(rel => filterIncomingUnrestrictedNonStandardRelationshipPaths(NonStandardRelationshipPath(rel), relationshipType)(p))
    paths
  }

  // Private methods

  private def filterOutgoingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    path: NonStandardRelationshipPath[A],
    relationshipType: ClassTag[A])(p: NonStandardRelationshipPath[A] => Boolean): Seq[NonStandardRelationshipPath[A]] = {

    val nextRelationships =
      filterOutgoingNonStandardRelationshipsOfType(
        path.target.taxonomyElemKey, relationshipType)(relationship => !stopAppending(path, relationship) && p(path.append(relationship)))

    val nextPaths = nextRelationships.map(rel => path.append(rel))

    if (nextPaths.isEmpty) {
      Seq(path)
    } else {
      nextPaths.flatMap { nextPath =>
        // Recursive calls
        filterOutgoingUnrestrictedNonStandardRelationshipPaths[A](nextPath, relationshipType)(p)
      }
    }
  }

  private def filterIncomingUnrestrictedNonStandardRelationshipPaths[A <: NonStandardRelationship](
    path: NonStandardRelationshipPath[A],
    relationshipType: ClassTag[A])(p: NonStandardRelationshipPath[A] => Boolean): Seq[NonStandardRelationshipPath[A]] = {

    val prevRelationships =
      filterIncomingNonStandardRelationshipsOfType(
        path.source.taxonomyElemKey, relationshipType)(relationship => !stopPrepending(path, relationship) && p(path.prepend(relationship)))

    val prevPaths = prevRelationships.map(rel => path.prepend(rel))

    if (prevPaths.isEmpty) {
      Seq(path)
    } else {
      prevPaths.flatMap { prevPath =>
        // Recursive calls
        filterIncomingUnrestrictedNonStandardRelationshipPaths[A](prevPath, relationshipType)(p)
      }
    }
  }
}
