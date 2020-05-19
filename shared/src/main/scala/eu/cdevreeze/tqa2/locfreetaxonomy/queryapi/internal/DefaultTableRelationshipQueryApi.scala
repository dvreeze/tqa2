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
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.NonStandardRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.TableRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.TableRelationship

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
}
