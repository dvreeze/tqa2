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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.LinkbaseRef
import eu.cdevreeze.tqa2.locfreetaxonomy.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa2.validate.Rule
import eu.cdevreeze.tqa2.validate.Taxonomies
import eu.cdevreeze.tqa2.validate.Validation
import eu.cdevreeze.tqa2.validate.ValidationResult

/**
 * Entrypoint schema validations, as well as validations for non-entrypoint schemas.
 *
 * In the locator-free model, most taxonomy documents (schemas and loc-free linkbases) contain no URI references. That is,
 * they contain no schemaLocation attributes in xs:import elements, no xbrldt:typedDomainRef attributes (but an alternative
 * without any URI reference), and certainly no XLink locators and simple links. Yet "entrypoint schemas" should contain
 * URI references, to all (other) documents in the entire DTS. So they are the exception to the rule that in the locator-free
 * model there are no URI references anywhere.
 *
 * Let's first define the notion of a "proper entrypoint schema". Regardless of whether the document is used as an entrypoint
 * of any DTS or not, a proper entrypoint schema is any (non-core) loc-free taxonomy schema in which there is at least one xs:import element
 * that contains a schemaLocation attribute, or in which there is at least one loc-free linkbaseRef (which is not an XLink
 * simple link, unlike its counterpart in regular taxonomies).
 *
 * An entrypoint of a DTS may contain any (non-core) taxonomy document (such as a formula linkbase), but only proper entrypoint schemas
 * contribute more to the DTS than themselves. Locator-free linkbases never contribute more to the DTS than themselves.
 *
 * In the locator-free model, it is required that "DTS discovery" is no more involved than combining the entrypoint documents,
 * and, for proper entrypoint schemas, combining their directly referenced schemas and linkbases as well. It is not allowed
 * for (proper) entrypoint schemas to refer to other (proper entrypoint) schemas, that in turn contribute more documents to
 * the DTS. So, in the locator-free model, "DTS discovery" is a trivial non-recursive process which involves no more documents
 * to analyze than just the documents making up the entrypoint. It has to be such a trivial process, or otherwise we quickly
 * get back to the tangled web of documents in a DTS that we tried so hard to avoid in the locator-free model.
 *
 * But how does this help support extension taxonomies? See below for more on that.
 *
 * Let's give some example DTS entrypoint scenarios, as supported by the locator-free model.
 *
 * The most common scenario is that of one proper entrypoint schema used as the entrypoint of the DTS. It contains xs:import
 * elements with schemaLocations referring to all other schemas in the DTS, as well as (locator-free) linkbaseRefs to all
 * (locator-free) linkbases in the DTS. In this scenario, all other schemas are required to be no proper entrypoint schemas,
 * so they do not contain any URI references.
 *
 * A common scenario during taxonomy development is that of one proper entrypoint schema, along with a formula linkbase (or
 * table linkbase, etc.), both together making up the entrypoint of the DTS. Again, all other schemas must contain no URI
 * references, in particular no xs:import schemaLocation attributes.
 *
 * Another scenario is extension taxonomies. We would like to express that an entrypoint schema not only refers to some
 * extension taxonomy documents, but also to an underlying entrypoint that pulls in most of the DTS. We cannot do that,
 * unfortunately, due to the restriction above that entrypoints cannot refer to other entrypoints via URI references.
 * What we can do, however, is the following: the extension taxonomy proper entrypoint schema contains xs:import elements
 * with schemaLocation (as well as linkbaseRefs) for the extension documents, and one xs:import element without schemaLocation
 * (but with namespace, of course) for the underlying entrypoint. This underlying entrypoint is the second proper entrypoint
 * schema, which refers to the underlying DTS, to be combined with the extension. In other words, the entrypoint is 2 proper
 * entrypoint schemas, "joined" (as in database joins) on the target namespace, and not on any URI reference.
 *
 * This leads to additional validations. If a proper entrypoint schema is part of the entrypoint of a DTS, it must have
 * schemaLocation attributes in all its xs:import elements, except for the "joined" (proper) entrypoint schemas, joined on
 * target namespace.
 *
 * In summary, like for regular taxonomies there are multiple possible schemes for (single or multiple) document entrypoints,
 * but unlike regular taxonomies there are restrictions in place that limit URI references only to (proper) entrypoints.
 *
 * @author Chris de Vreeze
 */
