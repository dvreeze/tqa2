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

package eu.cdevreeze.tqa2.locfreetaxonomy.relationship

import java.net.URI

import eu.cdevreeze.tqa2.common.xpointer.XPointer
import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.ExtendedLink
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.NonKeyResource
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElemKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XLinkArc
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XLinkResource

/**
 * Default relationship factory implementation.
 *
 * @author Chris de Vreeze
 */
class DefaultRelationshipFactory extends RelationshipFactory {

  def extractRelationships(
    taxonomy: Map[URI, TaxonomyElem],
    arcFilter: XLinkArc => Boolean): Seq[Relationship] = {

    taxonomy.keySet.toSeq.sortBy(_.toString).flatMap { uri =>
      extractRelationshipsFromDocument(uri, taxonomy, arcFilter)
    }
  }

  def extractRelationshipsFromDocument(
    docUri: URI,
    taxonomy: Map[URI, TaxonomyElem],
    arcFilter: XLinkArc => Boolean): Seq[Relationship] = {

    val rootElemOption = taxonomy.get(docUri)

    rootElemOption.toList.flatMap { rootElem =>
      val extendedLinks =
        rootElem.findTopmostElemsOrSelf(_.isInstanceOf[ExtendedLink]).collect { case link: ExtendedLink => link }

      extendedLinks.flatMap(link => extractRelationshipsFromExtendedLink(link, taxonomy, arcFilter))
    }.toIndexedSeq
  }

  def extractRelationshipsFromExtendedLink(
    extendedLink: ExtendedLink,
    taxonomy: Map[URI, TaxonomyElem],
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
    taxonomy: Map[URI, TaxonomyElem]): Seq[Relationship] = {

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

  private def makeEndpoint(resource: XLinkResource, taxonomy: Map[URI, TaxonomyElem]): Endpoint = {
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
        val docUri = withoutFragment(key.key)
        val docElem = taxonomy.getOrElse(docUri, sys.error(s"Missing document $docUri"))
        val elem = XPointer.findElem(docElem, XPointer.parseXPointers(key.key.getFragment))
          .getOrElse(sys.error(s"Missing element in $docUri with relative XPointer(s) ${key.key.getFragment}"))

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

  private def withoutFragment(uri: URI): URI = {
    new URI(uri.getScheme, uri.getSchemeSpecificPart, null) // scalastyle:off null
  }
}
