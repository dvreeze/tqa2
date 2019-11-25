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

package eu.cdevreeze.tqa2.docbuilder

import java.net.URI

/**
 * Support for creating URI converters.
 *
 * @author Chris de Vreeze
 */
object UriConverters {

  def identity: UriConverter = PartialUriConverters.identity.andThen(_.get)

  /**
   * Returns the URI converter that for each input URI tries all given partial URI converters until a
   * matching one is found, returning the conversion result. If for an URI no matching partial URI
   * converter is found, an exception is thrown.
   */
  def fromPartialUriConverters(partialUriConverters: Seq[PartialUriConverter]): UriConverter = {
    require(partialUriConverters.nonEmpty, s"No partial URI converters given")

    def convertUri(uri: URI): URI = {
      partialUriConverters.view.flatMap(_(uri)).headOption.getOrElse(sys.error(s"Could not convert URI $uri"))
    }

    convertUri
  }

  /**
   * Returns the URI converter that for each input URI tries all given partial URI converters until a
   * matching one is found, returning the conversion result. If for an URI no matching partial URI
   * converter is found, the URI itself is returned.
   */
  def fromPartialUriConvertersOrIdentity(partialUriConverters: Seq[PartialUriConverter]): UriConverter = {
    require(partialUriConverters.nonEmpty, s"No partial URI converters given")

    def convertUri(uri: URI): URI = {
      partialUriConverters.view.flatMap(_(uri)).headOption.getOrElse(uri)
    }

    convertUri
  }

  /**
   * Like `PartialUriConverters.fromCatalog(catalog)`, but otherwise throwing an exception.
   */
  def fromCatalog(catalog: SimpleCatalog): UriConverter = {
    fromPartialUriConverters(Seq(PartialUriConverters.fromCatalog(catalog)))
  }

  /**
   * Like `PartialUriConverters.fromCatalog(catalog)`, but otherwise the identity function.
   */
  def fromCatalogOrIdentity(catalog: SimpleCatalog): UriConverter = {
    fromPartialUriConvertersOrIdentity(Seq(PartialUriConverters.fromCatalog(catalog)))
  }
}
