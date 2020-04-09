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

package eu.cdevreeze.tqa2.internal.xmlutil.jvm

import java.io.StringReader
import java.io.StringWriter

import eu.cdevreeze.tqa2.internal.xmlutil.NodeBuilderUtil
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.jaxp.SaxEventProducers
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api.Processor

/**
 * Helper functions for editing nodebuilder elements. For example, pushing up namespace declarations, prettifying, removing unused
 * namespaces, etc.
 *
 * @author Chris de Vreeze
 */
final class JvmNodeBuilderUtil(namespacePrefixMapper: NamespacePrefixMapper, documentENameExtractor: DocumentENameExtractor)
    extends NodeBuilderUtil(namespacePrefixMapper, documentENameExtractor) {

  private val saxonProcessor: Processor = new Processor(false)

  def prettify(elem: nodebuilder.Elem): nodebuilder.Elem = {
    // See https://self-learning-java-tutorial.blogspot.com/2018/03/pretty-print-xml-string-in-java.html
    val tf = TransformerFactory.newInstance().asInstanceOf[SAXTransformerFactory]
    val indent = 2
    tf.setAttribute("indent-number", indent)

    val serializeHandler: TransformerHandler = tf.newTransformerHandler()
    serializeHandler.getTransformer.setOutputProperty(OutputKeys.INDENT, "yes")
    val sw: StringWriter = new StringWriter()
    serializeHandler.setResult(new StreamResult(sw))

    SaxEventProducers.produceEventsForElem(elem, Scope.Empty, serializeHandler)
    val prettifiedXmlString: String = sw.toString

    val saxonDoc: SaxonDocument =
      SaxonDocument(saxonProcessor.newDocumentBuilder().build(new StreamSource(new StringReader(prettifiedXmlString))))
    nodebuilder.Elem.from(saxonDoc.documentElement)
  }
}

object JvmNodeBuilderUtil {

  def apply(namespacePrefixMapper: NamespacePrefixMapper, documentENameExtractor: DocumentENameExtractor): JvmNodeBuilderUtil = {
    new JvmNodeBuilderUtil(namespacePrefixMapper, documentENameExtractor)
  }
}
