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

import java.io.ByteArrayInputStream
import java.net.URI
import java.nio.charset.Charset

import scala.util.Try

import eu.cdevreeze.yaidom2.node.resolved
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import eu.cdevreeze.yaidom2.node.simple
import eu.cdevreeze.yaidom2.queryapi.ScopedNodes
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.XdmNode
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

/**
 * Test of simple catalogs. Several example catalogs have been used from the XBRL Taxonomy Packages standard.
 *
 * @author Chris de Vreeze
 */
class SimpleCatalogTest extends AnyFunSuite {

  import SimpleCatalog.UriRewrite

  test("A simple catalog can be created with the apply method") {
    val simpleCatalog: SimpleCatalog = SimpleCatalog(
      Some(URI.create("../")),
      Seq(
        UriRewrite(None, "http://www.example.com/part1/2015-01-01/", "part1/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/", "part2/2015-01-01/")
      )
    )

    simpleCatalog.xmlBaseAttrOption should be(Some(URI.create("../")))

    simpleCatalog.uriRewrites.map(_.xmlBaseAttrOption).distinct should be(Seq(None))
    simpleCatalog.uriRewrites.map(_.uriStartString).distinct should be(
      Seq("http://www.example.com/part1/2015-01-01/", "http://www.example.com/part2/2015-01-01/"))
    simpleCatalog.uriRewrites.map(_.rewritePrefix).distinct should be(Seq("part1/2015-01-01/", "part2/2015-01-01/"))
  }

  test("A simple catalog can be created from a Map") {
    val simpleCatalog: SimpleCatalog = SimpleCatalog.from(
      Map(
        "http://www.example.com/part1/2015-01-01/" -> "../part1/2015-01-01/",
        "http://www.example.com/part2/2015-01-01/" -> "../part2/2015-01-01/"
      ))

    simpleCatalog.xmlBaseAttrOption should be(None)

    simpleCatalog.uriRewrites.map(_.xmlBaseAttrOption).distinct should be(Seq(None))
    simpleCatalog.uriRewrites.map(_.uriStartString).distinct should be(
      Seq("http://www.example.com/part1/2015-01-01/", "http://www.example.com/part2/2015-01-01/"))
    simpleCatalog.uriRewrites.map(_.rewritePrefix).distinct should be(Seq("../part1/2015-01-01/", "../part2/2015-01-01/"))
  }

  test("A simple catalog can be created from XML") {
    val xmlString =
      """<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" xml:base="../">
      |  <rewriteURI uriStartString="http://www.example.com/part1/2015-01-01/" rewritePrefix="part1/2015-01-01/"/>
      |  <rewriteURI uriStartString="http://www.example.com/part2/2015-01-01/" rewritePrefix="part2/2015-01-01/"/>
      |</catalog>""".stripMargin

    val doc = buildDocumentFromXmlString(xmlString)
    val simpleCatalog: SimpleCatalog = SimpleCatalog.fromElem(doc.documentElement)

    simpleCatalog.xmlBaseAttrOption should be(Some(URI.create("../")))

    simpleCatalog.uriRewrites.map(_.xmlBaseAttrOption).distinct should be(Seq(None))
    simpleCatalog.uriRewrites.map(_.uriStartString).distinct should be(
      Seq("http://www.example.com/part1/2015-01-01/", "http://www.example.com/part2/2015-01-01/"))
    simpleCatalog.uriRewrites.map(_.rewritePrefix).distinct should be(Seq("part1/2015-01-01/", "part2/2015-01-01/"))
  }

