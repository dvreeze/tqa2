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
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.{ElementReferenceRelationshipQueryApi, NonStandardRelationshipQueryApi}
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ElementReferenceRelationship

import scala.reflect.classTag

/**
 * Implementation of ElementReferenceRelationshipQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultElementReferenceRelationshipQueryApi extends ElementReferenceRelationshipQueryApi with NonStandardRelationshipQueryApi {

  // Finding and filtering relationships without looking at source element

  def findAllElementReferenceRelationships: Seq[ElementReferenceRelationship] = {
    findAllNonStandardRelationshipsOfType(classTag[ElementReferenceRelationship])
  }

  def filterElementReferenceRelationships(
    p: ElementReferenceRelationship => Boolean): Seq[ElementReferenceRelationship] = {

    filterNonStandardRelationshipsOfType(classTag[ElementReferenceRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  /**
   * Finds all element-reference relationships that are outgoing from the given XML element.This must be implemented as a fast
   * method, for example by exploiting a mapping from taxonomy element keys to outgoing relationships.
   */
  def findAllOutgoingElementReferenceRelationships(
    sourceKey: TaxonomyElemKey): Seq[ElementReferenceRelationship] = {

    findAllOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[ElementReferenceRelationship])
  }

  /**
   * Filters element-reference relationships that are outgoing from the given XML element.This must be implemented as a fast
   * method, for example by exploiting a mapping from taxonomy element keys to outgoing relationships.
   */
  def filterOutgoingElementReferenceRelationships(
    sourceKey: TaxonomyElemKey)(p: ElementReferenceRelationship => Boolean): Seq[ElementReferenceRelationship] = {

    filterOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[ElementReferenceRelationship])(p)
  }
}
