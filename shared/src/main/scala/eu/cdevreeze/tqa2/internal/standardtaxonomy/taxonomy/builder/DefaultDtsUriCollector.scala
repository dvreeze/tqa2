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

package eu.cdevreeze.tqa2.internal.standardtaxonomy.taxonomy.builder

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.internal.standardtaxonomy.dom._
import eu.cdevreeze.yaidom2.queryapi.anyElem

import scala.annotation.tailrec
import scala.reflect.classTag

/**
 * Default DTS URI collector, for 'standard' taxonomies.
 *
 * @author Chris de Vreeze
 */
trait DefaultDtsUriCollector extends DtsUriCollector {

  /**
   * Finds all URIs of taxonomy documents in the DTS, given the parameter entrypoint. If the entrypoint is an empty set
   * of URIs, an empty DTS is returned. The returned DTS always includes the entrypoint.
   */
  def findAllDtsUris(entrypoint: Set[URI], taxoElemBuilder: URI => TaxonomyElem): Set[URI] = {
    if (entrypoint.isEmpty) {
      Set.empty
    } else {
      val dts: Map[URI, TaxonomyElem] = findDts(entrypoint, Map.empty, taxoElemBuilder)
      dts.keySet
    }
  }

  /**
   * Overridable method to return all document URIs used in the given document only (as document element). In other words,
   * this method returns all URIs contributed to the DTS by the given document (other than the document URI itself).
   */
  def findAllUsedDocUris(docElem: TaxonomyElem): Set[URI] = {
    val taxoRootElems = docElem
      .findTopmostElemsOrSelfOfType(classTag[TaxonomyRootElem with TaxonomyElem])(anyElem)

    taxoRootElems.flatMap(e => findAllUsedDocUrisInTaxonomyRoot(e)).toSet
  }

  @tailrec
  private def findDts(
      docUris: Set[URI],
      processedDocElems: Map[URI, TaxonomyElem],
      taxoElemBuilder: URI => TaxonomyElem): Map[URI, TaxonomyElem] = {

    assert(docUris.nonEmpty)

    val processedDocUris: Set[URI] = processedDocElems.keySet

    assert(processedDocUris.subsetOf(docUris))

    // One step, processing all URIs currently known, and not yet processed
    val docUrisToProcess: Set[URI] = docUris.diff(processedDocUris)

    val taxoDocElemsFound: Seq[TaxonomyElem] = docUrisToProcess.toIndexedSeq.map(uri => taxoElemBuilder(uri))

    val taxoDocMapFound: Map[URI, TaxonomyElem] = taxoDocElemsFound.map(e => e.docUri -> e).toMap

    val docUrisFound: Set[URI] = taxoDocElemsFound.flatMap(e => findAllUsedDocUris(e)).toSet

    val newDocUris: Set[URI] = docUris.union(docUrisFound)

    val newProcessedDocs: Map[URI, TaxonomyElem] = processedDocElems ++ taxoDocMapFound

    assert(newProcessedDocs.keySet == docUris)

    if (docUrisFound.subsetOf(docUris)) {
      assert(newDocUris == docUris)

      newProcessedDocs
    } else {
      assert(newDocUris.diff(docUris).nonEmpty)

      // Recursive call
      findDts(newDocUris, newProcessedDocs, taxoElemBuilder)
    }
  }

  private def findAllUsedDocUrisInTaxonomyRoot(taxonomyRootElem: TaxonomyRootElem): Set[URI] = {
    taxonomyRootElem match {
      case xsSchema: XsSchema =>
        // Minding embedded linkbases

        findAllUsedDocUrisInXsSchema(xsSchema).union {
          xsSchema
            .findAllDescendantElemsOfType(classTag[Linkbase])
            .flatMap(lb => findAllUsedDocUrisInLinkbase(lb))
            .toSet
        }
      case linkbase: Linkbase =>
        findAllUsedDocUrisInLinkbase(linkbase)
    }
  }

  private def findAllUsedDocUrisInXsSchema(rootElem: XsSchema): Set[URI] = {
    // Using the base URI instead of document URI for xs:import and xs:include (although XML Schema knows nothing about XML Base)

    val imports = rootElem.findAllImports
    val includes = rootElem.filterDescendantElems(_.name == ENames.XsIncludeEName)
    val linkbaseRefs = rootElem.findAllDescendantElemsOfType(classTag[LinkbaseRef])

    val importUris =
      imports.flatMap(e => e.attrOption(ENames.SchemaLocationEName).map(u => makeAbsoluteWithoutFragment(URI.create(u), e)))
    val includeUris =
      includes.flatMap(e => e.attrOption(ENames.SchemaLocationEName).map(u => makeAbsoluteWithoutFragment(URI.create(u), e)))
    val linkbaseRefUris =
      linkbaseRefs.filter(e => e.rawHref != EmptyUri).map(e => makeAbsoluteWithoutFragment(e.rawHref, e))

    (importUris ++ includeUris ++ linkbaseRefUris).toSet.diff(Set(rootElem.docUri))
  }

  private def findAllUsedDocUrisInLinkbase(rootElem: Linkbase): Set[URI] = {
    // Only link:loc locators are used in DTS discovery.

    val locs = rootElem.findAllDescendantElemsOfType(classTag[StandardLoc])
    val roleRefs = rootElem.findAllDescendantElemsOfType(classTag[RoleRef])
    val arcroleRefs = rootElem.findAllDescendantElemsOfType(classTag[ArcroleRef])

    val locUris =
      locs.filter(e => e.rawHref != EmptyUri).map(e => makeAbsoluteWithoutFragment(e.rawHref, e))
    val roleRefUris =
      roleRefs.filter(e => e.rawHref != EmptyUri).map(e => makeAbsoluteWithoutFragment(e.rawHref, e))
    val arcroleRefUris =
      arcroleRefs.filter(e => e.rawHref != EmptyUri).map(e => makeAbsoluteWithoutFragment(e.rawHref, e))

    (locUris ++ roleRefUris ++ arcroleRefUris).toSet.diff(Set(rootElem.docUri))
  }

  private def makeAbsoluteWithoutFragment(uri: URI, elem: TaxonomyElem): URI = {
    removeFragment(elem.baseUri.resolve(uri))
  }

  private def removeFragment(uri: URI): URI = {
    if (uri.getFragment == null) {
      // No need to create a new URI in this case
      uri
    } else {
      new URI(uri.getScheme, uri.getSchemeSpecificPart, null)
    }
  }

  private val EmptyUri = URI.create("")
}

object DefaultDtsUriCollector {

  val instance: DefaultDtsUriCollector = new DefaultDtsUriCollector {}
}
