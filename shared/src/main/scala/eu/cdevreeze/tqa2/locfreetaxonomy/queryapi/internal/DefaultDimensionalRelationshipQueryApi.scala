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

import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.DimensionalRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.InterConceptRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.DimensionDefaultRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.DimensionDomainRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.DimensionalRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.DomainAwareRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.DomainAwareRelationshipPath
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.DomainMemberRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.DomainMemberRelationshipPath
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.HypercubeDimensionRelationship
import eu.cdevreeze.yaidom2.core.EName

/**
 * Implementation of DimensionalRelationshipQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultDimensionalRelationshipQueryApi extends DimensionalRelationshipQueryApi with InterConceptRelationshipQueryApi {

  def stopAppending(path: DomainAwareRelationshipPath, next: DomainAwareRelationship): Boolean = {
    stopAppending[DomainAwareRelationship](path, next)
  }

  def stopPrepending(path: DomainAwareRelationshipPath, prev: DomainAwareRelationship): Boolean = {
    stopPrepending[DomainAwareRelationship](path, prev)
  }

  def stopAppending(path: DomainMemberRelationshipPath, next: DomainMemberRelationship): Boolean = {
    stopAppending[DomainMemberRelationship](path, next)
  }

  def stopPrepending(path: DomainMemberRelationshipPath, prev: DomainMemberRelationship): Boolean = {
    stopPrepending[DomainMemberRelationship](path, prev)
  }

  // Finding and filtering relationships without looking at source or target concept

  def findAllDimensionalRelationships: Seq[DimensionalRelationship] = {
    findAllInterConceptRelationshipsOfType(classTag[DimensionalRelationship])
  }

  def filterDimensionalRelationships(p: DimensionalRelationship => Boolean): Seq[DimensionalRelationship] = {
    filterInterConceptRelationshipsOfType(classTag[DimensionalRelationship])(p)
  }

  def findAllDimensionalRelationshipsOfType[A <: DimensionalRelationship](relationshipType: ClassTag[A]): Seq[A] = {
    findAllInterConceptRelationshipsOfType(relationshipType)
  }

  def filterDimensionalRelationshipsOfType[A <: DimensionalRelationship](relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {
    filterInterConceptRelationshipsOfType(relationshipType)(p)
  }

  def findAllHasHypercubeRelationships: Seq[HasHypercubeRelationship] = {
    findAllDimensionalRelationshipsOfType(classTag[HasHypercubeRelationship])
  }

  def filterHasHypercubeRelationships(p: HasHypercubeRelationship => Boolean): Seq[HasHypercubeRelationship] = {
    filterDimensionalRelationshipsOfType(classTag[HasHypercubeRelationship])(p)
  }

  def findAllHypercubeDimensionRelationships: Seq[HypercubeDimensionRelationship] = {
    findAllDimensionalRelationshipsOfType(classTag[HypercubeDimensionRelationship])
  }

  def filterHypercubeDimensionRelationships(p: HypercubeDimensionRelationship => Boolean): Seq[HypercubeDimensionRelationship] = {
    filterDimensionalRelationshipsOfType(classTag[HypercubeDimensionRelationship])(p)
  }

  def findAllDimensionDomainRelationships: Seq[DimensionDomainRelationship] = {
    findAllDimensionalRelationshipsOfType(classTag[DimensionDomainRelationship])
  }

  def filterDimensionDomainRelationships(p: DimensionDomainRelationship => Boolean): Seq[DimensionDomainRelationship] = {
    filterDimensionalRelationshipsOfType(classTag[DimensionDomainRelationship])(p)
  }

  def findAllDomainMemberRelationships: Seq[DomainMemberRelationship] = {
    findAllDimensionalRelationshipsOfType(classTag[DomainMemberRelationship])
  }

  def filterDomainMemberRelationships(p: DomainMemberRelationship => Boolean): Seq[DomainMemberRelationship] = {
    filterDimensionalRelationshipsOfType(classTag[DomainMemberRelationship])(p)
  }

  def findAllDimensionDefaultRelationships: Seq[DimensionDefaultRelationship] = {
    findAllDimensionalRelationshipsOfType(classTag[DimensionDefaultRelationship])
  }

  def filterDimensionDefaultRelationships(p: DimensionDefaultRelationship => Boolean): Seq[DimensionDefaultRelationship] = {
    filterDimensionalRelationshipsOfType(classTag[DimensionDefaultRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  def findAllOutgoingHasHypercubeRelationships(sourceConcept: EName): Seq[HasHypercubeRelationship] = {
    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HasHypercubeRelationship])
  }

  def filterOutgoingHasHypercubeRelationships(sourceConcept: EName)(p: HasHypercubeRelationship => Boolean): Seq[HasHypercubeRelationship] = {
    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HasHypercubeRelationship])(p)
  }

  def filterOutgoingHasHypercubeRelationshipsOnElr(sourceConcept: EName, elr: String): Seq[HasHypercubeRelationship] = {
    filterOutgoingHasHypercubeRelationships(sourceConcept)(_.elr == elr)
  }

  def findAllOutgoingHypercubeDimensionRelationships(sourceConcept: EName): Seq[HypercubeDimensionRelationship] = {
    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HypercubeDimensionRelationship])
  }

  def filterOutgoingHypercubeDimensionRelationships(
    sourceConcept: EName)(p: HypercubeDimensionRelationship => Boolean): Seq[HypercubeDimensionRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[HypercubeDimensionRelationship])(p)
  }

  def filterOutgoingHypercubeDimensionRelationshipsOnElr(
    sourceConcept: EName, elr: String): Seq[HypercubeDimensionRelationship] = {

    filterOutgoingHypercubeDimensionRelationships(sourceConcept)(_.elr == elr)
  }

  def findAllConsecutiveHypercubeDimensionRelationships(
    relationship: HasHypercubeRelationship): Seq[HypercubeDimensionRelationship] = {

    filterOutgoingHypercubeDimensionRelationships(relationship.hypercube) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  def findAllOutgoingDimensionDomainRelationships(sourceConcept: EName): Seq[DimensionDomainRelationship] = {
    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDomainRelationship])
  }

  def filterOutgoingDimensionDomainRelationships(
    sourceConcept: EName)(p: DimensionDomainRelationship => Boolean): Seq[DimensionDomainRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDomainRelationship])(p)
  }

  def filterOutgoingDimensionDomainRelationshipsOnElr(
    sourceConcept: EName, elr: String): Seq[DimensionDomainRelationship] = {

    filterOutgoingDimensionDomainRelationships(sourceConcept)(_.elr == elr)
  }

  def findAllConsecutiveDimensionDomainRelationships(
    relationship: HypercubeDimensionRelationship): Seq[DimensionDomainRelationship] = {

    filterOutgoingDimensionDomainRelationships(relationship.dimension) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  def findAllOutgoingDomainMemberRelationships(sourceConcept: EName): Seq[DomainMemberRelationship] = {
    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DomainMemberRelationship])
  }

  def filterOutgoingDomainMemberRelationships(
    sourceConcept: EName)(p: DomainMemberRelationship => Boolean): Seq[DomainMemberRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DomainMemberRelationship])(p)
  }

  def filterOutgoingDomainMemberRelationshipsOnElr(sourceConcept: EName, elr: String): Seq[DomainMemberRelationship] = {
    filterOutgoingDomainMemberRelationships(sourceConcept)(_.elr == elr)
  }

  def findAllConsecutiveDomainMemberRelationships(relationship: DomainAwareRelationship): Seq[DomainMemberRelationship] = {
    filterOutgoingDomainMemberRelationships(relationship.targetConcept) { rel =>
      relationship.isFollowedBy(rel)
    }
  }

  def findAllOutgoingDimensionDefaultRelationships(sourceConcept: EName): Seq[DimensionDefaultRelationship] = {
    findAllOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDefaultRelationship])
  }

  def filterOutgoingDimensionDefaultRelationships(
    sourceConcept: EName)(p: DimensionDefaultRelationship => Boolean): Seq[DimensionDefaultRelationship] = {

    filterOutgoingInterConceptRelationshipsOfType(sourceConcept, classTag[DimensionDefaultRelationship])(p)
  }

  def filterOutgoingDimensionDefaultRelationshipsOnElr(
    sourceConcept: EName, elr: String): Seq[DimensionDefaultRelationship] = {

    filterOutgoingDimensionDefaultRelationships(sourceConcept)(_.elr == elr)
  }

  // Finding and filtering incoming relationships

  def findAllIncomingDomainMemberRelationships(targetConcept: EName): Seq[DomainMemberRelationship] = {
    findAllIncomingInterConceptRelationshipsOfType(targetConcept, classTag[DomainMemberRelationship])
  }

  def filterIncomingDomainMemberRelationships(targetConcept: EName)(p: DomainMemberRelationship => Boolean): Seq[DomainMemberRelationship] = {
    filterIncomingInterConceptRelationshipsOfType(targetConcept, classTag[DomainMemberRelationship])(p)
  }

  def findAllIncomingDomainAwareRelationships(targetConcept: EName): Seq[DomainAwareRelationship] = {
    findAllIncomingInterConceptRelationshipsOfType(targetConcept, classTag[DomainAwareRelationship])
  }

  def filterIncomingDomainAwareRelationships(
    targetConcept: EName)(p: DomainAwareRelationship => Boolean): Seq[DomainAwareRelationship] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, classTag[DomainAwareRelationship])(p)
  }

  def findAllIncomingHypercubeDimensionRelationships(targetConcept: EName): Seq[HypercubeDimensionRelationship] = {
    findAllIncomingInterConceptRelationshipsOfType(targetConcept, classTag[HypercubeDimensionRelationship])
  }

  def filterIncomingHypercubeDimensionRelationships(
    targetConcept: EName)(p: HypercubeDimensionRelationship => Boolean): Seq[HypercubeDimensionRelationship] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, classTag[HypercubeDimensionRelationship])(p)
  }

  def findAllIncomingHasHypercubeRelationships(targetConcept: EName): Seq[HasHypercubeRelationship] = {
    findAllIncomingInterConceptRelationshipsOfType(targetConcept, classTag[HasHypercubeRelationship])
  }

  def filterIncomingHasHypercubeRelationships(
    targetConcept: EName)(p: HasHypercubeRelationship => Boolean): Seq[HasHypercubeRelationship] = {

    filterIncomingInterConceptRelationshipsOfType(targetConcept, classTag[HasHypercubeRelationship])(p)
  }

  // Filtering outgoing and incoming relationship paths

  def findAllOutgoingConsecutiveDomainAwareRelationshipPaths(
    sourceConcept: EName): Seq[DomainAwareRelationshipPath] = {

    filterOutgoingConsecutiveDomainAwareRelationshipPaths(sourceConcept)(_ => true)
  }

  def filterOutgoingConsecutiveDomainAwareRelationshipPaths(
    sourceConcept: EName)(
    p: DomainAwareRelationshipPath => Boolean): Seq[DomainAwareRelationshipPath] = {

    filterOutgoingConsecutiveInterConceptRelationshipPaths(sourceConcept, classTag[DomainAwareRelationship])(p)
  }

  def findAllOutgoingConsecutiveDomainMemberRelationshipPaths(
    sourceConcept: EName): Seq[DomainMemberRelationshipPath] = {

    filterOutgoingConsecutiveDomainMemberRelationshipPaths(sourceConcept)(_ => true)
  }

  def filterOutgoingConsecutiveDomainMemberRelationshipPaths(
    sourceConcept: EName)(
    p: DomainMemberRelationshipPath => Boolean): Seq[DomainMemberRelationshipPath] = {

    filterOutgoingConsecutiveInterConceptRelationshipPaths(sourceConcept, classTag[DomainMemberRelationship])(p)
  }

  def findAllIncomingConsecutiveDomainAwareRelationshipPaths(
    targetConcept: EName): Seq[DomainAwareRelationshipPath] = {

    filterIncomingConsecutiveDomainAwareRelationshipPaths(targetConcept)(_ => true)
  }

  def filterIncomingConsecutiveDomainAwareRelationshipPaths(
    targetConcept: EName)(p: DomainAwareRelationshipPath => Boolean): Seq[DomainAwareRelationshipPath] = {

    filterIncomingConsecutiveInterConceptRelationshipPaths(targetConcept, classTag[DomainAwareRelationship])(p)
  }

  def findAllIncomingConsecutiveDomainMemberRelationshipPaths(targetConcept: EName): Seq[DomainMemberRelationshipPath] = {
    filterIncomingConsecutiveDomainMemberRelationshipPaths(targetConcept)(_ => true)
  }

  def filterIncomingConsecutiveDomainMemberRelationshipPaths(
    targetConcept: EName)(p: DomainMemberRelationshipPath => Boolean): Seq[DomainMemberRelationshipPath] = {

    filterIncomingConsecutiveInterConceptRelationshipPaths(targetConcept, classTag[DomainMemberRelationship])(p)
  }

  // Other query methods

  def findAllOwnOrInheritedHasHypercubes(concept: EName): Seq[HasHypercubeRelationship] = {
    val incomingRelationshipPaths = findAllIncomingConsecutiveDomainMemberRelationshipPaths(concept)

    val domainMemberRelationships = incomingRelationshipPaths.flatMap(_.relationships)

    val inheritedElrSourceConceptPairs = domainMemberRelationships.map(rel => rel.elr -> rel.sourceConcept)

    val ownElrSourceConceptPairs =
      findAllOutgoingHasHypercubeRelationships(concept).map(rel => rel.elr -> rel.sourceConcept)

    val elrSourceConceptPairs = (inheritedElrSourceConceptPairs ++ ownElrSourceConceptPairs).distinct

    val hasHypercubes =
      elrSourceConceptPairs flatMap {
        case (elr, sourceConcept) =>
          filterOutgoingHasHypercubeRelationshipsOnElr(sourceConcept, elr)
      }

    hasHypercubes
  }

  def findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]] = {
    val hasHypercubes = findAllOwnOrInheritedHasHypercubes(concept)

    hasHypercubes.groupBy(_.elr).view.mapValues(_.map(_.sourceConcept).toSet).toMap
  }

  def findAllInheritedHasHypercubes(concept: EName): Seq[HasHypercubeRelationship] = {
    val incomingRelationshipPaths =
      findAllIncomingConsecutiveDomainMemberRelationshipPaths(concept)

    val domainMemberRelationships = incomingRelationshipPaths.flatMap(_.relationships)

    val inheritedElrSourceConceptPairs =
      domainMemberRelationships.map(rel => rel.elr -> rel.sourceConcept).distinct

    val hasHypercubes =
      inheritedElrSourceConceptPairs flatMap {
        case (elr, sourceConcept) =>
          filterOutgoingHasHypercubeRelationshipsOnElr(sourceConcept, elr)
      }

    hasHypercubes
  }

  def findAllInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]] = {
    val hasHypercubes = findAllInheritedHasHypercubes(concept)

    hasHypercubes.groupBy(_.elr).view.mapValues(_.map(_.sourceConcept).toSet).toMap
  }

  def computeFilteredHasHypercubeInheritanceOrSelf(
    p: HasHypercubeRelationship => Boolean): Map[EName, Seq[HasHypercubeRelationship]] = {

    val hasHypercubes = filterHasHypercubeRelationships(p)

    val conceptHasHypercubes: Seq[(EName, HasHypercubeRelationship)] =
      hasHypercubes.flatMap { hasHypercube =>
        val domainMemberPaths =
          filterOutgoingConsecutiveDomainMemberRelationshipPaths(hasHypercube.primary)(_.firstRelationship.elr == hasHypercube.elr)

        val inheritingConcepts = domainMemberPaths.flatMap(_.relationships).map(_.targetConcept).distinct
        val ownOrInheritingConcepts = hasHypercube.primary +: inheritingConcepts

        ownOrInheritingConcepts.map(concept => concept -> hasHypercube)
      }

    conceptHasHypercubes.groupBy(_._1).view.mapValues(_.map(_._2).distinct).toMap
  }

  def computeFilteredHasHypercubeInheritance(
    p: HasHypercubeRelationship => Boolean): Map[EName, Seq[HasHypercubeRelationship]] = {

    val hasHypercubes = filterHasHypercubeRelationships(p)

    val conceptHasHypercubes: Seq[(EName, HasHypercubeRelationship)] =
      hasHypercubes.flatMap { hasHypercube =>
        val domainMemberPaths =
          filterOutgoingConsecutiveDomainMemberRelationshipPaths(hasHypercube.primary)(_.firstRelationship.elr == hasHypercube.elr)

        val inheritingConcepts = domainMemberPaths.flatMap(_.relationships).map(_.targetConcept).distinct

        inheritingConcepts.map(concept => concept -> hasHypercube)
      }

    conceptHasHypercubes.groupBy(_._1).view.mapValues(_.map(_._2).distinct).toMap
  }

  def computeHasHypercubeInheritanceOrSelf: Map[EName, Seq[HasHypercubeRelationship]] = {
    computeFilteredHasHypercubeInheritanceOrSelf(_ => true)
  }

  def computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]] = {
    computeHasHypercubeInheritanceOrSelf.view.mapValues { hasHypercubes =>
      hasHypercubes.groupBy(_.elr).view.mapValues(_.map(_.sourceConcept).toSet).toMap
    }.toMap
  }

  def computeHasHypercubeInheritance: Map[EName, Seq[HasHypercubeRelationship]] = {
    computeFilteredHasHypercubeInheritance(_ => true)
  }

  def computeHasHypercubeInheritanceReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]] = {
    computeHasHypercubeInheritance.view.mapValues { hasHypercubes =>
      hasHypercubes.groupBy(_.elr).view.mapValues(_.map(_.sourceConcept).toSet).toMap
    }.toMap
  }

  def computeHasHypercubeInheritanceOrSelfForElr(elr: String): Map[EName, Seq[HasHypercubeRelationship]] = {
    computeFilteredHasHypercubeInheritanceOrSelf(_.elr == elr)
  }

  def computeHasHypercubeInheritanceOrSelfForElrReturningPrimaries(elr: String): Map[EName, Set[EName]] = {
    computeHasHypercubeInheritanceOrSelfForElr(elr).view.mapValues { hasHypercubes =>
      hasHypercubes.map(_.sourceConcept).toSet
    }.toMap
  }

  def computeHasHypercubeInheritanceForElr(elr: String): Map[EName, Seq[HasHypercubeRelationship]] = {
    computeFilteredHasHypercubeInheritance(_.elr == elr)
  }

  def computeHasHypercubeInheritanceForElrReturningPrimaries(elr: String): Map[EName, Set[EName]] = {
    computeHasHypercubeInheritanceForElr(elr).view.mapValues { hasHypercubes =>
      hasHypercubes.map(_.sourceConcept).toSet
    }.toMap
  }

  def findAllMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName] = {
    val dimensionDomainPaths =
      filterOutgoingConsecutiveDomainAwareRelationshipPaths(dimension) { path =>
        path.firstRelationship.isInstanceOf[DimensionDomainRelationship] &&
          path.firstRelationship.targetConcept == domain &&
          path.firstRelationship.elr == dimensionDomainElr
      }

    val result = dimensionDomainPaths.flatMap(_.relationships).map(_.targetConcept).toSet
    result
  }

  def findAllUsableMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName] = {
    val dimensionDomainPaths =
      filterOutgoingConsecutiveDomainAwareRelationshipPaths(dimension) { path =>
        path.firstRelationship.isInstanceOf[DimensionDomainRelationship] &&
          path.firstRelationship.targetConcept == domain &&
          path.firstRelationship.elr == dimensionDomainElr
      }

    val potentiallyUsableMembers =
      dimensionDomainPaths.flatMap(_.relationships).filter(_.usable).map(_.targetConcept).toSet

    val potentiallyNonUsableMembers =
      dimensionDomainPaths.flatMap(_.relationships).filterNot(_.usable).map(_.targetConcept).toSet

    potentiallyUsableMembers.diff(potentiallyNonUsableMembers)
  }

  def findAllNonUsableMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName] = {
    findAllMembers(dimension, domain, dimensionDomainElr).diff(findAllUsableMembers(dimension, domain, dimensionDomainElr))
  }

  def findAllMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName] = {
    domainElrPairs.toSeq.flatMap({ case (domain, elr) => findAllMembers(dimension, domain, elr) }).toSet
  }

  def findAllUsableMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName] = {
    val potentiallyUsableMembers =
      domainElrPairs.toSeq.flatMap({ case (domain, elr) => findAllUsableMembers(dimension, domain, elr) }).toSet

    val potentiallyNonUsableMembers =
      domainElrPairs.toSeq.flatMap({ case (domain, elr) => findAllNonUsableMembers(dimension, domain, elr) }).toSet

    potentiallyUsableMembers.diff(potentiallyNonUsableMembers)
  }

  def findAllNonUsableMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName] = {
    findAllMembers(dimension, domainElrPairs).diff(findAllUsableMembers(dimension, domainElrPairs))
  }

  def findAllDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]] = {
    findAllDomainElrPairsPerDimension(hasHypercubeRelationship).map {
      case (dim, domainElrPairs) =>
        dim -> findAllMembers(dim, domainElrPairs)
    }
  }

  def findAllUsableDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]] = {
    findAllDomainElrPairsPerDimension(hasHypercubeRelationship).map {
      case (dim, domainElrPairs) =>
        dim -> findAllUsableMembers(dim, domainElrPairs)
    }
  }

  def findAllNonUsableDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]] = {
    findAllDomainElrPairsPerDimension(hasHypercubeRelationship).map {
      case (dim, domainElrPairs) =>
        dim -> findAllNonUsableMembers(dim, domainElrPairs)
    }
  }

  // Private methods

  private def findAllDomainElrPairsPerDimension(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[(EName, String)]] = {
    val hypercubeDimensionRelationships = findAllConsecutiveHypercubeDimensionRelationships(hasHypercubeRelationship)

    val dimensionDomainRelationships =
      hypercubeDimensionRelationships.flatMap(hd => findAllConsecutiveDimensionDomainRelationships(hd))

    val dimensionDomainRelationshipsByDimension = dimensionDomainRelationships.groupBy(_.dimension)

    dimensionDomainRelationshipsByDimension.view.mapValues(_.map(rel => rel.domain -> rel.elr).toSet).toMap
  }
}
