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
import java.util.zip.ZipFile

import eu.cdevreeze.tqa2.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa2.docbuilder.UriConverter
import eu.cdevreeze.tqa2.docbuilder.UriConverters
import org.xml.sax.InputSource

import scala.util.Try

/**
 * Support for creating (SAX) URI resolvers on the JVM.
 *
 * Note that this singleton object only has fundamental methods fromPartialSaxUriResolversWithoutFallback and fromPartialSaxUriResolversWithFallback,
 * and, arguably, methods fromUriConverter and forZipFile as well.
 *
 * @author Chris de Vreeze
 */
object SaxUriResolvers {

  /**
   * Returns the URI resolver that for each input URI tries all given partial URI resolvers until a
   * matching one is found, returning the InputSource resolution result. If for an URI no matching partial URI
   * resolver is found, an exception is thrown.
   */
  def fromPartialSaxUriResolversWithoutFallback(partialUriResolvers: Seq[PartialSaxUriResolver]): SaxUriResolver = {
    require(partialUriResolvers.nonEmpty, s"No partial URI resolvers given")

    def resolveUri(uri: URI): SaxInputSource = {
      partialUriResolvers.view.flatMap(_(uri)).headOption.getOrElse(sys.error(s"Could not resolve URI $uri"))
    }

    resolveUri
  }

  /**
   * Returns the URI resolver that for each input URI tries all given partial URI resolvers until a
   * matching one is found, returning the InputSource resolution result. If for an URI no matching partial URI
   * resolver is found, the URI itself is "opened" as InputSource.
   */
  def fromPartialSaxUriResolversWithFallback(partialUriResolvers: Seq[PartialSaxUriResolver]): SaxUriResolver = {
    require(partialUriResolvers.nonEmpty, s"No partial URI resolvers given")

    def resolveUri(uri: URI): SaxInputSource = {
      partialUriResolvers.view.flatMap(_(uri)).headOption.getOrElse {
        val is: InputStream = if (uri.getScheme == "file") new FileInputStream(new File(uri)) else uri.toURL.openStream()

        new SaxInputSource(new InputSource(is))
      }
    }

    resolveUri
  }

  /**
   * Returns `fromPartialSaxUriResolversWithoutFallback(Seq(partialUriResolver))`.
   */
  def fromPartialSaxUriResolverWithoutFallback(partialUriResolver: PartialSaxUriResolver): SaxUriResolver = {
    fromPartialSaxUriResolversWithoutFallback(Seq(partialUriResolver))
  }

  /**
   * Returns `fromPartialSaxUriResolversWithFallback(Seq(partialUriResolver))`.
   */
  def fromPartialSaxUriResolverWithFallback(partialUriResolver: PartialSaxUriResolver): SaxUriResolver = {
    fromPartialSaxUriResolversWithFallback(Seq(partialUriResolver))
  }

  /**
   * Like `PartialSaxUriResolvers.fromPartialUriConverter(liftedUriConverter).andThen(_.get)`.
   */
  def fromUriConverter(uriConverter: UriConverter): SaxUriResolver = {
    val delegate: PartialSaxUriResolver =
      PartialSaxUriResolvers.fromPartialUriConverter(uriConverter.andThen(u => Some(u)))

    delegate.andThen(_.ensuring(_.isDefined).get)
  }

  /**
   * Like `PartialSaxUriResolvers.forZipFile(zipFile, liftedUriConverter).andThen(_.get)`, .
   */
  def forZipFile(zipFile: ZipFile, uriConverter: UriConverter): SaxUriResolver = {
    val delegate: PartialSaxUriResolver =
      PartialSaxUriResolvers.forZipFile(zipFile, uriConverter.andThen(u => Some(u)))

    delegate.andThen(_.ensuring(_.isDefined).get)
  }

  /**
   * Returns `fromUriConverter(UriConverters.fromCatalogOrIdentity(catalog))`.
   */
  def fromCatalogWithFallback(catalog: SimpleCatalog): SaxUriResolver = {
    fromUriConverter(UriConverters.fromCatalogOrIdentity(catalog))
  }

