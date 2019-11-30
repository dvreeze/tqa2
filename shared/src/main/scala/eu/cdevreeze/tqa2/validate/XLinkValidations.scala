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

package eu.cdevreeze.tqa2.validate

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy

/**
 * XLink-related validations.
 *
 * @author Chris de Vreeze
 */
object XLinkValidations {

  val locatorNotAllowedRule: Rule = "XLink locator not allowed"

  val simpleLinkNotAllowedRule: Rule = "XLink simple link not allowed"

  object LocatorNotAllowed extends Validation {

    def rule: Rule = locatorNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val locs = taxo.rootElems.filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.attrOption(ENames.XLinkTypeEName).contains("locator")))

      locs.map(_.docUri).distinct.map(uri => ValidationResult(rule, "Locators found but not allowed", uri))
    }
  }

  object SimpleLinkNotAllowed extends Validation {

    def rule: Rule = simpleLinkNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val simpleLinks = taxo.rootElems.filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.attrOption(ENames.XLinkTypeEName).contains("simple")))

      simpleLinks.map(_.docUri).distinct.map(uri => ValidationResult(rule, "Simple links found but not allowed", uri))
    }
  }

  val all: Seq[Validation] = Seq(LocatorNotAllowed, SimpleLinkNotAllowed)
}
