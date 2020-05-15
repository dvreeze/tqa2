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
import java.net.URI

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxUriResolver
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxUriResolvers
import eu.cdevreeze.tqa2.docbuilder.jvm.SimpleCatalogs
import eu.cdevreeze.tqa2.docbuilder.jvm.saxon.SaxonDocumentBuilder
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyDocument
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElems
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyBase
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder.DefaultDtsUriCollector
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder.DtsUriCollector
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.jvm.DefaultParallelRelationshipFactory
import net.sf.saxon.s9api.Processor

/**
 * Console-program-specific bootstrapping support.
 *
 * @author Chris de Vreeze
 */
private[console] object ConsoleUtil {

  def createTaxonomyForCombinedDts(entrypointsParentDirRemoteUri: URI, taxoRootDir: File, processor: Processor): BasicTaxonomy = {
    require(entrypointsParentDirRemoteUri.isAbsolute, s"Expected absolute URI, but got '$entrypointsParentDirRemoteUri'")
    require(
      Option(entrypointsParentDirRemoteUri.getFragment).isEmpty,
      s"Expected no fragment in the URI, but got URI '$entrypointsParentDirRemoteUri")
    require(
      Set("http", "https").contains(entrypointsParentDirRemoteUri.getScheme),
      s"Expected scheme 'http' or 'https', but got URI '$entrypointsParentDirRemoteUri'"
    )

    val siteSchemes: Map[String, String] = Map(entrypointsParentDirRemoteUri.getHost -> entrypointsParentDirRemoteUri.getScheme)
    val catalog: SimpleCatalog = SimpleCatalogs.fromLocalMirrorRootDirectory(taxoRootDir, siteSchemes)
    val reverseCatalog: SimpleCatalog = catalog.reverse

    val entrypointsLocalParentDir: File = new File(catalog.getMappedUri(entrypointsParentDirRemoteUri))
    val entrypointFiles: Seq[File] = entrypointsLocalParentDir.listFiles(_.isFile).toIndexedSeq
    val entrypointUris: Set[URI] = entrypointFiles.map(f => reverseCatalog.getMappedUri(f.toURI)).toSet

    createTaxonomy(entrypointUris, taxoRootDir, processor)
  }

  def createTaxonomy(entrypointUris: Set[URI], taxoRootDir: File, processor: Processor): BasicTaxonomy = {
    val dtsUriCollector: DtsUriCollector = DefaultDtsUriCollector

    println(s"Finding DTS document URIs (entrypoint: ${entrypointUris.mkString(", ")} ...") // scalastyle:off

    val docBuilder: SaxonDocumentBuilder = getDocumentBuilder(taxoRootDir, processor)

    val dtsDocUris: Set[URI] =
      dtsUriCollector.findAllDtsUris(entrypointUris, uri => TaxonomyElems.of(docBuilder.build(uri).documentElement))

    println(s"Parsing DTS documents ...") // scalastyle:off

    val documents: Seq[TaxonomyDocument] = dtsDocUris.toSeq.sortBy(_.toString).map(u => TaxonomyDocument.from(docBuilder.build(u)))

    println(s"Building TaxonomyBase ...") // scalastyle:off

    val taxoBase: TaxonomyBase = TaxonomyBase.build(documents, SubstitutionGroupMap.Empty)

    println(s"Number of documents in the TaxonomyBase: ${taxoBase.rootElems.size}") // scalastyle:off
    println(s"Building BasicTaxonomy ...") // scalastyle:off

    val taxo: BasicTaxonomy = BasicTaxonomy.build(taxoBase, DefaultParallelRelationshipFactory)

    println(s"Number of relationships: ${taxo.relationships.size}") // scalastyle:off
    taxo
  }

  def createTaxonomy(entrypointUri: URI, taxoRootDir: File, processor: Processor): BasicTaxonomy = {
    createTaxonomy(Set(entrypointUri), taxoRootDir, processor)
  }

  private def getDocumentBuilder(taxoRootDir: File, processor: Processor): SaxonDocumentBuilder = {
    val uriResolver: SaxUriResolver = SaxUriResolvers.fromLocalMirrorRootDirectory(taxoRootDir)

    SaxonDocumentBuilder(processor, uriResolver)
  }
}
