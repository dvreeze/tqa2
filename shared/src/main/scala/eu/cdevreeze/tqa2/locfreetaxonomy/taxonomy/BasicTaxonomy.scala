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

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.TaxonomyElemKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.NamedGlobalSchemaComponent
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.internal.DefaultTaxonomyQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.InterConceptRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.InterConceptRelationshipPath
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.NonStandardRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.NonStandardRelationshipPath
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Relationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.RelationshipPath
import eu.cdevreeze.yaidom2.core.EName

/**
 * Basic taxonomy, and the default implementation of trait TaxonomyQueryApi.
 *
 * @author Chris de Vreeze
 */
final class BasicTaxonomy( // TODO Make constructor private!
  val taxonomyBase: TaxonomyBase,
  val relationshipTypes: Set[ClassTag[_ <: Relationship]],
  val relationshipMap: Map[ClassTag[_ <: Relationship], Seq[Relationship]],
  val outgoingRelationshipMap: Map[ClassTag[_ <: TaxonomyElemKey], Map[TaxonomyElemKey, Seq[Relationship]]],
  val incomingRelationshipMap: Map[ClassTag[_ <: TaxonomyElemKey], Map[TaxonomyElemKey, Seq[Relationship]]],
  val stopAppendingFunction: (RelationshipPath, Relationship) => Boolean,
  val stopPrependingFunction: (RelationshipPath, Relationship) => Boolean
) extends DefaultTaxonomyQueryApi {

  override def stopAppending[A <: InterConceptRelationship](path: InterConceptRelationshipPath[A], next: A): Boolean = {
    stopAppendingFunction(path, next)
  }

  override def stopPrepending[A <: InterConceptRelationship](path: InterConceptRelationshipPath[A], prev: A): Boolean = {
    stopPrependingFunction(path, prev)
  }

  override def stopAppending[A <: NonStandardRelationship](path: NonStandardRelationshipPath[A], next: A): Boolean = {
    stopAppendingFunction(path, next)
  }

  override def stopPrepending[A <: NonStandardRelationship](path: NonStandardRelationshipPath[A], prev: A): Boolean = {
    stopPrependingFunction(path, prev)
  }

  /**
   * The collection of taxonomy document root elements. This is a very fast method, because it is taken from the taxonomy
   * base, where it is a field.
   */
  override def rootElems: Seq[TaxonomyElem] = taxonomyBase.rootElems

  override def substitutionGroupMap: SubstitutionGroupMap = taxonomyBase.netSubstitutionGroupMap

  /**
   * Mapping from target ENames to named top-level schema components having that target EName.
   * This is a very fast method, because it is taken from the taxonomy base, where it is a field.
   */
  override def namedGlobalSchemaComponentMap: Map[EName, Seq[NamedGlobalSchemaComponent]] = {
    taxonomyBase.namedGlobalSchemaComponentMap
  }
}

object BasicTaxonomy {

  def build(): BasicTaxonomy = {
    ???
  }
}
