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

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.TaxonomyElemKey
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.FormulaRelationship

import scala.reflect.ClassTag

/**
 * Purely abstract trait offering a formula-related relationship query API.
 *
 * @author Chris de Vreeze
 */
trait FormulaRelationshipQueryApi {

  // Query API methods

  def findAllFormulaRelationships: Seq[FormulaRelationship]

  def filterFormulaRelationships(p: FormulaRelationship => Boolean): Seq[FormulaRelationship]

  def findAllFormulaRelationshipsOfType[A <: FormulaRelationship](relationshipType: ClassTag[A]): Seq[A]

  def filterFormulaRelationshipsOfType[A <: FormulaRelationship](relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Finds all formula-related relationships that are outgoing from the given XML element.
   */
  def findAllOutgoingFormulaRelationships(sourceKey: TaxonomyElemKey): Seq[FormulaRelationship]

  /**
   * Filters formula-related relationships that are outgoing from the given XML element.
   */
  def filterOutgoingFormulaRelationships(sourceKey: TaxonomyElemKey)(p: FormulaRelationship => Boolean): Seq[FormulaRelationship]

  /**
   * Finds all formula-related relationships of the given type that are outgoing from the given XML element.
   */
  def findAllOutgoingFormulaRelationshipsOfType[A <: FormulaRelationship](sourceKey: TaxonomyElemKey, relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters formula-related relationships of the given type that are outgoing from the given XML element.
   */
  def filterOutgoingFormulaRelationshipsOfType[A <: FormulaRelationship](sourceKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A]

  /**
   * Finds all formula-related relationships that are incoming to the given XML element.
   */
  def findAllIncomingFormulaRelationships(targetKey: TaxonomyElemKey): Seq[FormulaRelationship]

  /**
   * Filters formula-related relationships that are incoming to the given XML element.
   */
  def filterIncomingFormulaRelationships(targetKey: TaxonomyElemKey)(p: FormulaRelationship => Boolean): Seq[FormulaRelationship]

  /**
   * Finds all formula-related relationships of the given type that are incoming to the given XML element.
   */
  def findAllIncomingFormulaRelationshipsOfType[A <: FormulaRelationship](targetKey: TaxonomyElemKey, relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters formula-related relationships of the given type that are incoming to the given XML element.
   */
  def filterIncomingFormulaRelationshipsOfType[A <: FormulaRelationship](targetKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A]
}
