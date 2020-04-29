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

import eu.cdevreeze.tqa2.locfreetaxonomy.dom._
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Rule
import eu.cdevreeze.tqa2.validate.Taxonomies
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult
import eu.cdevreeze.yaidom2.core.EName

import scala.reflect.classTag

/**
 * Taxonomy element key validations, checking that they are not "dead" references.
 *
 * @author Chris de Vreeze
 */
object TaxoElemKeyValidations {

  val missingConceptRule: Rule = "Missing concept"

  val missingElementRule: Rule = "Missing element declaration"

  val missingTypeRule: Rule = "Missing type"

  val missingRoleTypeRule: Rule = "Missing role type"

  val missingArcroleTypeRule: Rule = "Missing arcrole type"

  val missingAnyElementRule: Rule = "Missing XML element"

  object MissingConcept extends Validation {

    def rule: Rule = missingConceptRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val keys = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.findAllDescendantElemsOrSelfOfType(classTag[ConceptKey]))

      val missingConcepts: Seq[EName] = keys.map(_.key).distinct.filter(c => taxo.findConceptDeclaration(c).isEmpty)

      missingConcepts.map(c => ValidationResult(rule, "Missing concept", c))
    }
  }

  object MissingElement extends Validation {

    def rule: Rule = missingElementRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val keys = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.findAllDescendantElemsOrSelfOfType(classTag[ElementKey]))

      val missingElems: Seq[EName] = keys.map(_.key).distinct.filter(e => taxo.findGlobalElementDeclaration(e).isEmpty)

      missingElems.map(c => ValidationResult(rule, "Missing element", c))
    }
  }

  object MissingType extends Validation {

    def rule: Rule = missingTypeRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val keys = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.findAllDescendantElemsOrSelfOfType(classTag[TypeKey]))

      val missingTypes: Seq[EName] = keys.map(_.key).distinct.filter(e => taxo.findNamedTypeDefinition(e).isEmpty)

      missingTypes.map(c => ValidationResult(rule, "Missing type", c))
    }
  }

  object MissingRoleType extends Validation {

    def rule: Rule = missingRoleTypeRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val keys = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.findAllDescendantElemsOrSelfOfType(classTag[RoleKey]))

      val allRoleTypeUris: Set[String] = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.findAllDescendantElemsOrSelfOfType(classTag[RoleType]))
        .map(_.roleUri)
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
        .flatMap(_.findAllDescendantElemsOrSelfOfType(classTag[ArcroleKey]))

      val allArcroleTypeUris: Set[String] = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.findAllDescendantElemsOrSelfOfType(classTag[ArcroleType]))
        .map(_.arcroleUri)
        .toSet

      val missingArcroles: Seq[String] = keys.map(_.key).distinct.filterNot(allArcroleTypeUris)

      missingArcroles.map(u => ValidationResult(rule, "Missing arcrole type", u))
    }
  }

  object MissingAnyElem extends Validation {

    def rule: Rule = missingAnyElementRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val keys = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.findAllDescendantElemsOrSelfOfType(classTag[AnyElementKey]))

      val missingAnyElemKeys: Seq[URI] = keys.map(_.key).distinct.filter(k => findElem(k, taxo).isEmpty)

      missingAnyElemKeys.map(c => ValidationResult(rule, "Missing XML element", c))
    }
  }

  private def findElem(uri: URI, taxo: BasicTaxonomy): Option[TaxonomyElem] = {
    taxo.taxonomyBase.findElemByUri(uri)
  }

  val all: Seq[Validation] = Seq(MissingConcept, MissingElement, MissingType, MissingRoleType, MissingArcroleType, MissingAnyElem)
}
