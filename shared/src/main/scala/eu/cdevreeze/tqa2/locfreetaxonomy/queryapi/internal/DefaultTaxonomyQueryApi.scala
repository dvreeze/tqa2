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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.queryapi.TaxonomyQueryApi
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Relationship

import scala.reflect.ClassTag

/**
 * Partial but mostly complete implementation of TaxonomyQueryApi. The methods are overridable, which is needed in case more
 * efficient implementations are possible. It should act as an "abstract base class" for concrete taxonomy classes that
 * offer the TaxonomyQueryApi interface.
 *
 * @author Chris de Vreeze
 */
// scalastyle:off number.of.methods
trait DefaultTaxonomyQueryApi
    extends TaxonomyQueryApi
    with DefaultSchemaQueryApi
    with DefaultTaxonomySchemaQueryApi
    with DefaultStandardRelationshipQueryApi
    with DefaultNonStandardRelationshipQueryApi
    with DefaultInterConceptRelationshipQueryApi
    with DefaultPresentationRelationshipQueryApi
    with DefaultConceptLabelRelationshipQueryApi
    with DefaultConceptReferenceRelationshipQueryApi
    with DefaultElementLabelRelationshipQueryApi
    with DefaultElementReferenceRelationshipQueryApi
    with DefaultDimensionalRelationshipQueryApi
    with DefaultFormulaRelationshipQueryApi
    with DefaultTableRelationshipQueryApi {

  // The following abstract methods must be implemented as val fields

  /**
   * All document elements in the taxonomy
   */
  def rootElems: Seq[TaxonomyElem]

  /**
   * The key set of the relationship map
   */
  def relationshipTypes: Set[ClassTag[_ <: Relationship]]

  /**
   * The mapping from relationship runtime classes to collections of relationships of that type.
   * The map contains no duplicate relationships anywhere, so each relationship occurs only once.
   */
  def relationshipMap: Map[ClassTag[_ <: Relationship], Seq[Relationship]]

  /**
   * Mapping from taxonomy element key class tags to maps from taxonomy element keys (of that type) to collections of
   * relationships outgoing from that taxonomy element key.
   */
  def outgoingRelationshipMap: Map[ClassTag[_ <: TaxonomyElemKey], Map[TaxonomyElemKey, Seq[Relationship]]]

  /**
   * Mapping from taxonomy element key class tags to maps from taxonomy element keys (of that type) to collections of
   * relationships incoming to that taxonomy element key.
   */
  def incomingRelationshipMap: Map[ClassTag[_ <: TaxonomyElemKey], Map[TaxonomyElemKey, Seq[Relationship]]]

  // RelationshipQueryApi

  def findAllRelationshipsOfType[A <: Relationship](relationshipType: ClassTag[A]): Seq[A] = {
    filterRelationshipsOfType[A](relationshipType)(_ => true)
  }

  def filterRelationshipsOfType[A <: Relationship](relationshipType: ClassTag[A])(p: A => Boolean): Seq[A] = {
    implicit val clsTag: ClassTag[A] = relationshipType

    val relationshipClassTags: Set[ClassTag[_ <: Relationship]] =
      relationshipTypes.filter(tp => relationshipType.runtimeClass.isAssignableFrom(tp.runtimeClass))

    relationshipMap.view.filterKeys(relationshipClassTags).iterator.flatMap(_._2).toSeq.collect { case rel: A if p(rel) => rel }
  }

  def findAllOutgoingRelationships(sourceKey: TaxonomyElemKey): Seq[Relationship] = {
    filterOutgoingRelationships(sourceKey)(_ => true)
  }

  def filterOutgoingRelationships(sourceKey: TaxonomyElemKey)(p: Relationship => Boolean): Seq[Relationship] = {
    outgoingRelationshipMap.get(ClassTag(sourceKey.getClass)).flatMap(_.get(sourceKey)).getOrElse(Seq.empty).filter(p)
  }

  def findAllOutgoingRelationshipsOfType[A <: Relationship](sourceKey: TaxonomyElemKey, relationshipType: ClassTag[A]): Seq[A] = {
    filterOutgoingRelationshipsOfType[A](sourceKey, relationshipType)(_ => true)
  }

  def filterOutgoingRelationshipsOfType[A <: Relationship](sourceKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A] = {

    implicit val clsTag: ClassTag[A] = relationshipType

    findAllOutgoingRelationships(sourceKey).collect { case rel: A if p(rel) => rel }
  }

  def findAllIncomingRelationships(targetKey: TaxonomyElemKey): Seq[Relationship] = {
    filterIncomingRelationships(targetKey)(_ => true)
  }

  def filterIncomingRelationships(targetKey: TaxonomyElemKey)(p: Relationship => Boolean): Seq[Relationship] = {
    incomingRelationshipMap.get(ClassTag(targetKey.getClass)).flatMap(_.get(targetKey)).getOrElse(Seq.empty).filter(p)
  }

  def findAllIncomingRelationshipsOfType[A <: Relationship](targetKey: TaxonomyElemKey, relationshipType: ClassTag[A]): Seq[A] = {
    filterIncomingRelationshipsOfType[A](targetKey, relationshipType)(_ => true)
  }

  def filterIncomingRelationshipsOfType[A <: Relationship](targetKey: TaxonomyElemKey, relationshipType: ClassTag[A])(
      p: A => Boolean): Seq[A] = {

    implicit val clsTag: ClassTag[A] = relationshipType

    findAllIncomingRelationships(targetKey).collect { case rel: A if p(rel) => rel }
  }

  // DefaultStandardRelationshipQueryApi

  // DefaultNonStandardRelationshipQueryApi

  // DefaultInterConceptRelationshipQueryApi

  // DefaultPresentationRelationshipQueryApi

  // DefaultConceptLabelRelationshipQueryApi

  // DefaultConceptReferenceRelationshipQueryApi

  // DefaultElementLabelRelationshipQueryApi

  // DefaultElementReferenceRelationshipQueryApi

  // DefaultDimensionalRelationshipQueryApi

  // SchemaQueryApi

  // TaxonomySchemaQueryApi

  // TaxonomyQueryApi

  def relationships: Seq[Relationship] = {
    relationshipMap.toSeq.flatMap(_._2)
  }
}