  test("A simple catalog can be created from XML and serialized to the same XML") {
    val xmlString =
      """<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" xml:base="../">
        |  <rewriteURI uriStartString="http://www.example.com/part1/2015-01-01/" rewritePrefix="part1/2015-01-01/"/>
        |  <rewriteURI uriStartString="http://www.example.com/part2/2015-01-01/" rewritePrefix="part2/2015-01-01/"/>
        |</catalog>""".stripMargin

    val doc = buildDocumentFromXmlString(xmlString)
    val simpleCatalog: SimpleCatalog = SimpleCatalog.fromElem(doc.documentElement)

    val serializedCatalog: simple.Elem = simpleCatalog.toElem

    toResolvedElem(serializedCatalog) should be(toResolvedElem(doc.documentElement))

    val simpleCatalog2: SimpleCatalog = SimpleCatalog.fromElem(serializedCatalog)

    simpleCatalog2 should be(simpleCatalog)
  }

  test("A simple catalog can be serialized to XML and deserialized again to the same simple catalog") {
    val simpleCatalog: SimpleCatalog = SimpleCatalog(
      Some(URI.create("../")),
      Seq(
        UriRewrite(Some(URI.create("./")), "http://www.example.com/part1/2015-01-01/", "part1/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/", "part2/2015-01-01/")
      )
    )

    val simpleCatalog2: SimpleCatalog = SimpleCatalog.fromElem(simpleCatalog.toElem)

    simpleCatalog2 should be(simpleCatalog)
  }

  test("A simple catalog can be simplified to one without XML Base attributes") {
    val simpleCatalog1: SimpleCatalog = SimpleCatalog(
      Some(URI.create("../")),
      Seq(
        UriRewrite(Some(URI.create("./")), "http://www.example.com/part1/2015-01-01/", "part1/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/", "part2/2015-01-01/")
      )
    )

    val simpleCatalog2: SimpleCatalog = SimpleCatalog(
      None,
      Seq(
        UriRewrite(None, "http://www.example.com/part1/2015-01-01/", "../part1/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/", "../part2/2015-01-01/")
      )
    )

    simpleCatalog2.toMap should be(simpleCatalog1.toMap)
  }

