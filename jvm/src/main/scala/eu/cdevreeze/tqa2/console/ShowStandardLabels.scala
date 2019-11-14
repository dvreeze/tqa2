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

import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ConceptLabelRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom2.core.EName
import net.sf.saxon.s9api.Processor

/**
 * Program that shows standard labels in a given locator-free taxonomy loader. The program expects a local mirror, with a
 * directory for the host name (no port), which has a sub-tree for the path.
 *
 * TODO Remove this program once it is no longer needed here.
 *
 * @author Chris de Vreeze
 */
object ShowStandardLabels {

  // TODO Use catalogs, ZIP files, HTTP versus HTTPS, multi-document entrypoints, etc.

  private val processor = new Processor(false)

  def main(args: Array[String]): Unit = {
    require(args.length == 2, s"Usage: ShowStandardLabels <taxo root dir> <entrypoint URI>")

    val start = System.currentTimeMillis()

    val taxoRootDir = new File(args(0))
    require(taxoRootDir.isDirectory, s"Not a directory: '$taxoRootDir'")

    val entrypointUri: URI = URI.create(args(1))

    val taxo: BasicTaxonomy = ConsoleUtil.createTaxonomy(entrypointUri, taxoRootDir, processor)

    printStandardLabelInfo(taxo)

    val end = System.currentTimeMillis()

    println() // scalastyle:off
    println(s"The program took ${end - start} ms") // scalastyle:off
  }

  // scalastyle:off
  def printStandardLabelInfo(taxo: BasicTaxonomy): Unit = {
    val conceptsHavingLabel: Set[EName] = taxo.findAllConceptLabelRelationships.groupBy(_.sourceConcept).keySet
      .filter { concept =>
        taxo.findAllOutgoingParentChildRelationships(concept).nonEmpty || taxo.findAllIncomingParentChildRelationships(concept).nonEmpty
      }

    println()
    println(s"Number of concepts (in P-links) having standard label(s): ${conceptsHavingLabel.size}")

    println()
    println("---------- Concepts and their (standard) labels ----------")

    conceptsHavingLabel.toSeq.sortBy(_.toString).foreach { concept =>
      println()
      println(s"Concept: $concept")

      val conceptLabelRels: Seq[ConceptLabelRelationship] = taxo.findAllOutgoingConceptLabelRelationships(concept)

      conceptLabelRels.sortBy(rel => (rel.resourceRole, rel.language)).foreach { rel =>
        val lang = rel.language
        val role = rel.resourceRole
        val labelText = rel.labelText

        println(s"\tLanguage: $lang. Role: $role")
        println(s"\t\tLabel text: $labelText")
      }
    }
  }
}
