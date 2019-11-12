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

import scala.reflect.classTag

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.TaxonomyElemKey
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.{ElementLabelRelationshipQueryApi, NonStandardRelationshipQueryApi}
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ElementLabelRelationship

/**
 * Implementation of ElementLabelRelationshipQueryApi. The methods are overridable, which can be considered in case more efficient
 * implementations are possible.
 *
 * @author Chris de Vreeze
 */
trait DefaultElementLabelRelationshipQueryApi extends ElementLabelRelationshipQueryApi with NonStandardRelationshipQueryApi {

  // Finding and filtering relationships without looking at source element

  def findAllElementLabelRelationships: Seq[ElementLabelRelationship] = {
    findAllNonStandardRelationshipsOfType(classTag[ElementLabelRelationship])
  }

  def filterElementLabelRelationships(
    p: ElementLabelRelationship => Boolean): Seq[ElementLabelRelationship] = {

    filterNonStandardRelationshipsOfType(classTag[ElementLabelRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  /**
   * Finds all element-label relationships that are outgoing from the given XML element. This must be implemented as a fast
   * method, for example by exploiting a mapping from taxonomy element keys to outgoing relationships.
   */
  def findAllOutgoingElementLabelRelationships(
    sourceKey: TaxonomyElemKey): Seq[ElementLabelRelationship] = {

    findAllOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[ElementLabelRelationship])
  }

  /**
   * Filters element-label relationships that are outgoing from the given XML element. This must be implemented as a fast
   * method, for example by exploiting a mapping from taxonomy element keys to outgoing relationships.
   */
  def filterOutgoingElementLabelRelationships(
    sourceKey: TaxonomyElemKey)(p: ElementLabelRelationship => Boolean): Seq[ElementLabelRelationship] = {

    filterOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[ElementLabelRelationship])(p)
  }
}
