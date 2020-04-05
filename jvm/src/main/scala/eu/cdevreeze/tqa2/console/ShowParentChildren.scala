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

import eu.cdevreeze.tqa2.locfreetaxonomy.common.TaxonomyElemKeys.ConceptKey
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom2.core.EName
import net.sf.saxon.s9api.Processor

/**
 * Program that shows parent-child trees in a given locator-free taxonomy loader. The program expects a local mirror, with a
 * directory for the host name (no port), which has a sub-tree for the path.
 *
 * TODO Remove this program once it is no longer needed here.
 *
 * @author Chris de Vreeze
 */
object ShowParentChildren {

  // TODO Use catalogs, ZIP files, HTTP versus HTTPS, multi-document entrypoints, etc.

  private val processor = new Processor(false)

  def main(args: Array[String]): Unit = {
    require(args.length == 2, s"Usage: ShowParentChildren <taxo root dir> <entrypoint URI>")

    val start = System.currentTimeMillis()

    val taxoRootDir = new File(args(0))
    require(taxoRootDir.isDirectory, s"Not a directory: '$taxoRootDir'")

    val entrypointUri: URI = URI.create(args(1))

    val taxo: BasicTaxonomy = ConsoleUtil.createTaxonomy(entrypointUri, taxoRootDir, processor)

    printParentChildren(taxo)

    val end = System.currentTimeMillis()

    println() // scalastyle:off
    println(s"The program took ${end - start} ms") // scalastyle:off
  }

  // scalastyle:off
  def printParentChildren(taxo: BasicTaxonomy): Unit = {
    val parentChildRels = taxo.findAllParentChildRelationships

    val parentChildElrs: Seq[String] = parentChildRels.map(_.elr).distinct.sorted

    val conceptsInParentChildRels: Seq[EName] =
      parentChildRels.flatMap(rel => Seq(rel.sourceConcept, rel.targetConcept)).distinct.sortBy(_.toString)

    println()
    println(s"Number of parent-child relationship ELRs: ${parentChildElrs.size}")
    println(s"Number of concepts in parent-child relationships: ${conceptsInParentChildRels.size}")

    println()
    println("---------- Parent-children per ELR ----------")

    parentChildElrs.foreach { elr =>
      val rels = taxo.filterParentChildRelationships(_.elr == elr)
      val roots: Seq[EName] = rels.map(_.sourceConcept).toSet.diff(rels.map(_.targetConcept).toSet).toSeq.sortBy(_.toString)

      val paths = roots.flatMap(root => taxo.filterOutgoingConsecutiveParentChildRelationshipPaths(root)(_.firstRelationship.elr == elr))

      println()
      println(s"ELR: $elr. Paths (max depth: ${paths.map(_.relationships.size).maxOption.getOrElse(0)}):")

      paths.foreach { path =>
        println(s"\t${path.elements.map(_.taxonomyElemKey.asInstanceOf[ConceptKey].key).mkString(" --> ")}")
      }
    }
  }
}
