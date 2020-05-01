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
import eu.cdevreeze.tqa2.common.taxoutils.Taxonomies
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Rule
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult

/**
 * "Entrypoint schema validations", checking for at most one level of "connectivity", and checking that xs:import
 * schemaLocations and clink:linkbaseRefs in root documents are not "dead" links.
 *
 * In the locator-free model, there are 2 kinds of schema documents, namely standalone ones and root ones.
 * The standalone ones contain no xs:import elements with schemaLocation attribute, and no clink:linkbaseRef elements.
 * Schem documents that are not standalone are called root documents. Generalizing to any taxonomy document, a standalone
 * document is either a standalone schema, or any locator-free linkbase (which is by definition standalone). A root
 * document is any document that is not standalone (and therefore a root schema).
 *
 * Note that standalone documents have no URL references at all. There are no XLink locators, no XLink simple links,
 * no xs:include elements, no xbrldt:typedDomainRef attributes, etc.
 *
 * The locator-free model allows for only "one level of connectivity". That is, a root schema must refer only
 * to standalone documents via xs:import schemaLocation attributes or clink:linkbaseRef hrefs, and not to other root
 * documents.
 *
 * Entrypoint documents in the locator-free model are typically root documents, although this is not necessarily the case.
 *
 * The most common case is to have precisely one entrypoint document making yp an entrypoint. In that case the entrypoint
 * document would be a root schema, and all other (schema and locator-free linkbase) documents would be standalone.
 * The root entrypoint document would refer to all other documents in the DTS in that case. Indeed, in the locator-free
 * model there is no such thing as DTS discovery (other then summing up the entire DTS in root documents).
 *
 * Another scenario is to have an entrypoint that consists of a "real entrypoint document", which is a root schema
 * document, and one or more standalone documents that are formula linkbases, for example. In this case, as in the most
 * common case, there is just one root document, and the rule mentioned above would trivially hold.
 *
 * Another scenario is extension taxonomies. There would be one "stable" entrypoint document, which is a root
 * document. There would also be an extension entrypoint document, which would also be a root document. We would
 * like to have the 2nd document refer to the first one, but that would violate the above-mentioned rule if an xs:import
 * with schemaLocation attribute were used for that. So we would use an xs:import without schemaLocation attribute
 * to have the 2nd entrypoint document refer to the first one. "The system" would enforce that the namespace in this
 * xs:import element would be the target namespace of some document, and that would indeed be the case, since the first
 * document indeed has that target namespace. The entrypoint would then be both root documents taken together,
 * and together they would indeed sum up the entire DTS (except for these 2 documents themselves).
 *
 * Note that the "one level of connectivity" rule also rules out any circular dependencies among documents.
 *
 * @author Chris de Vreeze
 */
object EntrypointSchemaValidations {

  val missingImportTargetNotAllowedRule = "Missing target of xs:import schemaLocation not allowed"

  val missingCLinkbaseRefTargetNotAllowedRule = "Missing target of clink:LinkbaseRef href not allowed"

  val rootDocReferringToAnotherRootDocNotAllowedRule = "Root documents must only refer to standalone documents"

  val closedSchemaSetRule =
    "The schema set imported with location from a root schema must be closed under imported/target namespaces"

  // TODO Entrypoint completeness check, in that entrypoint xs:import elements without schemaLocation "join" with other schemas in the entrypoint

  object MissingImportTarget extends Validation {

    def rule: Rule = missingImportTargetNotAllowedRule

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

    def rule: Rule = missingCLinkbaseRefTargetNotAllowedRule

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

  object RootDocReferringToAnotherRootDocNotAllowed extends Validation {

    def rule: Rule = rootDocReferringToAnotherRootDocNotAllowedRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val rootDocs: Seq[TaxonomyElem] = taxo.rootElems
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .filter(e => Taxonomies.canBeLocFreeRootDocument(e))

      val rootDocUris: Set[URI] = rootDocs.map(_.docUri).toSet

      val violatingDocs = rootDocs
        .filter { docElem =>
          val importedDocUris: Set[URI] = docElem
            .filterDescendantElems(_.name == ENames.XsImportEName)
            .flatMap(_.attrOption(ENames.SchemaLocationEName))
            .map(URI.create)
            .toSet

          val referendedLinkbaseUris: Set[URI] = docElem
            .filterDescendantElems(_.name == ENames.CLinkLinkbaseRefEName)
            .flatMap(_.attrOption(ENames.HrefEName))
            .map(URI.create)
            .toSet

          val referencedDocUris: Set[URI] = importedDocUris.union(referendedLinkbaseUris)

          val referencedDocs: Seq[TaxonomyElem] = referencedDocUris.toSeq.flatMap(uri => taxo.taxonomyBase.rootElemMap.get(uri))

          referencedDocs.filter(e => rootDocUris.contains(e.docUri)).nonEmpty
        }

      violatingDocs.map(_.docUri).distinct.map { uri =>
        ValidationResult(rule, "Root document referring to another root document not allowed", uri)
      }
    }
  }

  object NotAClosedSchemaSet extends Validation {

    def rule: Rule = closedSchemaSetRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val rootSchemas: Seq[XsSchema] = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .filter(Taxonomies.canBeLocFreeRootDocument)

      rootSchemas.sortBy(_.toString).flatMap(e => validateRootSchema(e, taxo))
    }

    def validateRootSchema(rootSchema: XsSchema, taxo: BasicTaxonomy): Seq[ValidationResult] = {
      val importedSchemasViaLocation: Seq[XsSchema] = findAllImportedSchemasViaSchemaLocation(rootSchema, taxo)

      val targetNamespaces: Set[String] = importedSchemasViaLocation.flatMap(_.targetNamespaceOption).toSet

      val importedNamespaces: Set[String] = importedSchemasViaLocation
        .flatMap(_.findAllImports)
        .map(_.attr(ENames.NamespaceEName))
        .filter(Taxonomies.isProperTaxonomySchemaNamespace)
        .toSet

      val missingTargetNamespaces: Set[String] = importedNamespaces.diff(targetNamespaces)

      missingTargetNamespaces.toSeq.sorted.map { ns =>
        ValidationResult(rule, "Not a closed schema set due to missing TNS", Result(rootSchema.docUri, ns))
      }
    }

    private def findAllImportedSchemasViaSchemaLocation(rootSchema: XsSchema, taxo: BasicTaxonomy): Seq[XsSchema] = {
      val uris: Seq[URI] = rootSchema.findAllImports
        .flatMap(_.attrOption(ENames.SchemaLocationEName))
        .map(URI.create)
        .filter(u => Taxonomies.isProperTaxonomyDocumentUri(u))

      uris.flatMap(uri => taxo.taxonomyBase.findElemByUri(uri)).collect { case e: XsSchema => e }
    }

    final case class Result(rootSchemaUri: URI, missingTargetNamespace: String)
  }

  val all: Seq[Validation] = {
    Seq(MissingImportTarget, MissingLinkbaseTarget, RootDocReferringToAnotherRootDocNotAllowed, NotAClosedSchemaSet)
  }
}
