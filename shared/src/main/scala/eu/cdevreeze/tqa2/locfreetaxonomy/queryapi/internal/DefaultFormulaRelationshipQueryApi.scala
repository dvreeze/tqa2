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

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.TaxonomyElemKey
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.FormulaRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.NonStandardRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.FormulaRelationship

import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * Implementation of FormulaRelationshipQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * The methods that query for outgoing or incoming relationships, given a source/target element keu, must be fast.
 *
 * @author Chris de Vreeze
 */
trait DefaultFormulaRelationshipQueryApi extends FormulaRelationshipQueryApi with NonStandardRelationshipQueryApi {

  // Query API methods

  def findAllFormulaRelationships: Seq[FormulaRelationship] = {
    findAllNonStandardRelationshipsOfType(classTag[FormulaRelationship])
  }

  def filterFormulaRelationships(p: FormulaRelationship => Boolean): Seq[FormulaRelationship] = {
    filterNonStandardRelationshipsOfType(classTag[FormulaRelationship])(p)
  }

  def findAllFormulaRelationshipsOfType[A <: FormulaRelationship](relationshipType: ClassTag[A]): Seq[A] = {
    findAllNonStandardRelationshipsOfType(relationshipType)
  }

  def filterFormulaRelationshipsOfType[A <: FormulaRelationship](relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {
    filterNonStandardRelationshipsOfType(relationshipType)(p)
  }

  def findAllOutgoingFormulaRelationships(sourceKey: TaxonomyElemKey): Seq[FormulaRelationship] = {
    findAllOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[FormulaRelationship])
  }

  def filterOutgoingFormulaRelationships(sourceKey: TaxonomyElemKey)(p: FormulaRelationship => Boolean): Seq[FormulaRelationship] = {
    filterOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[FormulaRelationship])(p)
  }

  def findAllOutgoingFormulaRelationshipsOfType[A <: FormulaRelationship](
      sourceKey: TaxonomyElemKey,
      relationshipType: ClassTag[A]): Seq[A] = {
    findAllOutgoingNonStandardRelationshipsOfType(sourceKey, relationshipType)
  }

  def filterOutgoingFormulaRelationshipsOfType[A <: FormulaRelationship](sourceKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A] = {
    filterOutgoingNonStandardRelationshipsOfType(sourceKey, relationshipType)(p)
  }

  def findAllIncomingFormulaRelationships(targetKey: TaxonomyElemKey): Seq[FormulaRelationship] = {
    findAllIncomingNonStandardRelationshipsOfType(targetKey, classTag[FormulaRelationship])
  }

  def filterIncomingFormulaRelationships(targetKey: TaxonomyElemKey)(p: FormulaRelationship => Boolean): Seq[FormulaRelationship] = {
    filterIncomingNonStandardRelationshipsOfType(targetKey, classTag[FormulaRelationship])(p)
  }

  def findAllIncomingFormulaRelationshipsOfType[A <: FormulaRelationship](
      targetKey: TaxonomyElemKey,
      relationshipType: ClassTag[A]): Seq[A] = {
    findAllIncomingNonStandardRelationshipsOfType(targetKey, relationshipType)
  }

  def filterIncomingFormulaRelationshipsOfType[A <: FormulaRelationship](targetKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A] = {
    filterIncomingNonStandardRelationshipsOfType(targetKey, relationshipType)(p)
  }
}
