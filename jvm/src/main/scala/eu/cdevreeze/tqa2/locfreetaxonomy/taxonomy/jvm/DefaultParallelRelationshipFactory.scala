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

package eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.jvm

import java.net.URI

import eu.cdevreeze.tqa2.locfreetaxonomy.dom._
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Relationship
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.DefaultRelationshipFactory
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.RelationshipFactory
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyBase

import scala.collection.parallel.CollectionConverters._

/**
 * Default parallel relationship factory implementation. JVM-only.
 *
 * @author Chris de Vreeze
 */
object DefaultParallelRelationshipFactory extends RelationshipFactory {

  private val defaultRelationshipFactory: DefaultRelationshipFactory.type = DefaultRelationshipFactory

  def extractRelationships(taxonomy: TaxonomyBase, arcFilter: XLinkArc => Boolean): Seq[Relationship] = {
    taxonomy.rootElemMap.keySet.toSeq.par.flatMap { uri =>
      extractRelationshipsFromDocument(uri, taxonomy, arcFilter)
    }.seq
  }

  def extractRelationshipsFromDocument(docUri: URI, taxonomy: TaxonomyBase, arcFilter: XLinkArc => Boolean): Seq[Relationship] = {
    defaultRelationshipFactory.extractRelationshipsFromDocument(docUri, taxonomy, arcFilter)
  }

  def extractRelationshipsFromExtendedLink(
      extendedLink: ExtendedLink,
      taxonomy: TaxonomyBase,
      arcFilter: XLinkArc => Boolean): Seq[Relationship] = {

    defaultRelationshipFactory.extractRelationshipsFromExtendedLink(extendedLink, taxonomy, arcFilter)
  }

  def extractRelationshipsFromArc(
      arc: XLinkArc,
      labeledResourceMap: Map[String, Seq[XLinkResource]],
      parentBaseUriOption: Option[URI],
      taxonomy: TaxonomyBase): Seq[Relationship] = {

    defaultRelationshipFactory.extractRelationshipsFromArc(arc, labeledResourceMap, parentBaseUriOption, taxonomy)
  }
}