object EntrypointSchemaValidations {

  val importSchemaLocationNotAllowedInNonEntrypointRule: Rule = "Attribute schemaLocation not allowed in xs:import in non-entrypoints"

  val linkbaseRefNotAllowedInNonEntrypointRule: Rule = "LinkbaseRef not allowed in non-entrypoints"

  val importSchemaLocationToEntrypointSchemaNotAllowedInEntrypointRule: Rule =
    "Attribute schemaLocation referring to other entrypoint schema not allowed in xs:import in entrypoints"

  // TODO Entrypoint completeness check, in that entrypoint xs:import elements without schemaLocation "join" with other schemas in the entrypoint

  final case class ImportSchemaLocationNotAllowedInNonEntrypoint(entrypoint: Set[URI]) extends Validation {

    def rule: Rule = importSchemaLocationNotAllowedInNonEntrypointRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val violatingImports = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .filterNot(e => entrypoint.contains(e.docUri))
        .flatMap(_.findAllImports)
        .filter(_.attrOption(ENames.SchemaLocationEName).nonEmpty)

      violatingImports.map(_.docUri).distinct.map { uri =>
        ValidationResult(rule, "Attribute schemaLocation found but not allowed in xs:import in non-entrypoint", uri)
      }
    }
  }

  final case class LinkbaseRefNotAllowedInNonEntrypoint(entrypoint: Set[URI]) extends Validation {

    def rule: Rule = linkbaseRefNotAllowedInNonEntrypointRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val linkbaseRefs = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .filterNot(e => entrypoint.contains(e.docUri))
        .flatMap(_.filterDescendantElems(_.name == ENames.CLinkLinkbaseRefEName))
        .collect { case e: LinkbaseRef => e }

      linkbaseRefs.map(_.docUri).distinct.map { uri =>
        ValidationResult(rule, "LinkbaseRef found but not allowed in non-entrypoint", uri)
      }
    }
  }

  /**
   * Validation that checks that entrypoint schemas do not refer (via schemaLocations) to other schemas in the same entrypoint.
   * Along with the validations that check that schemas outside the entrypoint contain neither linkbaseRefs nor xs:import schemaLocations,
   * it is made sure that no schemas refer to other schemas that refer to linkbases or other schemas.
   */
  final case class ImportSchemaLocationToOtherEntrypointSchemaNotAllowedInEntrypoint(entrypoint: Set[URI]) extends Validation {

    def rule: Rule = importSchemaLocationToEntrypointSchemaNotAllowedInEntrypointRule

    def validationFunction: BasicTaxonomy => Seq[ValidationResult] = { taxo =>
      val violatingImports = taxo.findAllXsdSchemas
        .filter(Taxonomies.isProperTaxonomyDocumentContent)
        .filter(e => entrypoint.contains(e.docUri))
        .flatMap(_.findAllImports)
        .filter(e => e.attrOption(ENames.SchemaLocationEName).exists(u => entrypoint.contains(e.baseUri.resolve(URI.create(u)))))

      violatingImports.map(_.docUri).distinct.map { uri =>
        ValidationResult(rule,
                         "Attribute schemaLocation referring to other entrypoint schema found but not allowed in xs:import in entrypoint",
                         uri)
      }
    }
  }

  def all(entrypoint: Set[URI]): Seq[Validation] = {
    Seq(
      ImportSchemaLocationNotAllowedInNonEntrypoint(entrypoint),
      LinkbaseRefNotAllowedInNonEntrypoint(entrypoint),
      ImportSchemaLocationToOtherEntrypointSchemaNotAllowedInEntrypoint(entrypoint)
    )
  }
}