  /**
   * Returns `fromUriConverter(UriConverters.fromCatalog(catalog))`.
   */
  def fromCatalogWithoutFallback(catalog: SimpleCatalog): SaxUriResolver = {
    fromUriConverter(UriConverters.fromCatalog(catalog))
  }

  /**
   * Returns an URI resolver that expects all files to be found in a local mirror, with the host name
   * of the URI mirrored under the given root directory. The protocol (HTTP or HTTPS) is not represented in
   * the local mirror.
   */
  def fromLocalMirrorRootDirectory(rootDir: File): SaxUriResolver = {
    require(rootDir.isDirectory, s"Not a directory: $rootDir")
    require(rootDir.isAbsolute, s"Not an absolute path: $rootDir")

    def convertUri(uri: URI): URI = {
      require(uri.getHost != null, s"Missing host name in URI '$uri'")
      require(uri.getScheme == "http" || uri.getScheme == "https", s"Not an HTTP(S) URI: '$uri'")

      val uriStart = returnWithTrailingSlash(new URI(uri.getScheme, uri.getHost, null, null))
      val rewritePrefix = returnWithTrailingSlash(new File(rootDir, uri.getHost).toURI)

      val catalog = SimpleCatalog.from(Map(uriStart -> rewritePrefix))

      val mappedUri = catalog.getMappedUri(uri)
      mappedUri
    }

    fromUriConverter(convertUri)
  }

  /**
   * Returns an URI resolver that expects all files to be found in a local mirror in a ZIP file, with the host name
   * of the URI mirrored under the given optional parent directory. The protocol (HTTP or HTTPS) is not represented in
   * the local mirror.
   */
  def forZipFileContainingLocalMirror(zipFile: ZipFile, parentPathOption: Option[URI]): SaxUriResolver = {
    require(parentPathOption.forall(!_.isAbsolute), s"Not a relative URI: ${parentPathOption.get}")

    def convertUri(uri: URI): URI = {
      require(uri.getHost != null, s"Missing host name in URI '$uri'")
      require(uri.getScheme == "http" || uri.getScheme == "https", s"Not an HTTP(S) URI: '$uri'")

      val uriStart = returnWithTrailingSlash(new URI(uri.getScheme, uri.getHost, null, null))

      val hostAsRelativeUri = URI.create(uri.getHost + "/")

      val rewritePrefix =
        parentPathOption
          .map(pp => URI.create(returnWithTrailingSlash(pp)).resolve(hostAsRelativeUri))
          .getOrElse(hostAsRelativeUri)
          .toString
          .ensuring(_.endsWith("/"))

      val catalog = SimpleCatalog.from(Map(uriStart -> rewritePrefix))

      val mappedUri = catalog.getMappedUri(uri)
      mappedUri
    }

    forZipFile(zipFile, convertUri)
  }

  /**
   * Returns `forZipFile(zipFile, UriConverters.fromCatalogOrIdentity(catalog))`.
   */
  def forZipFileUsingCatalogWithFallback(zipFile: ZipFile, catalog: SimpleCatalog): SaxUriResolver = {
    forZipFile(zipFile, UriConverters.fromCatalogOrIdentity(catalog))
  }

  /**
   * Returns `forZipFile(zipFile, UriConverters.fromCatalog(catalog))`.
   */
  def forZipFileUsingCatalogWithoutFallback(zipFile: ZipFile, catalog: SimpleCatalog): SaxUriResolver = {
    forZipFile(zipFile, UriConverters.fromCatalog(catalog))
  }

  val default: SaxUriResolver = fromUriConverter(UriConverters.identity)

  private def returnWithTrailingSlash(uri: URI): String = {
    val s = uri.toString
    if (s.endsWith("/")) s else s + "/"
  }

  /**
   * Extension of SaxUriResolver with resolution fallback capability.
   */
  implicit class WithResolutionFallback(val uriResolver: SaxUriResolver) extends AnyVal {

    /**
     * Adds a second SaxUriResolver as resolution fallback, if the first SaxUriResolver fails with an exception for some URI.
     */
    def withResolutionFallback(otherUriResolver: SaxUriResolver): SaxUriResolver = {
      def resolve(uri: URI): SaxInputSource = {
        Try(uriResolver(uri)).getOrElse(otherUriResolver(uri))
      }

      resolve
    }
  }
}
