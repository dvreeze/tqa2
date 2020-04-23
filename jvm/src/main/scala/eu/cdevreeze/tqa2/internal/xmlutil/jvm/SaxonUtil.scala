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

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URI

import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.jaxp.SaxEventProducers
import eu.cdevreeze.yaidom2.node.saxon
import eu.cdevreeze.yaidom2.queryapi.BackingDocumentApi
import eu.cdevreeze.yaidom2.queryapi.BackingNodes
import eu.cdevreeze.yaidom2.queryapi.ScopedNodes
import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.Serializer
import org.xml.sax.ContentHandler

/**
 * Helper functions for converting to Saxon wrapper documents, etc. Should (mostly) be part of yaidom2 instead.
 *
 * @author Chris de Vreeze
 */
object SaxonUtil {

  def convertToSaxonDocument(doc: BackingDocumentApi, processor: Processor): saxon.Document = {
    doc match {
      case doc: saxon.Document => doc
      case doc =>
        val saxonDocBuilder = processor.newDocumentBuilder()
        saxonDocBuilder.setBaseURI(doc.docUriOption.getOrElse(new URI("")))
        val buildingContentHandler = saxonDocBuilder.newBuildingContentHandler()

        SaxEventProducers.produceEventsForDocument(doc, buildingContentHandler)
        saxon.Document(buildingContentHandler.getDocumentNode)
    }
  }

  def convertToSaxonDocument(elem: BackingNodes.Elem, processor: Processor): saxon.Document = {
    convertToSaxonDocument(elem.docUri, elem, processor)
  }

  def convertToSaxonDocument(docUri: URI, elem: ScopedNodes.Elem, processor: Processor): saxon.Document = {
    val saxonDocBuilder = processor.newDocumentBuilder()
    saxonDocBuilder.setBaseURI(docUri)
    val buildingContentHandler = saxonDocBuilder.newBuildingContentHandler()

    produceEventsForRootElem(elem, buildingContentHandler)
    saxon.Document(buildingContentHandler.getDocumentNode)
  }

  /**
   * Alternative to method SaxonProducers.produceEventsForElem for root elements that works for Saxon, in that
   * startDocument and endDocument calls are made.
   */
  def produceEventsForRootElem(elem: ScopedNodes.Elem, contentHandler: ContentHandler): Unit = {
    contentHandler.startDocument()
    SaxEventProducers.produceEventsForElem(elem, Scope.Empty, contentHandler)
    contentHandler.endDocument()
  }

  /**
   * Serializes the given SaxonDocument, adding no indentation by itself.
   */
  def serialize(saxonDoc: saxon.SaxonDocument, file: File): Unit = {
    serialize(saxonDoc, new FileOutputStream(file))
  }

  /**
   * Serializes the given SaxonDocument, adding no indentation by itself. Encoding UTF-8 is used.
   * This method closes the OutputStream at the end.
   */
  def serialize(saxonDoc: saxon.SaxonDocument, os: OutputStream): Unit = {
    val encoding = "utf-8"

    // Rather weird serialization. No indent, but respecting the "ignorable" whitespace in the DOM tree.
    // Also, making sure there is a newline after the XML declaration.

    os.write(s"""<?xml version="1.0" encoding="$encoding"?>\n""".getBytes(encoding))
    os.flush()
    val serializer = saxonDoc.newSerializer(os)
    serializer.setOutputProperty(Serializer.Property.METHOD, "xml")
    serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes")
    serializer.setOutputProperty(Serializer.Property.ENCODING, encoding)
    serializer.setOutputProperty(Serializer.Property.INDENT, "no")
    serializer.serializeNode(saxonDoc.xdmNode)
    serializer.close()
    os.close()
  }
}
