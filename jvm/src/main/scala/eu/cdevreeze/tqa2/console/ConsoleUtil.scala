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

package eu.cdevreeze.tqa2.console

import java.io.File
import java.io.FileInputStream
import java.net.URI

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyBase
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder.DefaultDtsUriCollector
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder.DtsUriCollector
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api.Processor

/**
 * Console-program-specific bootstrapping support.
 *
 * @author Chris de Vreeze
 */
private[console] object ConsoleUtil {

  def createTaxonomy(entrypointUri: URI, taxoRootDir: File, processor: Processor): BasicTaxonomy = {
    val dtsUriCollector: DtsUriCollector = new DefaultDtsUriCollector(uri => build(uri, taxoRootDir.toURI, processor))

    println(s"Finding DTS document URIs (entrypoint: $entrypointUri) ...") // scalastyle:off

    val dtsDocUris: Set[URI] = dtsUriCollector.findAllDtsUris(Set(entrypointUri))

    println(s"Parsing DTS documents ...") // scalastyle:off

    val rootElems: Seq[TaxonomyElem] = dtsDocUris.toSeq.sortBy(_.toString).map(u => build(u, taxoRootDir.toURI, processor))

    println(s"Building TaxonomyBase ...") // scalastyle:off

    val taxoBase: TaxonomyBase = TaxonomyBase.build(rootElems, SubstitutionGroupMap.Empty)

    println(s"Number of documents in the TaxonomyBase: ${taxoBase.rootElems.size}") // scalastyle:off
    println(s"Building BasicTaxonomy ...") // scalastyle:off

    val taxo: BasicTaxonomy = BasicTaxonomy.build(taxoBase, new DefaultRelationshipFactory)

    println(s"Number of relationships: ${taxo.relationships.size}") // scalastyle:off
    taxo
  }

  private def build(uri: URI, taxoRootDirAsUri: URI, processor: Processor): TaxonomyElem = {
    val localUri: URI = rewriteUri(uri, taxoRootDirAsUri)

    val is = new FileInputStream(new File(localUri))
    val source = new StreamSource(is, uri.toString)
    val xdmNode = processor.newDocumentBuilder().build(source)
    val doc = SaxonDocument(xdmNode)

    TaxonomyElem(doc.documentElement)
  }

  private def rewriteUri(uri: URI, taxoRootDirAsUri: URI): URI = {
    require(uri.isAbsolute, s"Expected absolute URI, but got '$uri'")
    require(Option(uri.getFragment).isEmpty, s"Expected no fragment in the URI, but got URI '$uri")
    require(Set("http", "https").contains(uri.getScheme), s"Expected scheme 'http' or 'https', but got URI '$uri'")

    val host = uri.getHost
    val hostOnlyUri: URI = new URI(uri.getScheme, host, "/", null) // scalastyle:off null
    val relativeUri: URI = hostOnlyUri.relativize(uri).ensuring(!_.isAbsolute)

    val localHostDir = new File(taxoRootDirAsUri.resolve(host))
    require(localHostDir.isDirectory, s"Not a directory: $localHostDir")
    val localHostUri: URI = localHostDir.toURI
    localHostUri.resolve(relativeUri)
  }
}
