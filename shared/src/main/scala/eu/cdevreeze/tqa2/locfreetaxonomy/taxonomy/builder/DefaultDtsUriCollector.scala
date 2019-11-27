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

package eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder

import java.net.URI

import scala.util.Try

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Import
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.LinkbaseRef
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem

/**
 * Default DTS URI collector, in the locator-free taxonomy model. All collected URIs are assumed to be absolute URIs.
 *
 * It is assumed that only schema documents can act as entrypoints, and if they do, they must refer directly to the entire
 * DTS. More precisely, the entire DTS is the document itself along with all documents (directly) referred to by the entrypoint.
 *
 * Hence, entrypoints cannot be nested, so entrypoints cannot refer to other entrypoints, etc. Note that per DTS typically only 1 schema
 * should act as entrypoint, so only 1 schema should contain URI references (in linkbaseRefs and schema imports). It is, however,
 * allowed to have multiple entrypoints, and it is allowed for them to have overlapping content. It is even allowed to use
 * a non-entrypoint schema or linkbase as (additional) "entrypoint", but then they only contribute themselves to the DTS, without pointing to
 * any other DTS content.
 *
 * Taxonomy extensions can either contain their additional entrypoint schema, or they can be just an additional set of taxonomy
 * documents without extra entrypoint schema.
 *
 * @author Chris de Vreeze
 */
object DefaultDtsUriCollector extends DtsUriCollector {

  def findAllDtsUris(entrypoint: Set[URI], taxoElemBuilder: URI => TaxonomyElem): Set[URI] = {
    entrypoint.toSeq.flatMap { docUri =>
      val docElem: TaxonomyElem = Try(taxoElemBuilder(docUri)).getOrElse(sys.error(s"Missing document with URI $docUri"))

      findOwnDtsUris(docElem).union(entrypoint)
    }.toSet
  }

  private def findOwnDtsUris(docElem: TaxonomyElem): Set[URI] = {
    docElem.name match {
      case ENames.XsSchemaEName =>
        val linkbaseRefs: Seq[LinkbaseRef] = docElem
          .filterDescendantElems(_.name == ENames.CLinkLinkbaseRefEName)
          .collect { case e: LinkbaseRef => e }

        val imports: Seq[Import] = docElem
          .filterDescendantElems(_.name == ENames.XsImportEName)
          .collect { case e: Import => e }

        linkbaseRefs
          .map(_.href)
          .toSet
          .union(imports.flatMap(_.attrOption(ENames.SchemaLocationEName).map(URI.create)).toSet)
      case _ =>
        Set.empty
    }
  }
}
