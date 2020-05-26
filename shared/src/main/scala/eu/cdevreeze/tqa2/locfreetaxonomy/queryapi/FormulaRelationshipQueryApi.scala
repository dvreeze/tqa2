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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Assertion
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.FactVariable
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.VariableSet
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship._

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

  // Specialized query API methods

  // Variable-set relationships

  def findAllVariableSetRelationships: Seq[VariableSetRelationship]

  def filterVariableSetRelationships(p: VariableSetRelationship => Boolean): Seq[VariableSetRelationship]

  /**
   * Finds all variable-set relationships that are outgoing from the given VariableSet.
   */
  def findAllOutgoingVariableSetRelationships(variableSet: VariableSet): Seq[VariableSetRelationship]

  /**
   * Filters variable-set relationships that are outgoing from the given VariableSet.
   */
  def filterOutgoingVariableSetRelationships(variableSet: VariableSet)(p: VariableSetRelationship => Boolean): Seq[VariableSetRelationship]

  // Variable-filter relationships

  def findAllVariableFilterRelationships: Seq[VariableFilterRelationship]

  def filterVariableFilterRelationships(p: VariableFilterRelationship => Boolean): Seq[VariableFilterRelationship]

  /**
   * Finds all variable-filter relationships that are outgoing from the given FactVariable.
   */
  def findAllOutgoingVariableFilterRelationships(factVariable: FactVariable): Seq[VariableFilterRelationship]

  /**
   * Filters variable-filter relationships that are outgoing from the given FactVariable.
   */
  def filterOutgoingVariableFilterRelationships(factVariable: FactVariable)(
      p: VariableFilterRelationship => Boolean): Seq[VariableFilterRelationship]

  // Variable-set-filter relationships

  def findAllVariableSetFilterRelationships: Seq[VariableSetFilterRelationship]

  def filterVariableSetFilterRelationships(p: VariableSetFilterRelationship => Boolean): Seq[VariableSetFilterRelationship]

  /**
   * Finds all variable-set-filter relationships that are outgoing from the given VariableSet.
   */
  def findAllOutgoingVariableSetFilterRelationships(variableSet: VariableSet): Seq[VariableSetFilterRelationship]

  /**
   * Filters variable-set-filter relationships that are outgoing from the given VariableSet.
   */
  def filterOutgoingVariableSetFilterRelationships(variableSet: VariableSet)(
      p: VariableSetFilterRelationship => Boolean): Seq[VariableSetFilterRelationship]

  // Variable-set-precondition relationships

  def findAllVariableSetPreconditionRelationships: Seq[VariableSetPreconditionRelationship]

  def filterVariableSetPreconditionRelationships(
      p: VariableSetPreconditionRelationship => Boolean): Seq[VariableSetPreconditionRelationship]

  /**
   * Finds all variable-set-precondition relationships that are outgoing from the given VariableSet.
   */
  def findAllOutgoingVariableSetPreconditionRelationships(variableSet: VariableSet): Seq[VariableSetPreconditionRelationship]

  /**
   * Filters variable-set-precondition relationships that are outgoing from the given VariableSet.
   */
  def filterOutgoingVariableSetPreconditionRelationships(variableSet: VariableSet)(
      p: VariableSetPreconditionRelationship => Boolean): Seq[VariableSetPreconditionRelationship]

  // Assertion message relationships. Note that these relationships are strictly not formula-related relationships.

  def findAllAssertionMessageRelationships: Seq[AssertionMessageRelationship]

  def filterAssertionMessageRelationships(p: AssertionMessageRelationship => Boolean): Seq[AssertionMessageRelationship]

  /**
   * Finds all assertion-message relationships that are outgoing from the given Assertion.
   */
  def findAllOutgoingAssertionMessageRelationships(assertion: Assertion): Seq[AssertionMessageRelationship]

  /**
   * Filters assertion-message relationships that are outgoing from the given Assertion.
   */
  def filterOutgoingAssertionMessageRelationships(assertion: Assertion)(
      p: AssertionMessageRelationship => Boolean): Seq[AssertionMessageRelationship]
}
