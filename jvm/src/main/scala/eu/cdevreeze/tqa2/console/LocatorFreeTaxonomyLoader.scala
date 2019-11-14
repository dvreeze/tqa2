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
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ParentChildRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyBase
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder.DefaultDtsUriCollector
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder.DtsUriCollector
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.node.saxon.SaxonDocument
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api.Processor

/**
 * Locator-free taxonomy loader, showing some info about the loaded taxonomy. It can be used to "debug" the locator-free model,
 * and also to validate locator-free taxonomies themselves. The program expects a local mirror, with a directory for the host name
 * (no port), which has a sub-tree for the path.
 *
 * TODO Remove this program once it is no longer needed here.
 *
 * @author Chris de Vreeze
 */
object LocatorFreeTaxonomyLoader {

  // TODO Use catalogs, ZIP files, HTTP versus HTTPS, multi-document entrypoints, etc.

  private val processor = new Processor(false)

  def main(args: Array[String]): Unit = {
    require(args.length == 2, s"Usage: LocatorFreeTaxonomyLoader <taxo root dir> <entrypoint URI>")

    val start = System.currentTimeMillis()

    val taxoRootDir = new File(args(0))
    require(taxoRootDir.isDirectory, s"Not a directory: '$taxoRootDir'")

    val entrypointUri: URI = URI.create(args(1))

    val dtsUriCollector: DtsUriCollector = new DefaultDtsUriCollector(uri => build(uri, taxoRootDir.toURI))

    println(s"Finding DTS document URIs ...") // scalastyle:off

    val dtsDocUris: Set[URI] = dtsUriCollector.findAllDtsUris(Set(entrypointUri))

    println(s"Parsing DTS documents ...") // scalastyle:off

    val rootElems: Seq[TaxonomyElem] = dtsDocUris.toSeq.sortBy(_.toString).map(u => build(u, taxoRootDir.toURI))

    println(s"Building TaxonomyBase ...") // scalastyle:off

    val taxoBase: TaxonomyBase = TaxonomyBase.build(rootElems, SubstitutionGroupMap.Empty)

    println(s"Number of documents in the TaxonomyBase: ${taxoBase.rootElems.size}") // scalastyle:off
    println(s"Building BasicTaxonomy ...") // scalastyle:off

    val taxo: BasicTaxonomy = BasicTaxonomy.build(taxoBase, new DefaultRelationshipFactory)

    println(s"Number of documents in the BasicTaxonomy: ${taxoBase.rootElems.size}") // scalastyle:off
    println(s"Number of relationships: ${taxo.relationships.size}") // scalastyle:off

    val hasHypercubes: Seq[HasHypercubeRelationship] = taxo.findAllHasHypercubeRelationships
    val hasHypercubeElrs: Set[String] = hasHypercubes.map(_.elr).toSet

    println() // scalastye:off
    println(s"Number of dimensional (has-hypercube) ELRs: ${hasHypercubeElrs.size}") // scalastyle:off

    val parentChildren: Seq[ParentChildRelationship] = taxo.findAllParentChildRelationships
    val parentChildElrs: Set[String] = parentChildren.map(_.elr).toSet

    println(s"Number of parent-child relationship ELRs: ${parentChildElrs.size}") // scalastyle:off

    println(s"Number of parent-child relationship ELRs that are not dimensional ELRs: ${parentChildElrs.diff(hasHypercubeElrs).size}") // scalastyle:off
    println(s"Number of dimensional ELRs that are not parent-child relationship ELRs: ${hasHypercubeElrs.diff(parentChildElrs).size}") // scalastyle:off

    val dimensionalConcepts: Set[EName] = taxo.computeHasHypercubeInheritanceOrSelf.keySet

    val items: Set[EName] = taxo.findAllItemDeclarations.map(_.targetEName).toSet

    println(s"Number of dimensional concepts that are not items in the taxo: ${dimensionalConcepts.diff(items).size}") // scalastyle:off
    println(s"Number of items in the taxo that are not dimensional concepts: ${items.diff(dimensionalConcepts).size}") // scalastyle:off

    val end = System.currentTimeMillis()

    println() // scalastyle:off
    println(s"The program took ${end - start} ms") // scalastyle:off
  }

  private def build(uri: URI, taxoRootDirAsUri: URI): TaxonomyElem = {
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
