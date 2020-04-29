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
import java.util.regex.Pattern

import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.tqa2.common.namespaceutils.XbrlDocumentENameExtractor
import eu.cdevreeze.tqa2.docbuilder.SimpleCatalog
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxUriResolvers
import eu.cdevreeze.tqa2.docbuilder.jvm.saxon.SaxonDocumentBuilder
import eu.cdevreeze.tqa2.internal.converttaxonomy.DefaultXLinkResourceConverter
import eu.cdevreeze.tqa2.internal.converttaxonomy.TaxonomyBaseConverter
import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.tqa2.internal.standardtaxonomy.taxonomy.builder.DefaultDtsUriCollector
import eu.cdevreeze.tqa2.internal.standardtaxonomy.taxonomy.builder.DefaultTaxonomyBaseBuilder
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.TaxonomyBase
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.jvm.DefaultParallelRelationshipFactory
import eu.cdevreeze.tqa2.validate.Taxonomies
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.node.saxon
import eu.cdevreeze.yaidom2.node.saxon.SaxonProducers
import eu.cdevreeze.yaidom2.node.saxon.SaxonSerializer
import eu.cdevreeze.yaidom2.queryapi.ScopedElemApi
import net.sf.saxon.s9api.Processor

/**
 * Taxonomy converter, reading a standard taxonomy, converting it to the locator-free model, and saving it to disk.
 *
 * The program arguments are an input taxonomy root directory, an entrypoint regular expression, and an output locator-free
 * taxonomy root directory. It is assumed that each entrypoint is a single-document entrypoint.
 *
 * This program first parses the input taxonomy, which is a standard taxonomy. Then it converts the non-entrypoint files
 * to the locator-free model. Adter that it adds the entrypoints one by one. Finally it saves the locator-free taxonomy
 * to disk.
 *
 * @author Chris de Vreeze
 */
object TaxonomyConverter {

  // TODO Use catalogs, ZIP files, HTTP versus HTTPS, multi-document entrypoints, etc.

  private val processor = new Processor(false)

  private val forceSaving: Boolean = System.getProperty("forceSaving", "false").toBoolean

  def main(args: Array[String]): Unit = {
    require(
      args.length == 3,
      s"Usage: TaxonomyConverter <input taxo root dir> <output taxo root dir> <entrypoint URI regex>"
    )

    val start = System.currentTimeMillis()

    val inputTaxoRootDir = new File(args(0))
    require(inputTaxoRootDir.isDirectory, s"Not a directory: '$inputTaxoRootDir'")

    val outputTaxoRootDir = new File(args(1))
    outputTaxoRootDir.mkdirs()
    require(outputTaxoRootDir.isDirectory, s"Not a directory: '$outputTaxoRootDir'")

    val entrypointUriRegex: Pattern = Pattern.compile(URI.create(args(2)).toString)

    // Parsing the input taxonomy

    println(s"Parsing the input taxonomy ...") // scalastyle:off

    // Must be bidirectional
    val inputCatalog: SimpleCatalog = createCatalog(inputTaxoRootDir)
      .ensuring(c => c.reverse.reverse.netSimpleCatalog == c.netSimpleCatalog)

    val docBuilder: SaxonDocumentBuilder =
      SaxonDocumentBuilder(processor, SaxUriResolvers.fromCatalogWithoutFallback(inputCatalog))

    val reverseCatalog: SimpleCatalog = inputCatalog.reverse

    val fileUris: Set[URI] = findAllXmlFiles(inputTaxoRootDir).map(f => reverseCatalog.getMappedUri(f.toURI)).toSet

    def isEntrypoint(uri: URI): Boolean = entrypointUriRegex.matcher(uri.toString).matches

    val combinedEntrypoint: Set[URI] = fileUris.filter(isEntrypoint).ensuring(_.nonEmpty)

    val inputTaxoBase: standardtaxonomy.taxonomy.TaxonomyBase = DefaultTaxonomyBaseBuilder
      .withDocumentBuilder(docBuilder)
      .withDtsUriCollector(DefaultDtsUriCollector.instance)
      .build(combinedEntrypoint)

    println(s"Successfully parsed the input taxonomy. It contains ${inputTaxoBase.rootElems.size} documents") // scalastyle:off

    require(
      inputTaxoBase.rootElems
        .filter(e => Taxonomies.isProperTaxonomyDocumentUri(e.docUri))
        .forall(e => ScopedElemApi.containsNoConflictingScopes(e)),
      s"Per document, conflicting scopes are not allowed"
    )

    // Converting the input taxonomy to the locator-free model

    val scope: Scope = ScopedElemApi
      .unionScope(inputTaxoBase.rootElems)
      .withoutDefaultNamespace
      .filterNamespaces(ns => !extraScope.namespaces.contains(ns))
      .append(extraScope)

    val namespacePrefixMapper: NamespacePrefixMapper =
      NamespacePrefixMapper.fromPrefixToNamespaceMapWithFallback(scope.prefixNamespaceMap)

    val taxoConverter: TaxonomyBaseConverter = TaxonomyBaseConverter(
      new DefaultXLinkResourceConverter(namespacePrefixMapper),
      namespacePrefixMapper,
      XbrlDocumentENameExtractor.defaultInstance)

    println(s"Converting the input taxonomy ...") // scalastyle:off

    val outputTaxoBaseWithoutEntrypoints: TaxonomyBase =
      taxoConverter.convertTaxonomyBaseIgnoringEntrypoints(inputTaxoBase, isEntrypoint)

    println(
      s"Successfully converted the input taxonomy without entrypoints to a TaxonomyBase. The result contains ${outputTaxoBaseWithoutEntrypoints.rootElems.size} documents") // scalastyle:off

    println(s"Adding single document entrypoints for regex '$entrypointUriRegex'") // scalastyle:off

    val outputTaxoBase: TaxonomyBase = taxoConverter
      .addSingleDocumentEntrypoints(entrypointUriRegex, outputTaxoBaseWithoutEntrypoints, inputTaxoBase)

    println(s"Successfully converted the input taxonomy to a TaxonomyBase. The result contains ${outputTaxoBase.rootElems.size} documents") // scalastyle:off

    val outputTaxo: BasicTaxonomy = BasicTaxonomy.build(outputTaxoBase, DefaultParallelRelationshipFactory)

    println(s"Successfully converted the input taxonomy. The result contains ${outputTaxo.rootElems.size} documents") // scalastyle:off

    require(
      outputTaxo.findAllItemDeclarations.map(_.targetEName).toSet == inputTaxoBase.findAllItemDeclarations.map(_.targetEName).toSet,
      s"Input and output taxonomies not matching on target ENames of item declarations"
    )
    require(
      outputTaxo.relationships.map(_.arc).distinct.size ==
        inputTaxoBase.rootElems.flatMap(_.filterDescendantElemsOrSelf(_.isInstanceOf[standardtaxonomy.dom.XLinkArc])).size,
      s"Input and output taxonomies not matching on number of (underlying) arcs"
    )

    println(s"Performed some sanity checks on the result taxonomy compared to the original") // scalastyle:off

    // Saving the locator-free taxonomy to disk

    inputTaxoRootDir.listFiles.toSeq.filter(_.isDirectory).foreach { hostNameDir =>
      val targetDir: File = new File(outputTaxoRootDir, hostNameDir.getName)
      targetDir.mkdirs()
    }
    val outputCatalog: SimpleCatalog = createCatalog(outputTaxoRootDir)

    println(s"Saving the output taxonomy ...") // scalastyle:off

    serializeTaxonomy(outputTaxo, outputTaxoRootDir, outputCatalog)

    val end = System.currentTimeMillis()

    println() // scalastyle:off
    println(s"The program took ${end - start} ms") // scalastyle:off
  }

