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

import eu.cdevreeze.tqa2.locfreetaxonomy.dom._
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.yaidom2.core.EName
import net.sf.saxon.s9api.Processor

import scala.reflect.classTag

/**
 * Program that shows formula info in a given locator-free taxonomy loader. The program expects a local mirror, with a
 * directory for the host name (no port), which has a sub-tree for the path.
 *
 * TODO Remove this program once it is no longer needed here.
 *
 * @author Chris de Vreeze
 */
object ShowFormulas {

  // TODO Use catalogs, ZIP files, HTTP versus HTTPS, multi-document entrypoints, etc.

  private val processor = new Processor(false)

  def main(args: Array[String]): Unit = {
    require(args.length == 2, s"Usage: ShowFormulas <taxo root dir> <entrypoint URI>")

    val start = System.currentTimeMillis()

    val taxoRootDir = new File(args(0))
    require(taxoRootDir.isDirectory, s"Not a directory: '$taxoRootDir'")

    val entrypointUri: URI = URI.create(args(1))

    val taxo: BasicTaxonomy = ConsoleUtil.createTaxonomy(entrypointUri, taxoRootDir, processor)

    printFormulaInfo(taxo)

    val end = System.currentTimeMillis()

    println() // scalastyle:off
    println(s"The program took ${end - start} ms") // scalastyle:off
  }

  // scalastyle:off
  def printFormulaInfo(taxo: BasicTaxonomy): Unit = {
    val varSets: Seq[VariableSet] = taxo.findAllLinkbases.flatMap(_.findAllDescendantElemsOfType(classTag[VariableSet]))

    println()
    println(s"Number of variable sets: ${varSets.size}")

    println()
    println("---------- Formulas ----------")

    varSets.sortBy(_.docUri).foreach { varSet =>
      println()
      println(s"${varSet.getClass.getSimpleName} ${varSet.ownAnyElementKey.key}")

      val varSetRelationships = taxo.findAllOutgoingVariableSetRelationships(varSet)

      varSetRelationships.foreach { varSetRel =>
        println(s"\t${varSetRel.variableOrParameter.getClass.getSimpleName} ${varSetRel.arcNameAttrValue}")

        varSetRel.variableOrParameter match {
          case fv: FactVariable =>
            val varFilterRelationships = taxo.findAllOutgoingVariableFilterRelationships(fv)

            varFilterRelationships.foreach { varFilterRel =>
              val filter = varFilterRel.filter

              filter match {
                case f: ConceptNameFilter =>
                  printConceptNameFilterInfo(f)
                case f: ExplicitDimensionFilter =>
                  printExplicitDimensionFilterInfo(f)
                case f: TypedDimensionFilter =>
                  printTypedDimensionFilterInfo(f)
                case f =>
                  println(s"\t\t${f.getClass.getSimpleName}")
              }
            }
          case _ => ()
        }
      }
    }
  }

  // scalastyle:off
  def printConceptNameFilterInfo(filter: ConceptNameFilter): Unit = {
    val concepts: Seq[EName] = filter.concepts.flatMap(_.qnameElemOption).map(_.textAsResolvedQName)

    if (concepts.isEmpty) {
      println(s"\t\t${filter.getClass.getSimpleName}")
    } else {
      println(s"\t\t${filter.getClass.getSimpleName}, name(s): ${concepts.mkString(", ")}")
    }
  }

  // scalastyle:off
  def printExplicitDimensionFilterInfo(filter: ExplicitDimensionFilter): Unit = {
    val dimensions: Seq[EName] = filter.dimension.qnameElemOption.toSeq.map(_.textAsResolvedQName)

    if (dimensions.isEmpty) {
      println(s"\t\t${filter.getClass.getSimpleName}")
    } else {
      println(s"\t\t${filter.getClass.getSimpleName}, dimension: ${dimensions.mkString(", ")}")
    }
  }

  // scalastyle:off
  def printTypedDimensionFilterInfo(filter: TypedDimensionFilter): Unit = {
    val dimensions: Seq[EName] = filter.dimension.qnameElemOption.toSeq.map(_.textAsResolvedQName)

    if (dimensions.isEmpty) {
      println(s"\t\t${filter.getClass.getSimpleName}")
    } else {
      println(s"\t\t${filter.getClass.getSimpleName}, dimension: ${dimensions.mkString(", ")}")
    }
  }
}
