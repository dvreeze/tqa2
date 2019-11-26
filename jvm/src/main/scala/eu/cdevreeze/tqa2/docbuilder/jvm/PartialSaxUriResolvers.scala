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
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import scala.jdk.CollectionConverters._

import eu.cdevreeze.tqa2.docbuilder.PartialUriConverter
import eu.cdevreeze.tqa2.docbuilder.PartialUriConverters
import eu.cdevreeze.tqa2.docbuilder.SimpleCatalog
import org.xml.sax.InputSource

/**
 * Support for creating partial URI resolvers on the JVM.
 *
 * @author Chris de Vreeze
 */
object PartialSaxUriResolvers {

  /**
   * Creates a PartialSaxUriResolver from a partial URI converter. Typically the URI converter converts HTTP(S) URIs
   * to file protocol URIs, but it must in any case return only absolute URIs.
   *
   * The created PartialSaxUriResolver is defined for the same URIs as the input PartialUriConverter.
   */
  def fromPartialUriConverter(partialUriConverter: PartialUriConverter): PartialSaxUriResolver = {
    def resolveUri(uri: URI): Option[SaxInputSource] = {
      val mappedUriOption = partialUriConverter(uri)

      mappedUriOption.map { mappedUri =>
        require(mappedUri.isAbsolute, s"Cannot resolve relative URI '$mappedUri'")

        val is: InputStream =
          if (mappedUri.getScheme == "file") new FileInputStream(new File(mappedUri)) else mappedUri.toURL.openStream()

        new SaxInputSource(new InputSource(is))
      }
    }

    resolveUri
  }

  /**
   * Creates a PartialSaxUriResolver for a ZIP file, using the given partial URI converter. This partial URI
   * converter must return only relative URIs.
   *
   * The created PartialSaxUriResolver is defined for the same URIs as the input PartialUriConverter.
   *
   * The ZIP file should be closed after use (typically when the taxonomy has been loaded), thus closing
   * all its entry input streams.
   */
  def forZipFile(zipFile: ZipFile, partialUriConverter: PartialUriConverter): PartialSaxUriResolver = {
    val zipEntriesByRelativeUri: Map[URI, ZipEntry] = computeZipEntryMap(zipFile)

    def resolveUri(uri: URI): Option[SaxInputSource] = {
      val mappedUriOption = partialUriConverter(uri)

      mappedUriOption.map { mappedUri =>
        require(!mappedUri.isAbsolute, s"Cannot resolve absolute URI '$mappedUri'")

        val optionalZipEntry: Option[ZipEntry] = zipEntriesByRelativeUri.get(mappedUri)

        require(optionalZipEntry.isDefined, s"Missing ZIP entry in ZIP file $zipFile with URI $mappedUri")

        val is = zipFile.getInputStream(optionalZipEntry.get)

        new SaxInputSource(new InputSource(is))
      }
    }

    resolveUri
  }

  /**
   * Returns `fromPartialUriConverter(PartialUriConverters.fromCatalog(catalog))`.
   */
  def fromCatalog(catalog: SimpleCatalog): PartialSaxUriResolver = {
    fromPartialUriConverter(PartialUriConverters.fromCatalog(catalog))
  }

  /**
   * Returns `forZipFile(zipFile, PartialUriConverters.fromCatalog(catalog))`.
   */
  def forZipFileUsingCatalog(zipFile: ZipFile, catalog: SimpleCatalog): PartialSaxUriResolver = {
    forZipFile(zipFile, PartialUriConverters.fromCatalog(catalog))
  }

  private def computeZipEntryMap(zipFile: ZipFile): Map[URI, ZipEntry] = {
    val zipEntries = zipFile.entries().asScala.toIndexedSeq

    val zipFileParent = dummyDirectory

    zipEntries.map(e => toRelativeUri(e, zipFileParent) -> e).toMap
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
