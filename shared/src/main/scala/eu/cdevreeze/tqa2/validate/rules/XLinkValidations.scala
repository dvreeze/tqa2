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

package eu.cdevreeze.tqa2.validate.rules

import java.net.URI

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.ExtendedLink
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XLinkResource
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Rule
import eu.cdevreeze.tqa2.validate.Taxonomies
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult

/**
 * XLink-related validations, checking that neither locators nor simple links are used, and that the arcs point
 * to existing XLink resources within the same extended link.
 *
 * @author Chris de Vreeze
 */
object XLinkValidations {

  val locatorNotAllowedRule: Rule = "XLink locator not allowed"

  val simpleLinkNotAllowedRule: Rule = "XLink simple link not allowed"

  val missingArcFromRule: Rule = "Missing arc 'from'"

  val missingArcToRule: Rule = "Missing arc 'to'"

  object LocatorNotAllowed extends Validation {

    def rule: Rule = locatorNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val locs = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.attrOption(ENames.XLinkTypeEName).contains("locator")))

      locs.map(_.docUri).distinct.map(uri => ValidationResult(rule, "Locators found but not allowed", uri))
    }
  }

  object SimpleLinkNotAllowed extends Validation {

    def rule: Rule = simpleLinkNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val simpleLinks = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.attrOption(ENames.XLinkTypeEName).contains("simple")))

      simpleLinks.map(_.docUri).distinct.map(uri => ValidationResult(rule, "Simple links found but not allowed", uri))
    }
  }

  sealed trait MissingArcFromOrTo extends Validation {

    def rule: Rule

    protected def isForArcFrom: Boolean

    final def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val extendedLinks = taxo.findAllLinkbases.filter(Taxonomies.isProperTaxonomyDocumentContent).flatMap(_.findAllExtendedLinks)

      extendedLinks.flatMap(validateExtendedLink)
    }

    private def validateExtendedLink(extLink: ExtendedLink): Seq[ValidationResult] = {
      val resourceMap: Map[String, Seq[XLinkResource]] = extLink.labeledXlinkResourceMap.filter(_._2.nonEmpty)

      val arcs = extLink.arcs

      val missingLabels = arcs.map(arc => if (isForArcFrom) arc.from else arc.to).distinct.filterNot(resourceMap.keySet)

      missingLabels
        .map(lbl =>
          ValidationResult(rule, s"Missing XLink '${if (isForArcFrom) "from" else "to"}'", MissingXLinkLabel(extLink.docUri, lbl)))
    }
  }

  final case class MissingXLinkLabel(doUri: URI, missingLabel: String)

  object MissingArcFrom extends MissingArcFromOrTo {

    def rule: Rule = missingArcFromRule

    protected def isForArcFrom: Boolean = true
  }

  object MissingArcTo extends MissingArcFromOrTo {

    def rule: Rule = missingArcToRule

    protected def isForArcFrom: Boolean = false
  }

  val all: Seq[Validation] = Seq(LocatorNotAllowed, SimpleLinkNotAllowed, MissingArcFrom, MissingArcTo)
}
