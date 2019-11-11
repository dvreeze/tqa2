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
 * Purely abstract trait offering a dimensional definition relationship query API.
 *
 * @author Chris de Vreeze
 */
trait DimensionalRelationshipQueryApi {

  // Input strategies, used by relationship path query methods

  /**
   * Strategy used by methods like findAllOutgoingConsecutiveDomainAwareRelationshipPaths to stop appending relationships
   * to relationship paths when desired. Typically this method is used as stop condition when cycles are found.
   * If a cycle is allowed in the path, but it should stop growing beyond that, the second method parameter can be
   * ignored.
   */
  def stopAppending(path: DomainAwareRelationshipPath, next: DomainAwareRelationship): Boolean

  /**
   * Strategy used by methods like findAllIncomingConsecutiveDomainAwareRelationshipPaths to stop prepending relationships
   * to relationship paths when desired. Typically this method is used as stop condition when cycles are found.
   * If a cycle is allowed in the path, but it should stop growing beyond that, the second method parameter can be
   * ignored.
   */
  def stopPrepending(path: DomainAwareRelationshipPath, prev: DomainAwareRelationship): Boolean

  /**
   * Strategy used by methods like findAllOutgoingConsecutiveDomainMemberRelationshipPaths to stop appending relationships
   * to relationship paths when desired. Typically this method is used as stop condition when cycles are found.
   * If a cycle is allowed in the path, but it should stop growing beyond that, the second method parameter can be
   * ignored.
   */
  def stopAppending(path: DomainMemberRelationshipPath, next: DomainMemberRelationship): Boolean

  /**
   * Strategy used by methods like findAllIncomingConsecutiveDomainMemberRelationshipPaths to stop prepending relationships
   * to relationship paths when desired. Typically this method is used as stop condition when cycles are found.
   * If a cycle is allowed in the path, but it should stop growing beyond that, the second method parameter can be
   * ignored.
   */
  def stopPrepending(path: DomainMemberRelationshipPath, prev: DomainMemberRelationship): Boolean

  // Query API methods

  // Finding and filtering relationships without looking at source or target concept

  def findAllDimensionalRelationships: Seq[DimensionalRelationship]

  def filterDimensionalRelationships(
    p: DimensionalRelationship => Boolean): Seq[DimensionalRelationship]

  def findAllDimensionalRelationshipsOfType[A <: DimensionalRelationship](
    relationshipType: ClassTag[A]): Seq[A]

