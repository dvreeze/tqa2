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

package eu.cdevreeze.tqa2.locfreetaxonomy

import java.net.URI

import eu.cdevreeze.tqa2.docbuilder.UriConverter
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxUriResolver
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxUriResolvers

/**
 * Support for creating URI resolvers (on the JVM) for testing.
 *
 * @author Chris de Vreeze
 */
object UriResolverTestUtil {

  def getUriConverterForClasspath: UriConverter = {
    def convertUri(uri: URI): URI = {
      require(!uri.isAbsolute, s"Expected relative URI, to be resolved against the root of the classpath, but got '$uri'")

      UriResolverTestUtil.getClass.getResource("/" + uri.toString).toURI
    }

    convertUri
  }

  def getUriResolverForClasspath: SaxUriResolver = {
    SaxUriResolvers.fromUriConverter(getUriConverterForClasspath)
  }
}
