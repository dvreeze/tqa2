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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.DefinitionNode
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Table
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TableBreakdown
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.NonStandardRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.TableRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship._

import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * Implementation of TableRelationshipQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * The methods that query for outgoing or incoming relationships, given a source/target element key, must be fast.
 *
 * @author Chris de Vreeze
 */
trait DefaultTableRelationshipQueryApi extends TableRelationshipQueryApi with NonStandardRelationshipQueryApi {

  // Query API methods

  def findAllTableRelationships: Seq[TableRelationship] = {
    findAllNonStandardRelationshipsOfType(classTag[TableRelationship])
  }

  def filterTableRelationships(p: TableRelationship => Boolean): Seq[TableRelationship] = {
    filterNonStandardRelationshipsOfType(classTag[TableRelationship])(p)
  }

  def findAllTableRelationshipsOfType[A <: TableRelationship](relationshipType: ClassTag[A]): Seq[A] = {
    findAllNonStandardRelationshipsOfType(relationshipType)
  }

  def filterTableRelationshipsOfType[A <: TableRelationship](relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {
    filterNonStandardRelationshipsOfType(relationshipType)(p)
  }

  def findAllOutgoingTableRelationships(sourceKey: TaxonomyElemKey): Seq[TableRelationship] = {
    findAllOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[TableRelationship])
  }

  def filterOutgoingTableRelationships(sourceKey: TaxonomyElemKey)(p: TableRelationship => Boolean): Seq[TableRelationship] = {
    filterOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[TableRelationship])(p)
  }

  def findAllOutgoingTableRelationshipsOfType[A <: TableRelationship](sourceKey: TaxonomyElemKey, relationshipType: ClassTag[A]): Seq[A] = {
    findAllOutgoingNonStandardRelationshipsOfType(sourceKey, relationshipType)
  }

  def filterOutgoingTableRelationshipsOfType[A <: TableRelationship](sourceKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A] = {
    filterOutgoingNonStandardRelationshipsOfType(sourceKey, relationshipType)(p)
  }

  def findAllIncomingTableRelationships(targetKey: TaxonomyElemKey): Seq[TableRelationship] = {
    findAllIncomingNonStandardRelationshipsOfType(targetKey, classTag[TableRelationship])
  }

  def filterIncomingTableRelationships(targetKey: TaxonomyElemKey)(p: TableRelationship => Boolean): Seq[TableRelationship] = {
    filterIncomingNonStandardRelationshipsOfType(targetKey, classTag[TableRelationship])(p)
  }

  def findAllIncomingTableRelationshipsOfType[A <: TableRelationship](targetKey: TaxonomyElemKey, relationshipType: ClassTag[A]): Seq[A] = {
    findAllIncomingNonStandardRelationshipsOfType(targetKey, relationshipType)
  }

  def filterIncomingTableRelationshipsOfType[A <: TableRelationship](targetKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A] = {
    filterIncomingNonStandardRelationshipsOfType(targetKey, relationshipType)(p)
  }

  // Specialized query API methods

  def findAllTableBreakdownRelationships: Seq[TableBreakdownRelationship] = {
    findAllTableRelationshipsOfType(classTag[TableBreakdownRelationship])
  }

  def filterTableBreakdownRelationships(p: TableBreakdownRelationship => Boolean): Seq[TableBreakdownRelationship] = {
    filterTableRelationshipsOfType(classTag[TableBreakdownRelationship])(p)
  }

  def findAllOutgoingTableBreakdownRelationships(table: Table): Seq[TableBreakdownRelationship] = {
    findAllOutgoingTableRelationshipsOfType(table.ownKey, classTag[TableBreakdownRelationship])
  }

  def filterOutgoingTableBreakdownRelationships(table: Table)(p: TableBreakdownRelationship => Boolean): Seq[TableBreakdownRelationship] = {
    filterOutgoingTableRelationshipsOfType(table.ownKey, classTag[TableBreakdownRelationship])(p)
  }

  def findAllOutgoingBreakdownTreeRelationships(breakdown: TableBreakdown): Seq[BreakdownTreeRelationship] = {
    findAllOutgoingTableRelationshipsOfType(breakdown.ownKey, classTag[BreakdownTreeRelationship])
  }

  def filterOutgoingBreakdownTreeRelationships(breakdown: TableBreakdown)(
      p: BreakdownTreeRelationship => Boolean): Seq[BreakdownTreeRelationship] = {
    filterOutgoingTableRelationshipsOfType(breakdown.ownKey, classTag[BreakdownTreeRelationship])(p)
  }

  def findAllOutgoingDefinitionNodeSubtreeRelationships(node: DefinitionNode): Seq[DefinitionNodeSubtreeRelationship] = {
    findAllOutgoingTableRelationshipsOfType(node.ownKey, classTag[DefinitionNodeSubtreeRelationship])
  }

  def filterOutgoingDefinitionNodeSubtreeRelationships(node: DefinitionNode)(
      p: DefinitionNodeSubtreeRelationship => Boolean): Seq[DefinitionNodeSubtreeRelationship] = {
    filterOutgoingTableRelationshipsOfType(node.ownKey, classTag[DefinitionNodeSubtreeRelationship])(p)
  }

  def findAllOutgoingTableFilterRelationships(table: Table): Seq[TableFilterRelationship] = {
    findAllOutgoingTableRelationshipsOfType(table.ownKey, classTag[TableFilterRelationship])
  }

  def filterOutgoingTableFilterRelationships(table: Table)(p: TableFilterRelationship => Boolean): Seq[TableFilterRelationship] = {
    filterOutgoingTableRelationshipsOfType(table.ownKey, classTag[TableFilterRelationship])(p)
  }

  def findAllOutgoingTableParameterRelationships(table: Table): Seq[TableParameterRelationship] = {
    findAllOutgoingTableRelationshipsOfType(table.ownKey, classTag[TableParameterRelationship])
  }

  def filterOutgoingTableParameterRelationships(table: Table)(p: TableParameterRelationship => Boolean): Seq[TableParameterRelationship] = {
    filterOutgoingTableRelationshipsOfType(table.ownKey, classTag[TableParameterRelationship])(p)
  }
}
