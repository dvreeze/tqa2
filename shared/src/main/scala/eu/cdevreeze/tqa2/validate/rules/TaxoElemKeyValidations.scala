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

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.ArcroleKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.ArcroleType
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.ConceptKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.RoleKey
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.RoleType
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Rule
import eu.cdevreeze.tqa2.validate.Taxonomies
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult
import eu.cdevreeze.yaidom2.core.EName

/**
 * Taxonomy element key validations, checking that they are not "dead" references.
 *
 * @author Chris de Vreeze
 */
object TaxoElemKeyValidations {

  val missingConceptRule: Rule = "Missing concept"

  val missingRoleTypeRule: Rule = "Missing role type"

  val missingArcroleTypeRule: Rule = "Missing arcrole type"

  object MissingConcept extends Validation {

    def rule: Rule = missingConceptRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val keys = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.name == ENames.CKeyConceptKeyEName))
        .collect { case key: ConceptKey => key }

      val missingConcepts: Seq[EName] = keys.map(_.key).distinct.filter(c => taxo.findConceptDeclaration(c).isEmpty)

      missingConcepts.map(c => ValidationResult(rule, "Missing concept", c))
    }
  }

  object MissingRoleType extends Validation {

    def rule: Rule = missingRoleTypeRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val keys = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.name == ENames.CKeyRoleKeyEName))
        .collect { case key: RoleKey => key }

      val allRoleTypeUris: Set[String] = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.name == ENames.LinkRoleTypeEName))
        .collect { case e: RoleType => e.roleUri }
        .toSet

      val missingRoles: Seq[String] = keys.map(_.key).distinct.filterNot(allRoleTypeUris)

      missingRoles.map(u => ValidationResult(rule, "Missing role type", u))
    }
  }

  object MissingArcroleType extends Validation {

    def rule: Rule = missingArcroleTypeRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val keys = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.name == ENames.CKeyArcroleKeyEName))
        .collect { case key: ArcroleKey => key }

      val allArcroleTypeUris: Set[String] = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.name == ENames.LinkArcroleTypeEName))
        .collect { case e: ArcroleType => e.arcroleUri }
        .toSet

      val missingArcroles: Seq[String] = keys.map(_.key).distinct.filterNot(allArcroleTypeUris)

      missingArcroles.map(u => ValidationResult(rule, "Missing arcrole type", u))
    }
  }

  val all: Seq[Validation] = Seq(MissingConcept, MissingRoleType, MissingArcroleType)
}
