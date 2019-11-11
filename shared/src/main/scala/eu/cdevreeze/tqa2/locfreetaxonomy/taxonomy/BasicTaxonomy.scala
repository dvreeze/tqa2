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

package eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy

import scala.reflect.ClassTag

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.TaxonomyElemKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.internal.DefaultTaxonomyQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Relationship

/**
 * Basic taxonomy, and the default implementation of trait TaxonomyQueryApi.
 *
 * @author Chris de Vreeze
 */
abstract class BasicTaxonomy ( // TODO Make class final and make constructor private!
  val rootElems: Seq[TaxonomyElem],
  val relationshipTypes: Set[ClassTag[_ <: Relationship]],
  val relationshipMap: Map[ClassTag[_ <: Relationship], Seq[Relationship]],
  val outgoingRelationshipMap: Map[ClassTag[_ <: TaxonomyElemKey], Map[TaxonomyElemKey, Seq[Relationship]]],
  val incomingRelationshipMap: Map[ClassTag[_ <: TaxonomyElemKey], Map[TaxonomyElemKey, Seq[Relationship]]]
) extends DefaultTaxonomyQueryApi {

}

object BasicTaxonomy {

  def build(): BasicTaxonomy = {
    ???
  }
}
