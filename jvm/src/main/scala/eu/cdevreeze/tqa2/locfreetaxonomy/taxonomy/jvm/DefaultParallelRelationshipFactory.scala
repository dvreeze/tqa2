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

import eu.cdevreeze.tqa2.common.xpointer.XPointer
import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys
import eu.cdevreeze.tqa2.locfreetaxonomy.dom._
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Endpoint
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Relationship
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.RelationshipFactory
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyBase

import scala.collection.parallel.CollectionConverters._

/**
 * Default parallel relationship factory implementation. JVM-only.
 *
 * @author Chris de Vreeze
 */
object DefaultParallelRelationshipFactory extends RelationshipFactory {

  def extractRelationships(taxonomy: TaxonomyBase, arcFilter: XLinkArc => Boolean): Seq[Relationship] = {
    taxonomy.rootElemMap.keySet.toSeq
      .sortBy(_.toString)
      .par
      .flatMap { uri =>
        extractRelationshipsFromDocument(uri, taxonomy, arcFilter)
      }
      .seq
  }

  def extractRelationshipsFromDocument(docUri: URI, taxonomy: TaxonomyBase, arcFilter: XLinkArc => Boolean): Seq[Relationship] = {
    val rootElemOption = taxonomy.rootElemMap.get(docUri)

    rootElemOption.toList.flatMap { rootElem =>
      val extendedLinks =
        rootElem.findTopmostElemsOrSelf(_.isInstanceOf[ExtendedLink]).collect { case link: ExtendedLink => link }

      extendedLinks.flatMap(link => extractRelationshipsFromExtendedLink(link, taxonomy, arcFilter))
    }.toIndexedSeq
  }

  def extractRelationshipsFromExtendedLink(
      extendedLink: ExtendedLink,
      taxonomy: TaxonomyBase,
      arcFilter: XLinkArc => Boolean): Seq[Relationship] = {

    val labeledResourceMap = extendedLink.labeledXlinkResourceMap
    val baseUriOption = extendedLink.baseUriOption

    extendedLink.arcs.filter(arcFilter).flatMap { arc =>
      extractRelationshipsFromArc(arc, labeledResourceMap, baseUriOption, taxonomy)
    }
  }

  def extractRelationshipsFromArc(
      arc: XLinkArc,
      labeledResourceMap: Map[String, Seq[XLinkResource]],
      parentBaseUriOption: Option[URI],
      taxonomy: TaxonomyBase): Seq[Relationship] = {

    val sourceResources =
      labeledResourceMap.getOrElse(arc.from, sys.error(s"No resource with label ${arc.from} (in ${arc.docUri})"))

    val targetResources =
      labeledResourceMap.getOrElse(arc.to, sys.error(s"No resource with label ${arc.to} (in ${arc.docUri})"))

    for {
      sourceResource <- sourceResources
      source = makeEndpoint(sourceResource, taxonomy)
      targetResource <- targetResources
      target = makeEndpoint(targetResource, taxonomy)
    } yield {
      Relationship(arc, source, target)
    }
  }

  private def makeEndpoint(resource: XLinkResource, taxonomy: TaxonomyBase): Endpoint = {
    // First ignore "locators" to "resources"

    val rawResult: Endpoint =
      resource match {
        case key: TaxonomyElemKey =>
          Endpoint.KeyEndpoint(key.taxoElemKey)
        case nonKey: NonKeyResource =>
          // TODO Improve. What if the element is a root element, for example? So we need a URI fragment then?
          val docUri: URI = nonKey.docUri
          val xpointer: XPointer = XPointer.toXPointer(nonKey.underlyingElem)
          val ownUri: URI = new URI(docUri.getScheme, docUri.getSchemeSpecificPart, xpointer.toString)

          val ownKey: TaxonomyElemKeys.AnyElementKey = TaxonomyElemKeys.AnyElementKey(ownUri)

          Endpoint.LocalResource(ownKey, nonKey)
      }

    // TODO Improve performance

    // Now fix "locators" to "resources"

    (rawResult.taxonomyElemKey, rawResult.targetResourceOption) match {
      case (key: TaxonomyElemKeys.AnyElementKey, None) =>
        val elem = taxonomy.getElemByUri(key.key)

        elem match {
          case res: NonKeyResource =>
            Endpoint.RemoteResource(key, res)
          case _ =>
            rawResult
        }
      case _ =>
        rawResult
    }
  }
}
