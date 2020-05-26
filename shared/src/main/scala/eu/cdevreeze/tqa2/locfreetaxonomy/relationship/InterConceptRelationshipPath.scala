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

package eu.cdevreeze.tqa2.locfreetaxonomy.relationship

import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Endpoint.ElementKeyEndpoint
import eu.cdevreeze.yaidom2.core.EName

/**
 * Inter-concept relationship path. Subsequent relationships in the path must match in target and
 * source concept, respectively. It is not required that the arc role remains the same, or that
 * targetRole attributes are followed, although in practice this will be the case. A relationship path
 * must have at least one relationship.
 *
 * In practice, inter-concept relationship paths are "ELR-valid", that is, their relationships are in the same ELR,
 * or, in the case of dimensional relationship paths, their relationships are consecutive relationships.
 *
 * @author Chris de Vreeze
 */
final case class InterConceptRelationshipPath[A <: InterConceptRelationship] private (relationships: Seq[A]) extends RelationshipPath {

  override type RelationshipType = A

  override type SelfType = InterConceptRelationshipPath[A]

  require(relationships.nonEmpty, s"A relationship path must have at least one relationship")

  def source: ElementKeyEndpoint = firstRelationship.source

  def target: ElementKeyEndpoint = lastRelationship.target

  def sourceConcept: EName = firstRelationship.sourceConcept

  def targetConcept: EName = lastRelationship.targetConcept

  def firstRelationship: A = relationships.head

  def lastRelationship: A = relationships.last

  def elements: Seq[Endpoint] = {
    relationships.map(_.source) :+ relationships.last.target
  }

  def concepts: Seq[EName] = {
    relationships.map(_.sourceConcept) :+ relationships.last.targetConcept
  }

  def relationshipTargets: Seq[ElementKeyEndpoint] = {
    relationships.map(_.target)
  }

  def relationshipTargetConcepts: Seq[EName] = {
    relationships.map(_.targetConcept)
  }

  def hasCycle: Boolean = {
    val concepts = relationships.map(_.sourceConcept) :+ relationships.last.targetConcept
    concepts.distinct.size < concepts.size
  }

  def isMinimalIfHavingCycle: Boolean = {
    initOption.forall(p => !p.hasCycle)
  }

  def append(relationship: A): InterConceptRelationshipPath[A] = {
    require(canAppend(relationship), s"Could not append relationship $relationship")
    new InterConceptRelationshipPath(relationships :+ relationship)
  }

  def prepend(relationship: A): InterConceptRelationshipPath[A] = {
    require(canPrepend(relationship), s"Could not prepend relationship $relationship")
    new InterConceptRelationshipPath(relationship +: relationships)
  }

  def canAppend(relationship: A): Boolean = {
    this.targetConcept == relationship.sourceConcept
  }

  def canPrepend(relationship: A): Boolean = {
    this.sourceConcept == relationship.targetConcept
  }

  def inits: Seq[InterConceptRelationshipPath[A]] = {
    relationships.inits.filter(_.nonEmpty).toVector.map(rels => new InterConceptRelationshipPath[A](rels))
  }

  def tails: Seq[InterConceptRelationshipPath[A]] = {
    relationships.tails.filter(_.nonEmpty).toVector.map(rels => new InterConceptRelationshipPath[A](rels))
  }

  def initOption: Option[InterConceptRelationshipPath[A]] = {
    if (relationships.size == 1) {
      None
    } else {
      assert(relationships.size >= 2)
      Some(new InterConceptRelationshipPath(relationships.init))
    }
  }

  /**
   * Returns true if all subsequent relationships in the path "follow each other".
   * For dimensional relationships this means that they are consecutive relationships.
   */
  def isConsecutiveRelationshipPath: Boolean = {
    relationships.sliding(2).filter(_.size == 2).forall(pair => pair(0).isFollowedBy(pair(1)))
  }
}

object InterConceptRelationshipPath {

  def apply[A <: InterConceptRelationship](relationship: A): InterConceptRelationshipPath[A] = {
    new InterConceptRelationshipPath(Vector(relationship))
  }

  def from[A <: InterConceptRelationship](relationships: Seq[A]): InterConceptRelationshipPath[A] = {
    require(
      relationships.sliding(2).filter(_.size == 2).forall(pair => haveMatchingConcepts(pair(0), pair(1))),
      s"All subsequent relationships in a path must have matching target/source concepts"
    )

    new InterConceptRelationshipPath(relationships.toVector)
  }

  private def haveMatchingConcepts[A <: InterConceptRelationship](relationship1: A, relationship2: A): Boolean = {
    relationship1.targetConcept == relationship2.sourceConcept
  }
}
