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
 * Taxonomy schema validations.
 *
 * @author Chris de Vreeze
 */
object SchemaValidations {

  val includeNotAllowedRule: Rule = "xs:include not allowed"

  val tnsRequiredRule: Rule = "Missing targetNamespace attribute"

  val typedDomainRefNotAllowedRule: Rule = "xbrldt:typedDomainRef attribute not allowed"

  object IncludeNotAllowed extends Validation {

    def rule: Rule = includeNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val includes = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElemsOrSelf(_.name == ENames.XsIncludeEName))

      includes.map(_.docUri).distinct.map(uri => ValidationResult(rule, "xs:include elements found but not allowed", uri))
    }
  }

  /**
   * Validation that checks that all proper taxonomy schemas have a targetNamespace attribute. This implicitly checks that
   * chameleon schemas cannot occur. The latter is important, because chameleon schemas have no target namespace in isolation
   * so cannot be referred to from taxonomy element keys elsewhere, unless a very costly implementation is made to support
   * that "use case". Similar problems with chameleon schemas apply to regular taxonomies.
   */
  object MissingTargetNamespace extends Validation {

    def rule: Rule = tnsRequiredRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val schemasWithoutTns = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .filter(_.targetNamespaceOption.isEmpty)

      schemasWithoutTns.map(_.docUri).distinct.map(uri => ValidationResult(rule, "Missing targetNamespace attribute", uri))
    }
  }

  object TypedDomainRefNotAllowed extends Validation {

    def rule: Rule = typedDomainRefNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val violatingElems = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterGlobalElementDeclarations(_.attrOption(ENames.XbrldtTypedDomainRefEName).nonEmpty))

      violatingElems.map(_.docUri).distinct
        .map(uri => ValidationResult(rule, "Concept declarations with xbrdt:typedDomainRef attribute found but not allowed", uri))
    }
  }

  val all: Seq[Validation] = Seq(IncludeNotAllowed, MissingTargetNamespace, TypedDomainRefNotAllowed)
}
