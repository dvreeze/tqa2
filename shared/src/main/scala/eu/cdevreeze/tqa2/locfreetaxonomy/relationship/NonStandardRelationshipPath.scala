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

/**
 * Non-standard relationship path. Subsequent relationships in the path must match in target and
 * source XML element (key), respectively. It is not required that the arc role remains the same, or that
 * ELRs remain the same, although in practice the latter will be the case. A relationship path
 * must have at least one relationship.
 *
 * In practice, non-standard relationship paths are "ELR-valid", that is, their relationships are in the same ELR.
 *
 * @author Chris de Vreeze
 */
final case class NonStandardRelationshipPath[A <: NonStandardRelationship] private (relationships: Seq[A]) {
  require(relationships.nonEmpty, s"A relationship path must have at least one relationship")

  def sourceKey: Endpoint = firstRelationship.source // TODO Rename

  def targetKey: Endpoint = lastRelationship.target // TODO Rename

  def firstRelationship: A = relationships.head

  def lastRelationship: A = relationships.last

  def elementKeys: Seq[Endpoint] = {
    relationships.map(_.source) :+ relationships.last.target
  }

  def relationshipTargetElementKeys: Seq[Endpoint] = {
    relationships.map(_.target)
  }

  def hasCycle: Boolean = {
    val keys = relationships.map(_.source) :+ relationships.last.target
    keys.distinct.size < elementKeys.size
  }

  def isMinimalIfHavingCycle: Boolean = {
    initOption.forall(p => !p.hasCycle)
  }

  def append(relationship: A): NonStandardRelationshipPath[A] = {
    require(canAppend(relationship), s"Could not append relationship $relationship")
    new NonStandardRelationshipPath(relationships :+ relationship)
  }

  def prepend(relationship: A): NonStandardRelationshipPath[A] = {
    require(canPrepend(relationship), s"Could not prepend relationship $relationship")
    new NonStandardRelationshipPath(relationship +: relationships)
  }

  def canAppend(relationship: A): Boolean = {
    this.targetKey == relationship.source
  }

  def canPrepend(relationship: A): Boolean = {
    this.sourceKey == relationship.target
  }

  def inits: Seq[NonStandardRelationshipPath[A]] = {
    relationships.inits.filter(_.nonEmpty).toVector.map(rels => new NonStandardRelationshipPath[A](rels))
  }

  def tails: Seq[NonStandardRelationshipPath[A]] = {
    relationships.tails.filter(_.nonEmpty).toVector.map(rels => new NonStandardRelationshipPath[A](rels))
  }

  def initOption: Option[NonStandardRelationshipPath[A]] = {
    if (relationships.size == 1) {
      None
    } else {
      assert(relationships.size >= 2)
      Some(new NonStandardRelationshipPath(relationships.init))
    }
  }

  /**
   * Returns true if all subsequent relationships in the path are in the same ELR.
   */
  def isSingleElrRelationshipPath: Boolean = {
    relationships.sliding(2).filter(_.size == 2).forall(pair => pair(0).elr == pair(1).elr)
  }
}

object NonStandardRelationshipPath {

  def apply[A <: NonStandardRelationship](relationship: A): NonStandardRelationshipPath[A] = {
    new NonStandardRelationshipPath(Vector(relationship))
  }

  def from[A <: NonStandardRelationship](relationships: Seq[A]): NonStandardRelationshipPath[A] = {
    require(
      relationships.sliding(2).filter(_.size == 2).forall(pair => haveMatchingElementKeys(pair(0), pair(1))),
      s"All subsequent relationships in a path must have matching target/source elements")

    new NonStandardRelationshipPath(relationships.toVector)
  }

  private def haveMatchingElementKeys[A <: NonStandardRelationship](relationship1: A, relationship2: A): Boolean = {
    relationship1.target == relationship2.source
  }
}
