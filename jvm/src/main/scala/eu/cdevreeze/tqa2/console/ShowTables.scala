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

import eu.cdevreeze.tqa2.aspect.AspectModel
import eu.cdevreeze.tqa2.locfreetaxonomy.dom._
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import net.sf.saxon.s9api.Processor

import scala.reflect.classTag

/**
 * Program that shows table info in a given locator-free taxonomy loader. The program expects a local mirror, with a
 * directory for the host name (no port), which has a sub-tree for the path.
 *
 * TODO Remove this program once it is no longer needed here.
 *
 * @author Chris de Vreeze
 */
object ShowTables {

  // TODO Use catalogs, ZIP files, HTTP versus HTTPS, multi-document entrypoints, etc.

  private val processor = new Processor(false)

  def main(args: Array[String]): Unit = {
    require(args.length == 2, s"Usage: ShowTables <taxo root dir> <entrypoint URI>")

    val start = System.currentTimeMillis()

    val taxoRootDir = new File(args(0))
    require(taxoRootDir.isDirectory, s"Not a directory: '$taxoRootDir'")

    val entrypointUri: URI = URI.create(args(1))

    val taxo: BasicTaxonomy = ConsoleUtil.createTaxonomy(entrypointUri, taxoRootDir, processor)

    printTableInfo(taxo)

    val end = System.currentTimeMillis()

    println() // scalastyle:off
    println(s"The program took ${end - start} ms") // scalastyle:off
  }

  // scalastyle:off
  def printTableInfo(taxo: BasicTaxonomy): Unit = {
    val tables: Seq[Table] = taxo.findAllLinkbases.flatMap(_.findAllDescendantElemsOfType(classTag[Table]))

    println()
    println(s"Number of tables: ${tables.size}")

    println()
    println("---------- Tables ----------")

    tables.sortBy(_.docUri).foreach { table =>
      println()
      println(s"[${table.getClass.getSimpleName}]")
      println(s"${table.ownAnyElementKey.key}")

      val tableBreakdownRelationships = taxo.findAllOutgoingTableBreakdownRelationships(table).sortBy(_.arc.order)

      tableBreakdownRelationships.foreach { tableBreakdownRel =>
        println(s"\t[${tableBreakdownRel.breakdown.getClass.getSimpleName}]")
        println(s"\t${tableBreakdownRel.breakdown.idOption.getOrElse("")}")
        println(s"\tAxis: ${tableBreakdownRel.axis} (order: ${tableBreakdownRel.arc.order})")

        val breakdownTreeRelationships = taxo.findAllOutgoingBreakdownTreeRelationships(tableBreakdownRel.breakdown)

        breakdownTreeRelationships.foreach { breakdownTreeRel =>
          printNodeInfoTree(breakdownTreeRel.definitionNode, taxo, indentCount = 0)
        }
      }
    }
  }

  // scalastyle:off
  def printNodeInfoTree(node: DefinitionNode, taxo: BasicTaxonomy, indentCount: Int): Unit = {
    val indentString: String = "\t" * (indentCount + 2)

    println(indentString + s"[${node.getClass.getSimpleName}]")

    node match {
      case n: RuleNode                  => printRuleNodeInfo(n, indentCount)
      case n: ConceptRelationshipNode   => printConceptRelationshipNodeInfo(n, indentCount)
      case n: DimensionRelationshipNode => printDimensionRelationshipNodeInfo(n, indentCount)
      case _                            => ()
    }

    val subtreeRelationships = taxo.findAllOutgoingDefinitionNodeSubtreeRelationships(node).sortBy(_.arc.order)

    subtreeRelationships.foreach { subtreeRel =>
      printNodeInfoTree(subtreeRel.targetNode, taxo, indentCount + 1)
    }
  }

  def printRuleNodeInfo(node: RuleNode, indentCount: Int): Unit = {
    val indentString: String = "\t" * (indentCount + 2)

    println(indentString + s"Abstract: ${node.isAbstract}")
    println(indentString + s"Merged: ${node.isMerged}")

    node.allAspects.foreach { aspect =>
      printAspectInfo(aspect, indentCount)
    }
  }

  def printConceptRelationshipNodeInfo(node: ConceptRelationshipNode, indentCount: Int): Unit = {
    val indentString: String = "\t" * (indentCount + 2)

    println(indentString + s"Relationship sources: ${node.relationshipSources.map(_.source).mkString(", ")}")
    node.formulaAxisOption.foreach(v => println(indentString + s"Axis: ${v.formulaAxis}"))
    node.generationsOption.foreach(v => println(indentString + s"Generatiors: ${v.generations}"))
    node.linknameOption.foreach(v => println(indentString + s"Link name: ${v.linkname}"))
    node.linkroleOption.foreach(v => println(indentString + s"Linkrole: ${v.linkrole}"))
    node.arcnameOption.foreach(v => println(indentString + s"Arc name: ${v.arcname}"))
    node.arcroleOption.foreach(v => println(indentString + s"Arcrole: ${v.arcrole}"))
  }

  def printDimensionRelationshipNodeInfo(node: DimensionRelationshipNode, indentCount: Int): Unit = {
    val indentString: String = "\t" * (indentCount + 2)

    println(indentString + s"Relationship sources: ${node.relationshipSources.map(_.source).mkString(", ")}")
    node.formulaAxisOption.foreach(v => println(indentString + s"Axis: ${v.formulaAxis}"))
    node.generationsOption.foreach(v => println(indentString + s"Generatiors: ${v.generations}"))
    node.linkroleOption.foreach(v => println(indentString + s"Linkrole: ${v.linkrole}"))
  }

  private def printAspectInfo(aspect: FormulaAspect, indentCount: Int): Unit = {
    val indentString: String = "\t" * (indentCount + 2)

    aspect match {
      case aspect: ConceptAspect =>
        println(indentString + s"Concept aspect: ${aspect.aspect(AspectModel.DimensionalAspectModel)}")
      case aspect: ExplicitDimensionAspect =>
        println(indentString + s"Explicit dimension aspect: ${aspect.aspect(AspectModel.DimensionalAspectModel)}")
      case aspect: TypedDimensionAspect =>
        println(indentString + s"Typed dimension aspect: ${aspect.aspect(AspectModel.DimensionalAspectModel)}")
      case _ =>
        ()
    }
  }
}
