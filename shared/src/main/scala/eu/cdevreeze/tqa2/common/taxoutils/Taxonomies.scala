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

package eu.cdevreeze.tqa2.common.taxoutils

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.yaidom2.queryapi.BackingNodes

import scala.util.Try

/**
 * Utility object with functions that offer some "knowledge" about locator-free taxonomies. This utility can also
 * be used for standard taxonomy files, unless mentioned otherwise.
 *
 * @author Chris de Vreeze
 */
object Taxonomies {

  /**
   * Returns true if the URI has host www.xbrl.org and a path starting with "/taxonomy/". Documents having these URIs
   * are not considered core taxonomy documents.
   */
  def isXbrlOrgTaxonomyUri(uri: URI): Boolean = {
    Option(uri.getHost).contains("www.xbrl.org") && Option(uri.getPath).exists(_.startsWith("/taxonomy/"))
  }

  /**
   * Returns true if the namespace interpreted as URI returns true on calling function isXbrlOrgTaxonomyUri. Schema documents
   * having these target namespaces are not considered core taxonomy documents.
   */
  def isXbrlOrgTaxonomyNamespace(ns: String): Boolean = {
    Try(isXbrlOrgTaxonomyUri(URI.create(ns))).getOrElse(false)
  }

  /**
   * Returns true if the document URI belongs to www.w3.org, www.xbrl.org or www.locfreexbrl.org, but function isXbrlOrgTaxonomyUri
   * returns false. Note that among the files (mostly schemas) there is also one linkbase file (having one simple link), namely
   * http://www.xbrl.org/2016/severities.xml.
   */
  def isCoreDocumentUri(uri: URI): Boolean = {
    val host = Option(uri.getHost).getOrElse("")

    Set("www.w3.org", "xbrl.org", "www.xbrl.org", "www.locfreexbrl.org").contains(host) && !isXbrlOrgTaxonomyUri(uri)
  }

  /**
   * Returns true if function isCoreDocumentUri returns false.
   */
  def isProperTaxonomyDocumentUri(uri: URI): Boolean = {
    !isCoreDocumentUri(uri)
  }

  def isCoreNamespace(ns: String): Boolean = Try(isCoreDocumentUri(URI.create(ns))).getOrElse(false)

  def isProperTaxonomySchemaNamespace(ns: String): Boolean = !isCoreNamespace(ns)

  def isCoreDocumentContent(taxoElem: BackingNodes.Elem): Boolean = {
    isCoreDocumentUri(taxoElem.docUri)
  }

  def isProperTaxonomyDocumentContent(taxoElem: BackingNodes.Elem): Boolean = {
    isProperTaxonomyDocumentUri(taxoElem.docUri)
  }

  def canBeLocFreeDocument(taxoElem: BackingNodes.Elem): Boolean = {
    taxoElem.name == ENames.XsSchemaEName || taxoElem.name == ENames.CLinkLinkbaseEName
  }

  /**
   * Returns true if this document is standalone according to the locator-free model. That is, returns true
   * if method canBeLocFreeDocument returns true and all xs:import elements have no schemaLocation attribute
   * and there are no clink:linkbaseRef elements.
   */
  def canBeStandaloneLocFreeDocument(taxoElem: BackingNodes.Elem): Boolean = {
    canBeLocFreeDocument(taxoElem) && {
      taxoElem.filterDescendantElems { e =>
        (e.name == ENames.XsImportEName && e.attrOption(ENames.SchemaLocationEName).nonEmpty) ||
        (e.name == ENames.CLinkLinkbaseRefEName)
      }.isEmpty
    }
  }

  /**
   * Returns true if this document is a root document according to the locator-free model. That is, returns true
   * if method canBeLocFreeDocument returns true, and either at least one xs:import elements has a schemaLocation attribute
   * or there is at least one clink:linkbaseRef element.
   */
  def canBeLocFreeRootDocument(taxoElem: BackingNodes.Elem): Boolean = {
    canBeLocFreeDocument(taxoElem) && {
      taxoElem.filterDescendantElems { e =>
        (e.name == ENames.XsImportEName && e.attrOption(ENames.SchemaLocationEName).nonEmpty) ||
        (e.name == ENames.CLinkLinkbaseRefEName)
      }.nonEmpty
    }
  }

  val locfreeSchemaUris: Set[URI] = Set(
    URI.create("http://www.locfreexbrl.org/2003/locatorfreexbrl-linkbase-2003-12-31.xsd"),
    URI.create("http://www.locfreexbrl.org/2005/locatorfreexbrl-xbrldt-2005.xsd"),
    URI.create("http://www.locfreexbrl.org/2008/locatorfreexbrl-generic-link.xsd"),
    URI.create("http://www.locfreexbrl.org/2019/locatorfreexbrl-elementkey-2019.xsd"),
    URI.create("http://www.locfreexbrl.org/2019/locatorfreexbrl-extended-link.xsd"),
  )
}
