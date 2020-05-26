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

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.ElementKey
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.RelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.StandardRelationshipQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.StandardRelationship
import eu.cdevreeze.yaidom2.core.EName

import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * Implementation of StandardRelationshipQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultStandardRelationshipQueryApi extends StandardRelationshipQueryApi with RelationshipQueryApi {

  def findAllStandardRelationships: Seq[StandardRelationship] = {
    findAllRelationshipsOfType(classTag[StandardRelationship])
  }

  def filterStandardRelationships(p: StandardRelationship => Boolean): Seq[StandardRelationship] = {
    filterRelationshipsOfType(classTag[StandardRelationship])(p)
  }

  def findAllStandardRelationshipsOfType[A <: StandardRelationship](relationshipType: ClassTag[A]): Seq[A] = {
    findAllRelationshipsOfType(relationshipType)
  }

  def filterStandardRelationshipsOfType[A <: StandardRelationship](relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {

    filterRelationshipsOfType(relationshipType)(p)
  }

  def findAllOutgoingStandardRelationships(sourceConcept: EName): Seq[StandardRelationship] = {
    findAllOutgoingRelationshipsOfType(ElementKey(sourceConcept), classTag[StandardRelationship])
  }

  def filterOutgoingStandardRelationships(sourceConcept: EName)(p: StandardRelationship => Boolean): Seq[StandardRelationship] = {
    filterOutgoingRelationshipsOfType(ElementKey(sourceConcept), classTag[StandardRelationship])(p)
  }

  def findAllOutgoingStandardRelationshipsOfType[A <: StandardRelationship](sourceConcept: EName, relationshipType: ClassTag[A]): Seq[A] = {
    findAllOutgoingRelationshipsOfType(ElementKey(sourceConcept), relationshipType)
  }

  def filterOutgoingStandardRelationshipsOfType[A <: StandardRelationship](sourceConcept: EName, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A] = {

    filterOutgoingRelationshipsOfType(ElementKey(sourceConcept), relationshipType)(p)
  }
}
