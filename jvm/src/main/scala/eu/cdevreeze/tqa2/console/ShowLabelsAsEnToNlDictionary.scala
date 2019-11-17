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

import eu.cdevreeze.tqa2.locfreetaxonomy.common.StandardLabelRoles
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
object ShowLabelsAsEnToNlDictionary {

  // TODO Use catalogs, ZIP files, HTTP versus HTTPS, multi-document entrypoints, etc.

  private val processor = new Processor(false)

  def main(args: Array[String]): Unit = {
    require(args.length == 2, s"Usage: ShowLabelsAsEnToNlDictionary <taxo root dir> <entrypoint parent dir URI>")

    val taxoRootDir = new File(args(0))
    require(taxoRootDir.isDirectory, s"Not a directory: '$taxoRootDir'")

    val entrypointParentDirUri: URI = URI.create(args(1))

    val taxo: BasicTaxonomy = ConsoleUtil.createTaxonomyForCombinedDts(entrypointParentDirUri, taxoRootDir, processor)

    val rows: Seq[Row] = generateRows(taxo)

    val separator = System.getProperty("columnSeparator", "|")

    println() // scalastyle:off
    println(Row.showHeader(separator)) // scalastyle:off
    println() // scalastyle:off

    rows.foreach(row => println(row.show(separator))) // scalastyle:off
  }

  // scalastyle:off
  def generateRows(taxo: BasicTaxonomy): Seq[Row] = {
    val conceptsHavingLabel: Set[EName] = taxo.findAllConceptLabelRelationships.groupBy(_.sourceConcept).keySet
      .filter { concept =>
        taxo.findAllOutgoingParentChildRelationships(concept).nonEmpty || taxo.findAllIncomingParentChildRelationships(concept).nonEmpty
      }

    val sortedConcepts: Seq[EName] = conceptsHavingLabel.toSeq.sortBy(_.localPart)

    sortedConcepts.map { concept =>
      val conceptLabelRels: Seq[ConceptLabelRelationship] = taxo.findAllOutgoingConceptLabelRelationships(concept)

      val enLabelsByRole: Map[String, String] =
        conceptLabelRels.filter(_.language == "en").groupBy(_.resourceRole).view.mapValues(_.head.labelText).toMap

      val nlLabelsByRole: Map[String, String] =
        conceptLabelRels.filter(_.language == "nl").groupBy(_.resourceRole).view.mapValues(_.head.labelText).toMap

      Row(
        concept,
        enLabelsByRole.get(StandardLabelRoles.StandardLabel),
        enLabelsByRole.get(StandardLabelRoles.TerseLabel),
        enLabelsByRole.get(StandardLabelRoles.VerboseLabel),
        enLabelsByRole.get(StandardLabelRoles.Documentation),
        nlLabelsByRole.get(StandardLabelRoles.StandardLabel),
        nlLabelsByRole.get(StandardLabelRoles.TerseLabel),
        nlLabelsByRole.get(StandardLabelRoles.VerboseLabel),
        nlLabelsByRole.get(StandardLabelRoles.Documentation),
      )
    }
  }

  final case class Row(
    concept: EName,
    standardEnLabelOption: Option[String],
    terseEnLabelOption: Option[String],
    verboseEnLabelOption: Option[String],
    documentationEnLabelOption: Option[String],
    standardNlLabelOption: Option[String],
    terseNlLabelOption: Option[String],
    verboseNlLabelOption: Option[String],
    documentationNlLabelOption: Option[String]) {

    def show(columnSeparator: String): String = {
      val enLabels: Seq[String] =
        Seq(standardEnLabelOption, terseEnLabelOption, verboseEnLabelOption, documentationEnLabelOption).map(_.getOrElse(""))
      val nlLabels: Seq[String] =
        Seq(standardNlLabelOption, terseNlLabelOption, verboseNlLabelOption, documentationNlLabelOption).map(_.getOrElse(""))

      val labels: Seq[String] = enLabels.appendedAll(nlLabels)

      Seq(concept.toString).appendedAll(labels).mkString(columnSeparator)
    }
  }

  object Row {

    val header: Seq[String] = {
      Seq(
        "concept",
        "standard (en)",
        "terse (en)",
        "verbose (en)",
        "documentation (en)",
        "standard (nl)",
        "terse (nl)",
        "verbose (nl)",
        "documentation (nl)")
    }

    def showHeader(columnSeparator: String): String = {
      header.mkString(columnSeparator)
    }
  }
}