  def filterDimensionalRelationshipsOfType[A <: DimensionalRelationship](
    relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  def findAllHasHypercubeRelationships: Seq[HasHypercubeRelationship]

  def filterHasHypercubeRelationships(
    p: HasHypercubeRelationship => Boolean): Seq[HasHypercubeRelationship]

  def findAllHypercubeDimensionRelationships: Seq[HypercubeDimensionRelationship]

  def filterHypercubeDimensionRelationships(
    p: HypercubeDimensionRelationship => Boolean): Seq[HypercubeDimensionRelationship]

  def findAllDimensionDomainRelationships: Seq[DimensionDomainRelationship]

  def filterDimensionDomainRelationships(
    p: DimensionDomainRelationship => Boolean): Seq[DimensionDomainRelationship]

  def findAllDomainMemberRelationships: Seq[DomainMemberRelationship]

  def filterDomainMemberRelationships(
    p: DomainMemberRelationship => Boolean): Seq[DomainMemberRelationship]

  def findAllDimensionDefaultRelationships: Seq[DimensionDefaultRelationship]

  def filterDimensionDefaultRelationships(
    p: DimensionDefaultRelationship => Boolean): Seq[DimensionDefaultRelationship]

  // Finding and filtering outgoing relationships

  /**
   * Finds all has-hypercube relationships that are outgoing from the given concept.
   */
  def findAllOutgoingHasHypercubeRelationships(
    sourceConcept: EName): Seq[HasHypercubeRelationship]

  /**
   * Filters has-hypercube relationships that are outgoing from the given concept.
   */
  def filterOutgoingHasHypercubeRelationships(
    sourceConcept: EName)(p: HasHypercubeRelationship => Boolean): Seq[HasHypercubeRelationship]

  /**
   * Filters has-hypercube relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingHasHypercubeRelationshipsOnElr(
    sourceConcept: EName, elr: String): Seq[HasHypercubeRelationship]

  /**
   * Finds all hypercube-dimension relationships that are outgoing from the given concept.
   */
  def findAllOutgoingHypercubeDimensionRelationships(
    sourceConcept: EName): Seq[HypercubeDimensionRelationship]

  /**
   * Filters hypercube-dimension relationships that are outgoing from the given concept.
   */
  def filterOutgoingHypercubeDimensionRelationships(
    sourceConcept: EName)(p: HypercubeDimensionRelationship => Boolean): Seq[HypercubeDimensionRelationship]

  /**
   * Filters hypercube-dimension relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingHypercubeDimensionRelationshipsOnElr(
    sourceConcept: EName, elr: String): Seq[HypercubeDimensionRelationship]

  /**
   * Finds all consecutive hypercube-dimension relationships.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingHypercubeDimensionRelationships(relationship.targetConceptEName) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveHypercubeDimensionRelationships(
    relationship: HasHypercubeRelationship): Seq[HypercubeDimensionRelationship]

  /**
   * Finds all dimension-domain relationships that are outgoing from the given concept.
   */
  def findAllOutgoingDimensionDomainRelationships(
    sourceConcept: EName): Seq[DimensionDomainRelationship]

  /**
   * Filters dimension-domain relationships that are outgoing from the given concept.
   */
  def filterOutgoingDimensionDomainRelationships(
    sourceConcept: EName)(p: DimensionDomainRelationship => Boolean): Seq[DimensionDomainRelationship]

  /**
   * Filters dimension-domain relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingDimensionDomainRelationshipsOnElr(
    sourceConcept: EName, elr: String): Seq[DimensionDomainRelationship]

  /**
   * Finds all consecutive dimension-domain relationships.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingDimensionDomainRelationships(relationship.targetConceptEName) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveDimensionDomainRelationships(
    relationship: HypercubeDimensionRelationship): Seq[DimensionDomainRelationship]

  /**
   * Finds all domain-member relationships that are outgoing from the given concept.
   */
  def findAllOutgoingDomainMemberRelationships(
    sourceConcept: EName): Seq[DomainMemberRelationship]

  /**
   * Filters domain-member relationships that are outgoing from the given concept.
   */
  def filterOutgoingDomainMemberRelationships(
    sourceConcept: EName)(p: DomainMemberRelationship => Boolean): Seq[DomainMemberRelationship]

  /**
   * Filters domain-member relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingDomainMemberRelationshipsOnElr(
    sourceConcept: EName, elr: String): Seq[DomainMemberRelationship]

  /**
   * Finds all consecutive domain-member relationships.
   *
   * This method is shorthand for:
   * {{{
   * filterOutgoingDomainMemberRelationships(relationship.targetConceptEName) { rel =>
   *   relationship.isFollowedBy(rel)
   * }
   * }}}
   */
  def findAllConsecutiveDomainMemberRelationships(
    relationship: DomainAwareRelationship): Seq[DomainMemberRelationship]

  /**
   * Finds all dimension-default relationships that are outgoing from the given concept.
   */
  def findAllOutgoingDimensionDefaultRelationships(
    sourceConcept: EName): Seq[DimensionDefaultRelationship]

  /**
   * Filters dimension-default relationships that are outgoing from the given concept.
   */
  def filterOutgoingDimensionDefaultRelationships(
    sourceConcept: EName)(p: DimensionDefaultRelationship => Boolean): Seq[DimensionDefaultRelationship]

  /**
   * Filters dimension-default relationships that are outgoing from the given concept on the given ELR.
   */
  def filterOutgoingDimensionDefaultRelationshipsOnElr(
    sourceConcept: EName, elr: String): Seq[DimensionDefaultRelationship]

  // Finding and filtering incoming relationships

  /**
   * Finds all domain-member relationships that are incoming to the given concept.
   */
  def findAllIncomingDomainMemberRelationships(
    targetConcept: EName): Seq[DomainMemberRelationship]

  /**
   * Filters domain-member relationships that are incoming to the given concept.
   */
  def filterIncomingDomainMemberRelationships(
    targetConcept: EName)(p: DomainMemberRelationship => Boolean): Seq[DomainMemberRelationship]

  /**
   * Finds all "domain-aware" relationships that are incoming to the given concept.
   */
  def findAllIncomingDomainAwareRelationships(
    targetConcept: EName): Seq[DomainAwareRelationship]

  /**
   * Filters "domain-aware" relationships that are incoming to the given concept.
   */
  def filterIncomingDomainAwareRelationships(
    targetConcept: EName)(p: DomainAwareRelationship => Boolean): Seq[DomainAwareRelationship]

  /**
   * Finds all hypercube-dimension relationships that are incoming to the given (dimension) concept.
   */
  def findAllIncomingHypercubeDimensionRelationships(
    targetConcept: EName): Seq[HypercubeDimensionRelationship]

  /**
   * Filters hypercube-dimension relationships that are incoming to the given (dimension) concept.
   */
  def filterIncomingHypercubeDimensionRelationships(
    targetConcept: EName)(p: HypercubeDimensionRelationship => Boolean): Seq[HypercubeDimensionRelationship]

  /**
   * Finds all has-hypercube relationships that are incoming to the given (hypercube) concept.
   */
  def findAllIncomingHasHypercubeRelationships(
    targetConcept: EName): Seq[HasHypercubeRelationship]

  /**
   * Filters has-hypercube relationships that are incoming to the given (hypercube) concept.
   */
  def filterIncomingHasHypercubeRelationships(
    targetConcept: EName)(p: HasHypercubeRelationship => Boolean): Seq[HasHypercubeRelationship]

  // Filtering outgoing and incoming relationship paths

  /**
   * Returns `filterOutgoingConsecutiveDomainAwareRelationshipPaths(sourceConcept)(_ => true)`.
   */
  def findAllOutgoingConsecutiveDomainAwareRelationshipPaths(
    sourceConcept: EName): Seq[DomainAwareRelationshipPath]

  /**
   * Filters the consecutive (!) dimension-domain-or-domain-member relationship paths that are outgoing from the given concept.
   * Only relationship paths for which all (non-empty) "inits" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on method stopAppending returning true it stops growing.
   */
  def filterOutgoingConsecutiveDomainAwareRelationshipPaths(
    sourceConcept: EName)(
    p: DomainAwareRelationshipPath => Boolean): Seq[DomainAwareRelationshipPath]

  /**
   * Returns `filterOutgoingConsecutiveDomainMemberRelationshipPaths(sourceConcept)(_ => true)`.
   */
  def findAllOutgoingConsecutiveDomainMemberRelationshipPaths(
    sourceConcept: EName): Seq[DomainMemberRelationshipPath]

  /**
   * Filters the consecutive (!) domain-member relationship paths that are outgoing from the given concept.
   * Only relationship paths for which all (non-empty) "inits" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on method stopAppending returning true it stops growing.
   */
  def filterOutgoingConsecutiveDomainMemberRelationshipPaths(
    sourceConcept: EName)(
    p: DomainMemberRelationshipPath => Boolean): Seq[DomainMemberRelationshipPath]

  /**
   * Returns `filterIncomingConsecutiveDomainAwareRelationshipPaths(targetConcept)(_ => true)`.
   */
  def findAllIncomingConsecutiveDomainAwareRelationshipPaths(
    targetConcept: EName): Seq[DomainAwareRelationshipPath]

  /**
   * Filters the consecutive (!) dimension-domain-or-domain-member relationship paths that are incoming to the given concept.
   * Only relationship paths for which all (non-empty) "tails" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on method stopPrepending returning true it stops growing.
   */
  def filterIncomingConsecutiveDomainAwareRelationshipPaths(
    targetConcept: EName)(p: DomainAwareRelationshipPath => Boolean): Seq[DomainAwareRelationshipPath]

  /**
   * Returns `filterIncomingConsecutiveDomainMemberRelationshipPaths(targetConcept)(_ => true)`.
   */
  def findAllIncomingConsecutiveDomainMemberRelationshipPaths(
    targetConcept: EName): Seq[DomainMemberRelationshipPath]

  /**
   * Filters the consecutive (!) domain-member relationship paths that are incoming to the given concept.
   * Only relationship paths for which all (non-empty) "tails" pass the predicate are accepted by the filter!
   * The relationship paths are as long as possible, but on method stopPrepending returning true it stops growing.
   */
  def filterIncomingConsecutiveDomainMemberRelationshipPaths(
    targetConcept: EName)(p: DomainMemberRelationshipPath => Boolean): Seq[DomainMemberRelationshipPath]

  // Other query methods

  /**
   * Finds all own or inherited has-hypercubes. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllOwnOrInheritedHasHypercubes(concept: EName): Seq[HasHypercubeRelationship]

  /**
   * Finds all own or inherited has-hypercubes as a Map from ELRs to all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]]

  /**
   * Finds all inherited has-hypercubes. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllInheritedHasHypercubes(concept: EName): Seq[HasHypercubeRelationship]

  /**
   * Finds all inherited has-hypercubes as a Map from ELRs to all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   */
  def findAllInheritedHasHypercubesAsElrToPrimariesMap(concept: EName): Map[String, Set[EName]]

  /**
   * Finds all own or inherited has-hypercubes per concept that pass the predicate.
   * See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is potentially an expensive bulk version of method findAllOwnOrInheritedHasHypercubes, and
   * should typically be called as few times as possible.
   */
  def computeFilteredHasHypercubeInheritanceOrSelf(
    p: HasHypercubeRelationship => Boolean): Map[EName, Seq[HasHypercubeRelationship]]

  /**
   * Finds all inherited has-hypercubes per concept that pass the predicate.
   * See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is potentially an expensive bulk version of method findAllInheritedHasHypercubes, and
   * should typically be called as few times as possible.
   */
  def computeFilteredHasHypercubeInheritance(
    p: HasHypercubeRelationship => Boolean): Map[EName, Seq[HasHypercubeRelationship]]

  /**
   * Finds all own or inherited has-hypercubes per concept. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is an expensive bulk version of method findAllOwnOrInheritedHasHypercubes, and should be called
   * as few times as possible.
   *
   * This function is equivalent to:
   * {{{
   * computeFilteredHasHypercubeInheritanceOrSelf(_ => true)
   * }}}
   */
  def computeHasHypercubeInheritanceOrSelf: Map[EName, Seq[HasHypercubeRelationship]]

  /**
   * Finds all own or inherited has-hypercubes per concept returning Maps from ELRs to all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is an expensive bulk version of method findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap, and should be called
   * as few times as possible.
   */
  def computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]]

  /**
   * Finds all inherited has-hypercubes per concept. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is an expensive bulk version of method findAllInheritedHasHypercubes, and should be called
   * as few times as possible.
   *
   * This function is equivalent to:
   * {{{
   * computeFilteredHasHypercubeInheritance(_ => true)
   * }}}
   */
  def computeHasHypercubeInheritance: Map[EName, Seq[HasHypercubeRelationship]]

  /**
   * Finds all inherited has-hypercubes per concept returning Maps from ELRs to all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is an expensive bulk version of method findAllInheritedHasHypercubesAsElrToPrimariesMap, and should be called
   * as few times as possible.
   */
  def computeHasHypercubeInheritanceReturningElrToPrimariesMaps: Map[EName, Map[String, Set[EName]]]

  /**
   * Finds all own or inherited has-hypercubes per concept for the given ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is a rather expensive bulk version of method findAllOwnOrInheritedHasHypercubes, and should be called
   * as few times as possible.
   *
   * This function is equivalent to:
   * {{{
   * computeFilteredHasHypercubeInheritanceOrSelf(_.elr == elr)
   * }}}
   */
  def computeHasHypercubeInheritanceOrSelfForElr(elr: String): Map[EName, Seq[HasHypercubeRelationship]]

  /**
   * Finds all own or inherited has-hypercubes per concept, for the given ELR, returning Sets of all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is a rather expensive bulk version of method findAllOwnOrInheritedHasHypercubesAsElrToPrimariesMap, and should be called
   * as few times as possible.
   */
  def computeHasHypercubeInheritanceOrSelfForElrReturningPrimaries(elr: String): Map[EName, Set[EName]]

  /**
   * Finds all inherited has-hypercubes per concept for the given ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is a rather expensive bulk version of method findAllInheritedHasHypercubes, and should be called
   * as few times as possible.
   *
   * This function is equivalent to:
   * {{{
   * computeFilteredHasHypercubeInheritance(_.elr == elr)
   * }}}
   */
  def computeHasHypercubeInheritanceForElr(elr: String): Map[EName, Seq[HasHypercubeRelationship]]

  /**
   * Finds all inherited has-hypercubes per concept, for the given ELR, returning Sets of all primaries that are source concepts
   * of the has-hypercube relationships with that ELR. See section 2.6.1 of the XBRL Dimensions specification.
   *
   * This is a rather expensive bulk version of method findAllInheritedHasHypercubesAsElrToPrimariesMap, and should be called
   * as few times as possible.
   */
  def computeHasHypercubeInheritanceForElrReturningPrimaries(elr: String): Map[EName, Set[EName]]

  /**
   * Finds all members in the given dimension-domain. There should be at most one dimension-domain
   * relationship from the given dimension to the given domain, having the given ELR.
   */
  def findAllMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName]

  /**
   * Finds all usable members in the given dimension-domain. There should be at most one dimension-domain
   * relationship from the given dimension to the given domain, having the given ELR.
   */
  def findAllUsableMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName]

  /**
   * Finds all non-usable members in the given dimension-domain. There should be at most one dimension-domain
   * relationship from the given dimension to the given domain, having the given ELR.
   */
  def findAllNonUsableMembers(dimension: EName, domain: EName, dimensionDomainElr: String): Set[EName]

  /**
   * Finds all members in the given effective domain of the given dimension.
   */
  def findAllMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName]

  /**
   * Finds all usable members in the given effective domain of the given dimension. If a member is
   * usable in one dimension-domain but not usable in another one, it is considered not usable.
   */
  def findAllUsableMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName]

  /**
   * Finds all non-usable members in the given effective domain of the given dimension. If a member is
   * usable in one dimension-domain but not usable in another one, it is considered not usable.
   */
  def findAllNonUsableMembers(dimension: EName, domainElrPairs: Set[(EName, String)]): Set[EName]

  /**
   * Finds all (explicit) dimension members for the given has-hypercube relationship.
   */
  def findAllDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]]

  /**
   * Finds all usable (explicit) dimension members for the given has-hypercube relationship.
   */
  def findAllUsableDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]]

  /**
   * Finds all non-usable (explicit) dimension members for the given has-hypercube relationship.
   */
  def findAllNonUsableDimensionMembers(hasHypercubeRelationship: HasHypercubeRelationship): Map[EName, Set[EName]]
}
