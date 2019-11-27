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

package eu.cdevreeze.tqa2.docbuilder.jvm.saxon

import java.net.URI

import eu.cdevreeze.tqa2.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxInputSource
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api
import org.xml.sax.InputSource

/**
 * Saxon document builder, using an underlying Saxon DocumentBuilder and a URI resolver.
 *
 * @author Chris de Vreeze
 */
final class SaxonDocumentBuilder(val underlyingDocBuilder: s9api.DocumentBuilder, val uriResolver: URI => SaxInputSource)
    extends DocumentBuilder.UsingUriResolver {

  type BackingDoc = SaxonDocument

  def build(uri: URI): BackingDoc = {
    val is = uriResolver(uri).underlyingInputSource
    is.setSystemId(uri.toString)

    val src = convertInputSourceToSource(is).ensuring(_.getSystemId == uri.toString)

    val node = underlyingDocBuilder.build(src).ensuring(_.getUnderlyingNode.getSystemId == uri.toString)
    SaxonDocument(node)
  }

  private def convertInputSourceToSource(is: InputSource): Source = {
    assert(is.getSystemId != null) // scalastyle:ignore null

    if (is.getCharacterStream != null) { // scalastyle:ignore null
      val src = new StreamSource(is.getCharacterStream)
      Option(is.getSystemId).foreach(v => src.setSystemId(v))
      Option(is.getPublicId).foreach(v => src.setPublicId(v))
      src
    } else {
      require(is.getByteStream != null, s"Neither InputStream nor Reader set on InputSource") // scalastyle:ignore null
      val src = new StreamSource(is.getByteStream)
      Option(is.getSystemId).foreach(v => src.setSystemId(v))
      Option(is.getPublicId).foreach(v => src.setPublicId(v))
      // No encoding can be set
      src
    }
  }
}

object SaxonDocumentBuilder {

  def apply(underlyingDocBuilder: s9api.DocumentBuilder, uriResolver: URI => SaxInputSource): SaxonDocumentBuilder = {
    new SaxonDocumentBuilder(underlyingDocBuilder, uriResolver)
  }

  def apply(saxonProcessor: s9api.Processor, uriResolver: URI => SaxInputSource): SaxonDocumentBuilder = {
    new SaxonDocumentBuilder(saxonProcessor.newDocumentBuilder(), uriResolver)
  }
}
