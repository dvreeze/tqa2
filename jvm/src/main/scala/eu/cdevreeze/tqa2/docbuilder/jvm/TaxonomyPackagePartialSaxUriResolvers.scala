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

package eu.cdevreeze.tqa2.docbuilder.jvm

import java.io.File
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import eu.cdevreeze.tqa2.docbuilder.SimpleCatalog
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.node.saxon.SaxonNodes
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.XdmNode

import scala.jdk.CollectionConverters._

/**
 * Partial SAX URI resolvers specifically for a taxonomy package
 * (see https://www.xbrl.org/Specification/taxonomy-package/REC-2016-04-19/taxonomy-package-REC-2016-04-19.html).
 *
 * @author Chris de Vreeze
 */
object TaxonomyPackagePartialSaxUriResolvers {

  /**
   * Creates a PartialSaxUriResolver from a ZIP file (as ZipFile instance) containing a taxonomy package with XML catalog.
   * Consider managing the ZipFile resource with the scala.util.Using API.
   */
  def forTaxonomyPackage(taxonomyPackageZipFile: ZipFile): PartialSaxUriResolver = {
    val catalog: SimpleCatalog = parseCatalog(taxonomyPackageZipFile)

    PartialSaxUriResolvers.forZipFileUsingCatalog(taxonomyPackageZipFile, catalog)
  }

  private def parseCatalog(zipFile: ZipFile): SimpleCatalog = {
    val catalogEntry: ZipEntry = zipFile
      .entries()
      .asScala
      .find(entry => toRelativeUri(entry, dummyDirectory).toString.endsWith("/META-INF/catalog.xml"))
      .getOrElse(sys.error(s"No META-INF/catalog.xml found in taxonomy package ZIP file ${zipFile.getName}"))

    val catalogEntryRelativeUri: URI = toRelativeUri(catalogEntry, dummyDirectory).ensuring(!_.isAbsolute)

    val processor = new Processor(false)
    val underlyingDoc: XdmNode = processor.newDocumentBuilder().build(new StreamSource(zipFile.getInputStream(catalogEntry)))
    val catalogRootElem: SaxonNodes.Elem = SaxonDocument(underlyingDoc).documentElement

    SimpleCatalog.fromElem(catalogRootElem).copy(xmlBaseAttrOption = Some(catalogEntryRelativeUri))
  }

  private def toRelativeUri(zipEntry: ZipEntry, zipFileParent: File): URI = {
    val adaptedZipEntryFile = new File(zipFileParent, zipEntry.getName)
    val adaptedZipEntryUri = adaptedZipEntryFile.toURI
    val zipFileParentUri = URI.create(returnWithTrailingSlash(zipFileParent.toURI))
    val relativeZipEntryUri = zipFileParentUri.relativize(adaptedZipEntryUri)
    require(!relativeZipEntryUri.isAbsolute, s"Not a relative URI: $relativeZipEntryUri")

    relativeZipEntryUri
  }

  private def returnWithTrailingSlash(uri: URI): String = {
    val s = uri.toString
    if (s.endsWith("/")) s else s + "/"
  }

  private val dummyDirectory = new File("/dummyRoot")
}