  test("A simple catalog can be used to map URIs") {
    val simpleCatalog: SimpleCatalog = SimpleCatalog(
      Some(URI.create("../")),
      Seq(
        UriRewrite(Some(URI.create("./")), "http://www.example.com/part1/2015-01-01/", "part1/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/", "part2/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/03/", "part2/2015-01-01/0003/")
      )
    )

    simpleCatalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/test.xml")) should be(
      Some(URI.create("../part1/2015-01-01/test.xml")))
    simpleCatalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/01/test.xml")) should be(
      Some(URI.create("../part1/2015-01-01/01/test.xml")))
    simpleCatalog.findMappedUri(URI.create("http://www.example.com/part2/2015-01-01/02/test.xml")) should be(
      Some(URI.create("../part2/2015-01-01/02/test.xml")))
    simpleCatalog.findMappedUri(URI.create("http://www.example.com/part2/2015-01-01/03/test.xml")) should be(
      Some(URI.create("../part2/2015-01-01/0003/test.xml")))
    simpleCatalog.findMappedUri(URI.create("http://www.example.com/part3/2015-01-01/test.xml")) should be(None)
    simpleCatalog.findMappedUri(URI.create("part1/2015-01-01/test.xml")) should be(None)

    simpleCatalog.getMappedUri(URI.create("http://www.example.com/part1/2015-01-01/test.xml")) should be(
      URI.create("../part1/2015-01-01/test.xml"))
    simpleCatalog.getMappedUri(URI.create("http://www.example.com/part1/2015-01-01/01/test.xml")) should be(
      URI.create("../part1/2015-01-01/01/test.xml"))
    simpleCatalog.getMappedUri(URI.create("http://www.example.com/part2/2015-01-01/02/test.xml")) should be(
      URI.create("../part2/2015-01-01/02/test.xml"))
    simpleCatalog.getMappedUri(URI.create("http://www.example.com/part2/2015-01-01/03/test.xml")) should be(
      URI.create("../part2/2015-01-01/0003/test.xml"))
    Try(simpleCatalog.getMappedUri(URI.create("http://www.example.com/part3/2015-01-01/test.xml"))).toOption should be(None)
    Try(simpleCatalog.getMappedUri(URI.create("part1/2015-01-01/test.xml"))).toOption should be(None)
  }

  test("Mapping URIs directly using the simple catalog or indirectly resolved via its 'Map' gives the same results") {
    val simpleCatalog: SimpleCatalog = SimpleCatalog(
      Some(URI.create("../")),
      Seq(
        UriRewrite(Some(URI.create("./")), "http://www.example.com/part1/2015-01-01/", "part1/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/", "part2/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/03/", "part2/2015-01-01/0003/")
      )
    )

    val catalogAsMapping: Map[String, String] = simpleCatalog.toMap

    def findMappedUri(uri: URI): Option[URI] = {
      val startStringOption: Option[String] = catalogAsMapping.keySet.toSeq.sortBy(_.length).reverse.find(s => uri.toString.startsWith(s))

      startStringOption.flatMap { startString =>
        val rewritePrefixOption: Option[String] = catalogAsMapping.get(startString)

        rewritePrefixOption.map(rewrite => URI.create(rewrite).resolve(URI.create(startString).relativize(uri)))
      }
    }

    val uris: Seq[URI] = Seq(
      URI.create("http://www.example.com/part1/2015-01-01/test.xml"),
      URI.create("http://www.example.com/part1/2015-01-01/01/test.xml"),
      URI.create("http://www.example.com/part2/2015-01-01/02/test.xml"),
      URI.create("http://www.example.com/part2/2015-01-01/03/test.xml"),
      URI.create("http://www.example.com/part3/2015-01-01/test.xml"),
      URI.create("part1/2015-01-01/test.xml")
    )

    uris.foreach { uri =>
      simpleCatalog.findMappedUri(uri) should be(findMappedUri(uri))
    }
  }

  test("An invertible simple catalog can be inverted correctly") {
    val simpleCatalog: SimpleCatalog = SimpleCatalog(
      None,
      Seq(
        UriRewrite(None, "http://www.example.com/part1/2015-01-01/", "file:/path/to/www.example.com/part1/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/", "file:/path/to/www.example.com/part2/2015-01-01/"),
        UriRewrite(None, "http://www.xbrl.org/", "file:/path/to/www.xbrl.org/"),
        UriRewrite(None, "http://www.w3.org/", "file:/path/to/www.w3.org/"),
      )
    )

    val reverseCatalog: SimpleCatalog = simpleCatalog.reverse

    reverseCatalog.getMappedUri(URI.create("file:/path/to/www.example.com/part2/2015-01-01/01/test.xml")) should be(
      URI.create("http://www.example.com/part2/2015-01-01/01/test.xml")
    )
  }

  test("An invertible simple catalog can be inverted twice, getting back the same simple catalog") {
    val simpleCatalog: SimpleCatalog = SimpleCatalog(
      None,
      Seq(
        UriRewrite(None, "http://www.example.com/part1/2015-01-01/", "file:/path/to/www.example.com/part1/2015-01-01/"),
        UriRewrite(None, "http://www.example.com/part2/2015-01-01/", "file:/path/to/www.example.com/part2/2015-01-01/"),
        UriRewrite(None, "http://www.xbrl.org/", "file:/path/to/www.xbrl.org/"),
        UriRewrite(None, "http://www.w3.org/", "file:/path/to/www.w3.org/"),
      )
    )

    val reverseCatalog: SimpleCatalog = simpleCatalog.reverse

    reverseCatalog.reverse should be(simpleCatalog)
  }

  private val processor = new Processor(false)

  private def buildDocumentFromXmlString(xmlString: String): simple.Document = {
    val source = new StreamSource(new ByteArrayInputStream(xmlString.getBytes(Charset.forName("UTF-8"))))
    val xdmNode: XdmNode = processor.newDocumentBuilder().build(source)
    val saxonDoc = SaxonDocument(xdmNode)

    simple.Document.from(saxonDoc)
  }

  private def toResolvedElem(elem: ScopedNodes.Elem): resolved.Elem = {
    resolved.Elem.from(elem).removeAllInterElementWhitespace
  }
}