  private def createCatalog(dir: File): SimpleCatalog = {
    assert(dir.isDirectory)

    val taxoDirs: Seq[File] = dir.listFiles.toSeq.filter(_.isDirectory).ensuring(_.nonEmpty)

    val mappings: Map[String, String] = taxoDirs.map { taxoDir =>
      val hostName = taxoDir.getName
      val scheme = "http" // Rather arbitrary!
      URI.create(s"$scheme://$hostName/").toString -> taxoDir.toURI.toString
    }.toMap

    SimpleCatalog.from(mappings)
  }

  private def findAllXmlFiles(rootDir: File, isXmlFile: File => Boolean = isXmlFile): Seq[File] = {
    assert(rootDir.isDirectory)

    rootDir.listFiles().toSeq.flatMap {
      case d if d.isDirectory =>
        // Recursive call
        findAllXmlFiles(d, isXmlFile)
      case f if f.isFile && isXmlFile(f) =>
        Seq(f)
      case _ =>
        Seq.empty
    }
  }

  private def isXmlFile(f: File): Boolean = f.getName.endsWith(".xml") || f.getName.endsWith(".xsd")

  private def extraScope: Scope = Scope.from(
    "cxl" -> Namespaces.CxlNamespace,
    "clink" -> Namespaces.CLinkNamespace,
    "ckey" -> Namespaces.CKeyNamespace,
    "cxbrldt" -> Namespaces.CXbrldtNamespace,
    "cgen" -> Namespaces.CGenNamespace,
    // Prefix "xs" should be used instead of "xsd"
    "xs" -> Namespaces.XsNamespace,
  )

  private def serializeTaxonomy(taxo: BasicTaxonomy, rootDir: File, catalog: SimpleCatalog): Unit = {
    assert(rootDir.isDirectory)

    // Also the core files
    taxo.rootElems.foreach { rootElem =>
      val saxonDoc: saxon.Document =
        SaxonProducers.makeDocument(SaxonProducers.elementProducer(processor).from(rootElem))
      val localUri: URI = catalog.getMappedUri(rootElem.docUri)

      val file: File = new File(localUri)

      if (!file.exists() || forceSaving) {
        SaxonSerializer.serialize(saxonDoc, file)
      }
    }
  }
}
