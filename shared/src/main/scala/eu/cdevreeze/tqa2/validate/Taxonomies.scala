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

package eu.cdevreeze.tqa2.validate

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem

import scala.util.Try

/**
 * Utility object with functions that offer some "knowledge" about locator-free taxonomies. This utility can also
 * be used for standard taxonomy files, unless mentioned otherwise.
 *
 * @author Chris de Vreeze
 */
object Taxonomies {

  /**
   * Returns true if the document URI belongs to www.w3.org, www.xbrl.org or www.locfreexbrl.org. Note that among the
   * schemas there is also one linkbase file (having one simple link), namely http://www.xbrl.org/2016/severities.xml.
   */
  def isCoreDocumentUri(uri: URI): Boolean = {
    val host = Option(uri.getHost).getOrElse("")

    Set("www.w3.org", "xbrl.org", "www.xbrl.org", "www.locfreexbrl.org").contains(host)
  }

  /**
   * Returns true if function isCoreDocumentUri returns false.
   */
  def isProperTaxonomyDocumentUri(uri: URI): Boolean = {
    !isCoreDocumentUri(uri)
  }

  def isCoreNamespace(ns: String): Boolean = Try(isCoreDocumentUri(URI.create(ns))).getOrElse(false)

  def isProperTaxonomySchemaNamespace(ns: String): Boolean = !isCoreNamespace(ns)

  def isCoreDocumentContent(taxoElem: TaxonomyElem): Boolean = {
    isCoreDocumentUri(taxoElem.docUri)
  }

  def isProperTaxonomyDocumentContent(taxoElem: TaxonomyElem): Boolean = {
    isProperTaxonomyDocumentUri(taxoElem.docUri)
  }

  def canBeLocFreeDocument(taxoElem: TaxonomyElem): Boolean = {
    taxoElem.name == ENames.XsSchemaEName || taxoElem.name == ENames.CLinkLinkbaseEName
  }

  /**
   * Returns true if this document is standalone according to the locator-free model. That is, returns true
   * if method canBeLocFreeDocument returns true and all xs:import elements have no schemaLocation attribute
   * and there are no clink:linkbaseRef elements.
   */
  def canBeStandaloneLocFreeDocument(taxoElem: TaxonomyElem): Boolean = {
    canBeLocFreeDocument(taxoElem) && {
      taxoElem.filterDescendantElems { e =>
        (e.name == ENames.XsImportEName && e.attrOption(ENames.SchemaLocationEName).nonEmpty) ||
        (e.name == ENames.CLinkLinkbaseRefEName)
      }.isEmpty
    }
  }

  /**
   * Returns true if this document is incomplete according to the locator-free model. That is, returns true
   * if method canBeLocFreeDocument returns true, and either at least one xs:import elements has a schemaLocation attribute
   * or there is at least one clink:linkbaseRef element.
   */
  def canBeIncompleteLocFreeDocument(taxoElem: TaxonomyElem): Boolean = {
    canBeLocFreeDocument(taxoElem) && {
      taxoElem.filterDescendantElems { e =>
        (e.name == ENames.XsImportEName && e.attrOption(ENames.SchemaLocationEName).nonEmpty) ||
        (e.name == ENames.CLinkLinkbaseRefEName)
      }.nonEmpty
    }
  }
}
