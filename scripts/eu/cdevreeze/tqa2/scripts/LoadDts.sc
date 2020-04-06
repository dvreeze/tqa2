
// Run amm in scripts folder
// In amm session, use command "import $exec.eu.cdevreeze.tqa2.scripts.LoadDts"

// Taking TQA2 version 0.2.0-SNAPSHOT

import $ivy.`eu.cdevreeze.tqa2::tqa2:0.2.0-SNAPSHOT`

// Imports that (must) remain available after this initialization script

import java.net.URI
import java.io._

import net.sf.saxon.s9api.Processor

import eu.cdevreeze.yaidom2.core._
import eu.cdevreeze.yaidom2

import eu.cdevreeze.tqa2._
import eu.cdevreeze.tqa2.docbuilder.jvm.CachingDocumentBuilder
import eu.cdevreeze.tqa2.docbuilder.jvm.SaxUriResolvers
import eu.cdevreeze.tqa2.docbuilder.jvm.saxon.SaxonDocumentBuilder
import eu.cdevreeze.tqa2.internal.standardtaxonomy.taxonomy._
import eu.cdevreeze.tqa2.internal.standardtaxonomy.taxonomy.builder._

val processor = new Processor(false)

def loadDts(localRootDir: File, entrypointUris: Set[URI], docCacheSize: Int, lenient: Boolean): TaxonomyBase = {
  val docBuilder =
    SaxonDocumentBuilder(
      processor.newDocumentBuilder(),
      SaxUriResolvers.fromLocalMirrorRootDirectory(localRootDir))

  val documentBuilder =
    new CachingDocumentBuilder(CachingDocumentBuilder.createCache(docBuilder, docCacheSize))

  val dtsUriCollector = DefaultDtsUriCollector.instance

  val taxoBuilder: DefaultTaxonomyBaseBuilder =
    DefaultTaxonomyBaseBuilder.
      withDocumentBuilder(documentBuilder).
      withDtsUriCollector(dtsUriCollector)

  val taxoBase = taxoBuilder.build(entrypointUris)
  taxoBase
}

def loadDts(localRootDir: File, entrypointUri: URI): TaxonomyBase = {
  loadDts(localRootDir, Set(entrypointUri), 10000, false)
}

// Now the REPL has been set up for ad-hoc DTS querying.

println(s"Use loadDts(localRootDir, entrypointUri) to get a DTS as TaxonomyBase")
println(s"If needed, use loadDts(localRootDir, entrypointUris, docCacheSize, lenient) instead")
println(s"Store the result in val taxoBase, and import taxoBase._")
