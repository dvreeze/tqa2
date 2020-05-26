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

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.ElementKey
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.InterConceptRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.RelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.StandardRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.InterConceptRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.InterConceptRelationshipPath
import eu.cdevreeze.yaidom2.core.EName

import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * Implementation of InterConceptRelationshipQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultInterConceptRelationshipQueryApi
    extends InterConceptRelationshipQueryApi
    with StandardRelationshipQueryApi
    with RelationshipQueryApi {

  def findAllInterConceptRelationships: Seq[InterConceptRelationship] = {
    findAllStandardRelationshipsOfType(classTag[InterConceptRelationship])
  }

  def filterInterConceptRelationships(p: InterConceptRelationship => Boolean): Seq[InterConceptRelationship] = {
    filterStandardRelationshipsOfType(classTag[InterConceptRelationship])(p)
  }

  def findAllInterConceptRelationshipsOfType[A <: InterConceptRelationship](relationshipType: ClassTag[A]): Seq[A] = {
    findAllStandardRelationshipsOfType(relationshipType)
  }

  def filterInterConceptRelationshipsOfType[A <: InterConceptRelationship](relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {
    filterStandardRelationshipsOfType(relationshipType)(p)
  }

  def findAllOutgoingInterConceptRelationships(sourceConcept: EName): Seq[InterConceptRelationship] = {
    findAllOutgoingStandardRelationshipsOfType(sourceConcept, classTag[InterConceptRelationship])
  }

  def filterOutgoingInterConceptRelationships(sourceConcept: EName)(
      p: InterConceptRelationship => Boolean): Seq[InterConceptRelationship] = {
    filterOutgoingStandardRelationshipsOfType(sourceConcept, classTag[InterConceptRelationship])(p)
  }

  def findAllOutgoingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A]): Seq[A] = {

    findAllOutgoingStandardRelationshipsOfType(sourceConcept, relationshipType)
  }

  def filterOutgoingInterConceptRelationshipsOfType[A <: InterConceptRelationship](sourceConcept: EName, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A] = {

    filterOutgoingStandardRelationshipsOfType(sourceConcept, relationshipType)(p)
  }

  def findAllConsecutiveInterConceptRelationshipsOfType[A <: InterConceptRelationship](
      relationship: InterConceptRelationship,
      resultRelationshipType: ClassTag[A]): Seq[A] = {

    filterOutgoingInterConceptRelationshipsOfType(relationship.targetConcept, resultRelationshipType) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  def findAllIncomingInterConceptRelationships(targetConcept: EName): Seq[InterConceptRelationship] = {
    findAllIncomingRelationshipsOfType(ElementKey(targetConcept), classTag[InterConceptRelationship])
  }

  def filterIncomingInterConceptRelationships(targetConcept: EName)(
      p: InterConceptRelationship => Boolean): Seq[InterConceptRelationship] = {
    filterIncomingRelationshipsOfType(ElementKey(targetConcept), classTag[InterConceptRelationship])(p)
  }

  def findAllIncomingInterConceptRelationshipsOfType[A <: InterConceptRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A]): Seq[A] = {

    findAllIncomingRelationshipsOfType(ElementKey(targetConcept), relationshipType)
  }

  def filterIncomingInterConceptRelationshipsOfType[A <: InterConceptRelationship](targetConcept: EName, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A] = {

    filterIncomingRelationshipsOfType(ElementKey(targetConcept), relationshipType)(p)
  }

  def filterOutgoingConsecutiveInterConceptRelationshipPaths[A <: InterConceptRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): Seq[InterConceptRelationshipPath[A]] = {

    filterOutgoingUnrestrictedInterConceptRelationshipPaths(sourceConcept, relationshipType) { path =>
      path.isConsecutiveRelationshipPath && p(path)
    }
  }

  def filterIncomingConsecutiveInterConceptRelationshipPaths[A <: InterConceptRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): Seq[InterConceptRelationshipPath[A]] = {

    filterIncomingUnrestrictedInterConceptRelationshipPaths(targetConcept, relationshipType) { path =>
      path.isConsecutiveRelationshipPath && p(path)
    }
  }

  def filterOutgoingUnrestrictedInterConceptRelationshipPaths[A <: InterConceptRelationship](
      sourceConcept: EName,
      relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): Seq[InterConceptRelationshipPath[A]] = {

    val nextRelationships =
      filterOutgoingInterConceptRelationshipsOfType(sourceConcept, relationshipType)(rel => p(InterConceptRelationshipPath(rel)))

    val paths =
      nextRelationships.flatMap(rel =>
        filterOutgoingUnrestrictedInterConceptRelationshipPaths(InterConceptRelationshipPath(rel), relationshipType)(p))
    paths
  }

  def filterIncomingUnrestrictedInterConceptRelationshipPaths[A <: InterConceptRelationship](
      targetConcept: EName,
      relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): Seq[InterConceptRelationshipPath[A]] = {

    val prevRelationships =
      filterIncomingInterConceptRelationshipsOfType(targetConcept, relationshipType)(rel => p(InterConceptRelationshipPath(rel)))

    val paths =
      prevRelationships.flatMap(rel =>
        filterIncomingUnrestrictedInterConceptRelationshipPaths(InterConceptRelationshipPath(rel), relationshipType)(p))
    paths
  }

  // Private methods

  private def filterOutgoingUnrestrictedInterConceptRelationshipPaths[A <: InterConceptRelationship](
      path: InterConceptRelationshipPath[A],
      relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): Seq[InterConceptRelationshipPath[A]] = {

    val nextRelationships: Seq[A] =
      filterOutgoingInterConceptRelationshipsOfType(
        path.targetConcept,
        relationshipType
      )(relationship => !stopAppending(path, relationship) && p(path.append(relationship)))

    val nextPaths = nextRelationships.map(rel => path.append(rel))

    if (nextPaths.isEmpty) {
      Seq(path)
    } else {
      nextPaths.flatMap { nextPath =>
        // Recursive calls
        filterOutgoingUnrestrictedInterConceptRelationshipPaths[A](nextPath, relationshipType)(p)
      }
    }
  }

  private def filterIncomingUnrestrictedInterConceptRelationshipPaths[A <: InterConceptRelationship](
      path: InterConceptRelationshipPath[A],
      relationshipType: ClassTag[A])(p: InterConceptRelationshipPath[A] => Boolean): Seq[InterConceptRelationshipPath[A]] = {

    val prevRelationships: Seq[A] =
      filterIncomingInterConceptRelationshipsOfType(
        path.sourceConcept,
        relationshipType
      )(relationship => !stopPrepending(path, relationship) && p(path.prepend(relationship)))

    val prevPaths = prevRelationships.map(rel => path.prepend(rel))

    if (prevPaths.isEmpty) {
      Seq(path)
    } else {
      prevPaths.flatMap { prevPath =>
        // Recursive calls
        filterIncomingUnrestrictedInterConceptRelationshipPaths[A](prevPath, relationshipType)(p)
      }
    }
  }
}
