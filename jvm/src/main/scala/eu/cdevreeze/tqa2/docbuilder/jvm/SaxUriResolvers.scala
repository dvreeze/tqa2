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

import org.xml.sax.InputSource

/**
 * Support for creating (SAX) URI resolvers on the JVM.
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

  // TODO ...
}
