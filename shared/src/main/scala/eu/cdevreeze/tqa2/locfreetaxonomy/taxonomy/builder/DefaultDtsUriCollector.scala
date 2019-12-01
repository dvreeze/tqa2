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

import scala.util.{Failure, Success, Try}
import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Import
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.LinkbaseRef
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem

/**
 * Default DTS URI collector, in the locator-free taxonomy model. All collected URIs are assumed to be absolute URIs.
 *
 * For more information on entrypoints and DTSes in the locator-free model, see the validation rules on "entrypoints".
 *
 * Of particular interest is that "DTS discovery" is 1 level deep only. That is, entrypoint schemas cannot refer to other entrypoint
 * schemas, but they can be combined with other entrypoint schemas making up one multi-document entrypoint.
 *
 * @author Chris de Vreeze
 */
object DefaultDtsUriCollector extends DtsUriCollector {

  def findAllDtsUris(entrypoint: Set[URI], taxoElemBuilder: URI => TaxonomyElem): Set[URI] = {
    entrypoint.toSeq.flatMap { docUri =>
      val docElem: TaxonomyElem = Try(taxoElemBuilder(docUri)) match {
        case Success(v)   => v
        case Failure(exc) => throw new RuntimeException(s"Missing document with URI $docUri", exc)
      }

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
