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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.GlobalElementDeclaration
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Rule
import eu.cdevreeze.tqa2.validate.Taxonomies
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult
import eu.cdevreeze.yaidom2.core.EName

/**
 * Taxonomy schema validations, checking that xs:include is not used, that all schemas have unique target namespaces,
 * that xbrldt:typedDomainRef is not used, and that all cxbrldt:typedDomainKey attributes are not "dead" links.
 *
 * @author Chris de Vreeze
 */
object SchemaValidations {

  val includeNotAllowedRule: Rule = "Element xs:include not allowed"

  val tnsRequiredRule: Rule = "Missing targetNamespace attribute not allowed"

  val tnsUniqueRule: Rule = "Non-unique targetNamespace attribute across schemas not allowed"

  val typedDomainRefNotAllowedRule: Rule = "Attribute xbrldt:typedDomainRef not allowed"

  val missingTypedDomainNotAllowedRule: Rule = "Missing typed domain not allowed"

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
   *
   * Note that the targetNamespace attribute is very important in the locator-free model, because URIs are banned (except
   * in entrypoints), so namespaces play a far more central role in the locator-free model than in regular taxonomies.
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

  /**
   * Validation that checks that all schemas (with target namespace) have a unique target namespace, so that no 2 schemas have the
   * same target namespace.
   *
   * Again note that the targetNamespace attribute is very important in the locator-free model, because URIs are banned (except
   * in entrypoints), so namespaces play a far more central role in the locator-free model than in regular taxonomies.
   */
  object NonUniqueTargetNamespace extends Validation {

    def rule: Rule = tnsUniqueRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val schemasWithTns = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .filter(_.targetNamespaceOption.nonEmpty)

      val schemasGroupedByTns: Map[String, Seq[XsSchema]] = schemasWithTns.groupBy(_.targetNamespaceOption.get)

      val duplicateTnses: Set[String] = schemasGroupedByTns.filter(_._2.sizeIs >= 2).keySet

      duplicateTnses.toSeq.sorted.map(tns => ValidationResult(rule, "Non-unique targetNamespace attribute", tns))
    }
  }

  object TypedDomainRefNotAllowed extends Validation {

    def rule: Rule = typedDomainRefNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val violatingElems = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterGlobalElementDeclarations(_.attrOption(ENames.XbrldtTypedDomainRefEName).nonEmpty))

      violatingElems
        .map(_.docUri)
        .distinct
        .map(uri => ValidationResult(rule, "Concept declarations with xbrdt:typedDomainRef attribute found but not allowed", uri))
    }
  }

  object MissingTypedDomain extends Validation {

    def rule: Rule = missingTypedDomainNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val typedDimensions: Seq[GlobalElementDeclaration] = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.findAllGlobalElementDeclarations)
        .filter(_.attrOption(ENames.CXbrldtTypedDomainKeyEName).nonEmpty)

      val missingTypedDomains: Seq[EName] =
        typedDimensions
          .map(_.attrAsResolvedQName(ENames.CXbrldtTypedDomainKeyEName))
          .distinct
          .filter(e => taxo.findGlobalElementDeclaration(e).isEmpty)

      missingTypedDomains.map(e => ValidationResult(rule, "Missing typed domain", e))
    }
  }

  val all: Seq[Validation] =
    Seq(IncludeNotAllowed, MissingTargetNamespace, NonUniqueTargetNamespace, TypedDomainRefNotAllowed, MissingTypedDomain)
}
