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

import java.io.File
import java.net.URI
import java.util.zip.ZipFile

import eu.cdevreeze.tqa2.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa2.docbuilder.jvm.PartialSaxUriResolver
import eu.cdevreeze.tqa2.docbuilder.jvm.PartialSaxUriResolvers
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxUriResolver
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxUriResolvers
import eu.cdevreeze.tqa2.docbuilder.jvm.saxon.SaxonDocumentBuilder
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder.DefaultDtsUriCollector
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder.DefaultTaxonomyBuilder
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.builder.TaxonomyBuilder
import net.sf.saxon.s9api.Processor

/**
 * Support for parsing the dimensional conformance suite files.
 *
 * @author Chris de Vreeze
 */
object DimensionalConformanceSuiteUtil {

  private val dummyUriPrefix: URI = URI.create("http://www.example.com/")

  val uriResolver: SaxUriResolver = {
    val taxoZipFile: File = new File(
      TestResourceUtil.convertClasspathUriToAbsoluteUri(URI.create("xdt-conf-cr4-2009-10-06-partially-locfree.zip")))

    val coreSchemaZipFile: File = new File(TestResourceUtil.convertClasspathUriToAbsoluteUri(URI.create("core-schemas.zip")))

    val taxoCatalog: SimpleCatalog = SimpleCatalog.from(Map(dummyUriPrefix.toString -> ""))

    val coreSchemaCatalog: SimpleCatalog = SimpleCatalog.from(
      Map(
        "http://www.locfreexbrl.org/" -> "www.locfreexbrl.org/",
        "http://www.xbrl.org/" -> "www.xbrl.org/",
        "http://www.w3.org/" -> "www.w3.org/",
      ))

    val taxoPartialResolver: PartialSaxUriResolver = PartialSaxUriResolvers.forZipFileUsingCatalog(new ZipFile(taxoZipFile), taxoCatalog)

    val coreSchemaPartialResolver: PartialSaxUriResolver =
      PartialSaxUriResolvers.forZipFileUsingCatalog(new ZipFile(coreSchemaZipFile), coreSchemaCatalog)

    SaxUriResolvers.fromPartialSaxUriResolversWithoutFallback(Seq(taxoPartialResolver, coreSchemaPartialResolver))
  }

  def getDocumentBuilder(processor: Processor): SaxonDocumentBuilder = {
    SaxonDocumentBuilder(processor, uriResolver)
  }

  def makeTestDts(relativeDocPaths: Seq[URI], processor: Processor): BasicTaxonomy = {
    val entrypointUris: Set[URI] = relativeDocPaths.map(dummyUriPrefix.resolve).toSet

    val docBuilder = getDocumentBuilder(processor)

    val taxoBuilder: TaxonomyBuilder =
      DefaultTaxonomyBuilder
        .withDocumentBuilder(docBuilder)
        .withDtsUriCollector(DefaultDtsUriCollector)
        .withDefaultRelationshipFactory

    taxoBuilder.build(entrypointUris)
  }
}
