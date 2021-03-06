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

import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.ParentChildRelationship
import eu.cdevreeze.tqa2.locfreetaxonomy.relationship.Relationship
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult
import eu.cdevreeze.tqa2.validate.Validator
import eu.cdevreeze.tqa2.validate.rules._
import eu.cdevreeze.yaidom2.core.EName
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

    val taxo: BasicTaxonomy = ConsoleUtil.createTaxonomy(entrypointUri, taxoRootDir, processor)

    printTaxonomyInfo(taxo)

    validateTaxonomy(taxo, entrypointUri)

    val end = System.currentTimeMillis()

    println() // scalastyle:off
    println(s"The program took ${end - start} ms") // scalastyle:off
  }

  // scalastyle:off
  def printTaxonomyInfo(taxo: BasicTaxonomy): Unit = {
    val hasHypercubes: Seq[HasHypercubeRelationship] = taxo.findAllHasHypercubeRelationships
    val hasHypercubeElrs: Set[String] = hasHypercubes.map(_.elr).toSet

    println()
    println(s"Number of dimensional (has-hypercube) ELRs: ${hasHypercubeElrs.size}")

    val parentChildren: Seq[ParentChildRelationship] = taxo.findAllParentChildRelationships
    val parentChildElrs: Set[String] = parentChildren.map(_.elr).toSet

    println(s"Number of parent-child relationship ELRs: ${parentChildElrs.size}")

    println(s"Number of parent-child relationship ELRs that are not dimensional ELRs: ${parentChildElrs.diff(hasHypercubeElrs).size}")
    println(s"Number of dimensional ELRs that are not parent-child relationship ELRs: ${hasHypercubeElrs.diff(parentChildElrs).size}")

    val dimensionalConcepts: Set[EName] = taxo.computeHasHypercubeInheritanceOrSelf.keySet

    val items: Set[EName] = taxo.findAllItemDeclarations.map(_.targetEName).toSet

    println(s"Number of dimensional concepts that are not items in the taxo: ${dimensionalConcepts.diff(items).size}")
    println(s"Number of items in the taxo that are not dimensional concepts: ${items.diff(dimensionalConcepts).size}")

    val relationshipCounts: Map[Class[_ <: Relationship], Int] = taxo.relationships.groupBy(_.getClass).view.mapValues(_.size).toMap
    val relationshipArcroleCounts: Map[Class[_ <: Relationship], Map[String, Int]] =
      taxo.relationships.groupBy(_.getClass).view.mapValues(_.groupBy(_.arcrole).view.mapValues(_.size).toMap).toMap

    println()
    println(s"There are ${taxo.relationships.size} relationships in the taxonomy (in ${taxo.rootElems.size} documents)")
    println()
    relationshipCounts.toSeq.sortBy(_._2).reverse.foreach {
      case (relCls, cnt) =>
        val arcroleCounts: Map[String, Int] = relationshipArcroleCounts.getOrElse(relCls, Map.empty)

        val arcroleCountStrings: Seq[String] = arcroleCounts.toSeq
          .sortBy(_._2)
          .reverse
          .map {
            case (arcrole, cnt) => s"$arcrole ($cnt)"
          }

        println(
          s"Relationship class ${relCls.getSimpleName}, count $cnt (arcroles: ${arcroleCountStrings.mkString(", ")})"
        )
    }

    val allElems: Seq[TaxonomyElem] = taxo.rootElems.flatMap(_.findAllDescendantElemsOrSelf)

    val domTypeCounts: Map[Class[_ <: TaxonomyElem], Int] = allElems.groupBy(_.getClass).view.mapValues(_.size).toMap
    val domTypeElemNameCounts: Map[Class[_ <: TaxonomyElem], Map[EName, Int]] =
      allElems.groupBy(_.getClass).view.mapValues(_.groupBy(_.name).view.mapValues(_.size).toMap).toMap

    println()
    println(s"There are ${allElems.size} XML elements in the taxonomy (in ${taxo.rootElems.size} documents)")
    println()
    domTypeCounts.toSeq.sortBy(_._2).reverse.foreach {
      case (cls, cnt) =>
        val elemNameCounts: Map[EName, Int] = domTypeElemNameCounts.getOrElse(cls, Map.empty)

        val elemCountStrings: Seq[String] = elemNameCounts.toSeq
          .sortBy(_._2)
          .reverse
          .map { case (nm, cnt) => s"$nm ($cnt)" }

        println(s"Element class ${cls.getSimpleName}, count $cnt (element names: ${elemCountStrings.mkString(", ")})")
    }
  }

  // scalastyle:off
  def validateTaxonomy(taxo: BasicTaxonomy, entrypointUri: URI): Unit = {
    val validations: Seq[Validation] = XLinkValidations.all
      .appendedAll(SchemaValidations.all)
      .appendedAll(TaxoDocumentValidations.all)
      .appendedAll(TaxoElemKeyValidations.all)
      .appendedAll(NamespaceValidations.all)
      .appendedAll(EntrypointSchemaValidations.all)

    val validationResults: Seq[ValidationResult] = Validator.validate(taxo, validations)

    val validationOk = validationResults.isEmpty

    println()
    println(s"Number of validations: ${validations.size}")
    validations.foreach(v => println(s"""\tValidation: "${v.rule}""""))

    println()
    println(s"Validation OK: $validationOk")

    if (!validationOk) {
      println()
      println(s"Number of validation results: ${validationResults.size}")
      println(s"Validation results (${validationResults.size}):")
      validationResults.foreach(r => println(s"\t$r"))
    }
  }
}
