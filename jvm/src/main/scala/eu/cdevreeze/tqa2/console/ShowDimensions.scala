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

import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom2.core.EName
import net.sf.saxon.s9api.Processor

/**
 * Program that shows dimensional info in a given locator-free taxonomy loader. The program expects a local mirror, with a
 * directory for the host name (no port), which has a sub-tree for the path.
 *
 * TODO Remove this program once it is no longer needed here.
 *
 * @author Chris de Vreeze
 */
object ShowDimensions {

  // TODO Use catalogs, ZIP files, HTTP versus HTTPS, multi-document entrypoints, etc.

  private val processor = new Processor(false)

  def main(args: Array[String]): Unit = {
    require(args.length == 2, s"Usage: ShowDimensions <taxo root dir> <entrypoint URI>")

    val start = System.currentTimeMillis()

    val taxoRootDir = new File(args(0))
    require(taxoRootDir.isDirectory, s"Not a directory: '$taxoRootDir'")

    val entrypointUri: URI = URI.create(args(1))

    val taxo: BasicTaxonomy = ConsoleUtil.createTaxonomy(entrypointUri, taxoRootDir, processor)

    printDimensionalInfo(taxo)

    val end = System.currentTimeMillis()

    println() // scalastyle:off
    println(s"The program took ${end - start} ms") // scalastyle:off
  }

  def printDimensionalInfo(taxo: BasicTaxonomy): Unit = {
    printHypercubeInfo(taxo)
    printHypercubeInheritanceInfo(taxo)
  }

  // scalastyle:off
  def printHypercubeInfo(taxo: BasicTaxonomy): Unit = {
    val hasHypercubes: Seq[HasHypercubeRelationship] = taxo.findAllHasHypercubeRelationships
    val hasHypercubeElrs: Set[String] = hasHypercubes.map(_.elr).toSet

    println()
    println(s"Number of dimensional (has-hypercube) ELRs: ${hasHypercubeElrs.size}")

    println()
    println("---------- Dimensional trees per (has-hypercube relationship) ELR ----------")

    hasHypercubeElrs.toSeq.sorted.foreach { hhElr =>
      println()
      println(s"Has-hypercube ELR: $hhElr")

      val currHasHypercubes = hasHypercubes.filter(_.elr == hhElr)

      currHasHypercubes.foreach { hh =>
        taxo.findAllConsecutiveHypercubeDimensionRelationships(hh).foreach { hd =>
          val isTypedDim = taxo.findTypedDimensionDeclaration(hd.dimension).nonEmpty

          if (isTypedDim) println(s"\tTyped dimension: ${hd.dimension}") else println(s"\tExplicit dimension: ${hd.dimension}")
        }

        val dimMembers: Map[EName, Set[EName]] = taxo.findAllUsableDimensionMembers(hh)

        dimMembers.keySet.toSeq.sortBy(_.toString).foreach { dim =>
          println(s"Dimension $dim in has-hypercube ELR $hhElr has (${dimMembers(dim).size}) usable members:")

          dimMembers(dim).toSeq.sortBy(_.toString).foreach(mem => println(s"\t$mem"))
        }
      }

      val relevantTaxoDocUris: Set[URI] =
        currHasHypercubes.flatMap { hh =>
          taxo.filterOutgoingConsecutiveDimensionalRelationshipPaths(hh.hypercube) { path =>
            path.firstRelationship.elr == hhElr
          }
        }.flatMap(_.relationships).map(_.docUri).toSet.union(currHasHypercubes.map(_.docUri).toSet)

      println(s"Taxonomy documents involved in dimensional relationships for ELR $hhElr:")
      relevantTaxoDocUris.toSeq.sortBy(_.toString).foreach(uri => println(s"\tTaxonomy document: $uri"))
    }
  }

  // scalastyle:off
  def printHypercubeInheritanceInfo(taxo: BasicTaxonomy): Unit = {
    println()
    println("---------- Concepts inheriting or having hypercubes ----------")

    val hasHypercubeInheritanceOrSelf: Map[EName, Seq[HasHypercubeRelationship]] = taxo.computeHasHypercubeInheritanceOrSelf

    println()
    println(s"Found ${hasHypercubeInheritanceOrSelf.size} concepts (concrete or abstract) inheriting or having hypercubes")

    hasHypercubeInheritanceOrSelf.keySet.toSeq.sortBy(_.toString).foreach { concept =>
      println()
      val abstractOrConcrete = if (taxo.getConceptDeclaration(concept).isAbstract) "Abstract" else "Concrete"
      val kindOfConcept = taxo.getConceptDeclaration(concept).getClass.getSimpleName

      val elrs: Set[String] = hasHypercubeInheritanceOrSelf(concept).map(_.elr).toSet

      println(s"$abstractOrConcrete $kindOfConcept $concept inherits or has has-hypercubes in (${elrs.size}) ELRs:")
      elrs.toSeq.sorted.foreach(elr => println(s"\t$elr"))

      val relevantTaxoDocUris: Set[URI] = taxo.findAllIncomingConsecutiveDimensionalRelationshipPaths(concept)
        .flatMap(_.relationships).map(_.docUri).toSet

      println(s"Taxonomy documents with has-hypercube inheritance domain-member relationships for " +
        s"concept $concept (${relevantTaxoDocUris.size} documents):")

      relevantTaxoDocUris.toSeq.sortBy(_.toString).foreach(uri => println(s"\tTaxonomy document: $uri"))
    }
  }
}
