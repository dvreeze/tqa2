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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Rule
import eu.cdevreeze.tqa2.validate.Taxonomies
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult

/**
 * "Entrypoint schema validations", checking for at most one level of "incompleteness", and checking that xs:import
 * schemaLocations and clink:linkbaseRefs in incomplete documents are not "dead" links.
 *
 * In the locator-free model, there are 2 kinds of schema documents, namely standalone ones and incomplete ones.
 * The standalone ones contain no xs:import elements with schemaLocation attribute, and no clink:linkbaseRef elements.
 * Schem documents that are not standalone are called incomplete. Generalizing to any taxonomy document, a standalone
 * document is either a standalone schema, or any locator-free linkbase (which is by definition standalone). An incomplete
 * document is any document that is not standalone (and therefore an incomplete schema).
 *
 * Note that standalone documents have no URL references at all. There are no XLink locators, no XLink simple links,
 * no xs:include elements, no xbrldt:typedDomainRef attributes, etc.
 *
 * The locator-free model allows for only "one level of incompleteness". That is, an incomplete schema must refer only
 * to standalone documents via xs:import schemaLocation attributes or clink:linkbaseRef hrefs, and not to other incomplete
 * documents.
 *
 * Entrypoint documents in the locator-free model are typically incomplete documents, although this is not necessarily the case.
 *
 * The most common case is to have precisely one entrypoint document making yp an entrypoint. In that case the entrypoint
 * document would be an incomplete schema, and all other (schema and locator-free linkbase) documents would be standalone.
 * The incomplete entrypoint document would refer to all other documents in the DTS in that case. Indeed, in the locator-free
 * model there is no such thing as DTS discovery (other then summing up the entire DTS in incomplete doucments).
 *
 * Another scenario is to have an entrypoint that consists of a "real entrypoint document", which is an incomplete schema
 * document, and one or more standalone documents that are formula linkbases, for example. In this case, as in the most
 * common case, there is just one incomplete document, and the rule mentioned above would trivially hold.
 *
 * Another scenario is extension taxonomies. There would be one "stable" entrypoint document, which is an incomplete
 * document. There would also be an extension entrypoint document, which would also be an incomplete document. We would
 * like to have the 2nd document refer to the first one, but that would violate the above-mentioned rule if an xs:import
 * with schemaLocation attribute were used for that. So we would use an xs:import without schemaLocation attribute
 * to have the 2nd entrypoint document refer to the first one. "The system" would enforce that the namespace in this
 * xs:import element would be the target namespace of some document, and that would indeed be the case, since the first
 * document indeed has that target namespace. The entrypoint would then be both incomplete documents taken together,
 * and together they would indeed sum up the entire DTS (except for these 2 documents themselves).
 *
 * Note that the "one level of incompletenss" rule also rules out any circular dependencies among documents.
 *
 * @author Chris de Vreeze
 */
object EntrypointSchemaValidations {

  val missingImportTarget = "Missing target of xs:import schemaLocation"

  val missingCLinkbaseRefTarget = "Missing target of clink:LinkbaseRef href"

  val incompleteDocReferringToAnotherIncompleteDocNotAllowedRule = "Incomplete documents must only refer to standalone documents"

  // TODO Entrypoint completeness check, in that entrypoint xs:import elements without schemaLocation "join" with other schemas in the entrypoint

  object MissingImportTarget extends Validation {

    def rule: Rule = missingImportTarget

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val importTargets: Seq[URI] = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElems(_.name == ENames.XsImportEName))
        .flatMap(_.attrOption(ENames.SchemaLocationEName))
        .map(URI.create)
        .distinct

      val missingImportTargets = importTargets.filter(uri => taxo.taxonomyBase.rootElemMap.get(uri).isEmpty)

      missingImportTargets.sortBy(_.toString).map(uri => ValidationResult(rule, "Missing target of xs:import", uri))
    }
  }

  object MissingLinkbaseTarget extends Validation {

    def rule: Rule = missingCLinkbaseRefTarget

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val linkbaseRefTargets: Seq[URI] = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .flatMap(_.filterDescendantElems(_.name == ENames.CLinkLinkbaseRefEName))
        .flatMap(_.attrOption(ENames.HrefEName))
        .map(URI.create)
        .distinct

      val missingLinkbaseRefTargets = linkbaseRefTargets.filter(uri => taxo.taxonomyBase.rootElemMap.get(uri).isEmpty)

      missingLinkbaseRefTargets.sortBy(_.toString).map(uri => ValidationResult(rule, "Missing target of clink:linkbaseRef", uri))
    }
  }

  object IncompleteDocReferringToAnotherIncompleteDocNotAllowed extends Validation {

    def rule: Rule = incompleteDocReferringToAnotherIncompleteDocNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val incompleteDocs: Seq[TaxonomyElem] = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .filter(e => Taxonomies.canBeIncompleteLocFreeDocument(e))

      val incompleteDocUris: Set[URI] = incompleteDocs.map(_.docUri).toSet

      val violatingDocs = incompleteDocs
        .filter { docElem =>
          val importedDocUris: Set[URI] = docElem
            .filterDescendantElems(_.name == ENames.XsImportEName)
            .flatMap(_.attrOption(ENames.XsiSchemaLocationEName))
            .map(URI.create)
            .toSet

          val referendedLinkbaseUris: Set[URI] = docElem
            .filterDescendantElems(_.name == ENames.CLinkLinkbaseRefEName)
            .flatMap(_.attrOption(ENames.HrefEName))
            .map(URI.create)
            .toSet

          val referencedDocUris: Set[URI] = importedDocUris.union(referendedLinkbaseUris)

          val referencedDocs: Seq[TaxonomyElem] = referencedDocUris.toSeq.flatMap(uri => taxo.taxonomyBase.rootElemMap.get(uri))

          referencedDocs.filter(e => incompleteDocUris.contains(e.docUri)).nonEmpty
        }

      violatingDocs.map(_.docUri).distinct.map { uri =>
        ValidationResult(rule, "Incomplete document referring to another incomplete document not allowed", uri)
      }
    }
  }

  val all: Seq[Validation] = {
    Seq(MissingImportTarget, MissingLinkbaseTarget, IncompleteDocReferringToAnotherIncompleteDocNotAllowed)
  }
}
