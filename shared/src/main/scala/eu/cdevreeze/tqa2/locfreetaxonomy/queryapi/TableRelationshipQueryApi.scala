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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.DefinitionNode
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Table
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TableBreakdown
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship._

import scala.reflect.ClassTag

/**
 * Purely abstract trait offering a table-related relationship query API.
 *
 * @author Chris de Vreeze
 */
trait TableRelationshipQueryApi {

  // Query API methods

  def findAllTableRelationships: Seq[TableRelationship]

  def filterTableRelationships(p: TableRelationship => Boolean): Seq[TableRelationship]

  def findAllTableRelationshipsOfType[A <: TableRelationship](relationshipType: ClassTag[A]): Seq[A]

  def filterTableRelationshipsOfType[A <: TableRelationship](relationshipType: ClassTag[A])(p: A => Boolean): Seq[A]

  /**
   * Finds all table-related relationships that are outgoing from the given XML element.
   */
  def findAllOutgoingTableRelationships(sourceKey: TaxonomyElemKey): Seq[TableRelationship]

  /**
   * Filters table-related relationships that are outgoing from the given XML element.
   */
  def filterOutgoingTableRelationships(sourceKey: TaxonomyElemKey)(p: TableRelationship => Boolean): Seq[TableRelationship]

  /**
   * Finds all table-related relationships of the given type that are outgoing from the given XML element.
   */
  def findAllOutgoingTableRelationshipsOfType[A <: TableRelationship](sourceKey: TaxonomyElemKey, relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters table-related relationships of the given type that are outgoing from the given XML element.
   */
  def filterOutgoingTableRelationshipsOfType[A <: TableRelationship](sourceKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A]

  /**
   * Finds all table-related relationships that are incoming to the given XML element.
   */
  def findAllIncomingTableRelationships(targetKey: TaxonomyElemKey): Seq[TableRelationship]

  /**
   * Filters table-related relationships that are incoming to the given XML element.
   */
  def filterIncomingTableRelationships(targetKey: TaxonomyElemKey)(p: TableRelationship => Boolean): Seq[TableRelationship]

  /**
   * Finds all table-related relationships of the given type that are incoming to the given XML element.
   */
  def findAllIncomingTableRelationshipsOfType[A <: TableRelationship](targetKey: TaxonomyElemKey, relationshipType: ClassTag[A]): Seq[A]

  /**
   * Filters table-related relationships of the given type that are incoming to the given XML element.
   */
  def filterIncomingTableRelationshipsOfType[A <: TableRelationship](targetKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A]

  // Specialized query API methods

  def findAllTableBreakdownRelationships: Seq[TableBreakdownRelationship]

  def filterTableBreakdownRelationships(p: TableBreakdownRelationship => Boolean): Seq[TableBreakdownRelationship]

  /**
   * Finds all table-breakdown relationships that are outgoing from the given table.
   */
  def findAllOutgoingTableBreakdownRelationships(table: Table): Seq[TableBreakdownRelationship]

  /**
   * Filters table-breakdown relationships that are outgoing from the given table.
   */
  def filterOutgoingTableBreakdownRelationships(table: Table)(p: TableBreakdownRelationship => Boolean): Seq[TableBreakdownRelationship]

  /**
   * Finds all breakdown-tree relationships that are outgoing from the given breakdown.
   */
  def findAllOutgoingBreakdownTreeRelationships(breakdown: TableBreakdown): Seq[BreakdownTreeRelationship]

  /**
   * Filters breakdown-tree relationships that are outgoing from the given breakdown.
   */
  def filterOutgoingBreakdownTreeRelationships(breakdown: TableBreakdown)(
      p: BreakdownTreeRelationship => Boolean): Seq[BreakdownTreeRelationship]

  /**
   * Finds all definition-node-subtree relationships that are outgoing from the given node.
   */
  def findAllOutgoingDefinitionNodeSubtreeRelationships(node: DefinitionNode): Seq[DefinitionNodeSubtreeRelationship]

  /**
   * Filters definition-node-subtree relationships that are outgoing from the given node.
   */
  def filterOutgoingDefinitionNodeSubtreeRelationships(node: DefinitionNode)(
      p: DefinitionNodeSubtreeRelationship => Boolean): Seq[DefinitionNodeSubtreeRelationship]

  /**
   * Finds all table-filter relationships that are outgoing from the given table.
   */
  def findAllOutgoingTableFilterRelationships(table: Table): Seq[TableFilterRelationship]

  /**
   * Filters table-filter relationships that are outgoing from the given table.
   */
  def filterOutgoingTableFilterRelationships(table: Table)(p: TableFilterRelationship => Boolean): Seq[TableFilterRelationship]

  /**
   * Finds all table-parameter relationships that are outgoing from the given table.
   */
  def findAllOutgoingTableParameterRelationships(table: Table): Seq[TableParameterRelationship]

  /**
   * Filters table-parameter relationships that are outgoing from the given table.
   */
  def filterOutgoingTableParameterRelationships(table: Table)(p: TableParameterRelationship => Boolean): Seq[TableParameterRelationship]
}
