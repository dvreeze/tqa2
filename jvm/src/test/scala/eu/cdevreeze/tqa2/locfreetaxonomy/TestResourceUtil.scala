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

import eu.cdevreeze.tqa2.docbuilder.UriConverters
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxUriResolvers
import eu.cdevreeze.tqa2.docbuilder.jvm.saxon.SaxonDocumentBuilder
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import net.sf.saxon.s9api.Processor

/**
 * Support for converting classpath URIs (on the JVM) for testing, and for building documents from such relative URIs.
 *
 * @author Chris de Vreeze
 */
object TestResourceUtil {

  /**
   * Converts a relative URI, interpreted as relative to the root of the classpath, to an absolute "file" URI.
   */
  def convertClasspathUriToAbsoluteUri(relativeFilePath: URI): URI = {
    require(!relativeFilePath.isAbsolute,
            s"Expected relative URI, to be resolved against the root of the classpath, but got '$relativeFilePath'")

    TestResourceUtil.getClass.getResource("/" + relativeFilePath.toString).toURI
  }

  /**
   * Builds a Saxon document from the given relative URI, interpreted as relative to the root of the classpath.
   */
  def buildSaxonDocumentFromClasspathResource(relativeFilePath: URI, processor: Processor): SaxonDocument = {
    val uri: URI = TestResourceUtil.convertClasspathUriToAbsoluteUri(relativeFilePath)
    val docBuilder = SaxonDocumentBuilder(processor, SaxUriResolvers.fromUriConverter(UriConverters.identity))
    docBuilder.build(uri)
  }
}
