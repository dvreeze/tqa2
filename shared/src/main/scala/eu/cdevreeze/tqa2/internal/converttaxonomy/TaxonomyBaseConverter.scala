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

package eu.cdevreeze.tqa2.internal.converttaxonomy

import java.net.URI
import java.util.regex.Pattern

import eu.cdevreeze.tqa2.common.xmlschema.SubstitutionGroupMap
import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.locfreetaxonomy
import eu.cdevreeze.tqa2.validate.Taxonomies
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor

/**
 * Converter from standard taxonomies to locator-free taxonomies as TaxonomyBase instances.
 *
 * @author Chris de Vreeze
 */
final class TaxonomyBaseConverter(
    val xlinkResourceConverter: XLinkResourceConverter,
    val namespacePrefixMapper: NamespacePrefixMapper,
    val documentENameExtractor: DocumentENameExtractor) {

  /**
   * Checks if the conversion through method convertTaxonomyBaseIgnoringEntrypoints (with the same parameters as this method)
   * can succeed. Nothing is returned on success. An exception is thrown otherwise.
   *
   * The checks apply to all documents, including "core taxonomy documents" (e.g. www.xbrl.org ones), unless specified otherwise.
   * Unless applicable, the entrypoint exclusion filter is not used to filter away documents for checking.
   *
   * One check validates that document root elements are xs:schema or link:linkbase and vice versa. Embedded linkbases are
   * therefore not allowed.
   *
   * Another check validates that all schema documents have a non-empty targetNamespace attribute, that these target namespaces are unique
   * across the taxonomy, and that xs:include is not used anywhere.
   *
   * It is not checked that XLink arcs point to existing locators or resources in the same extended link. Neither is it
   * checked that XLink locators, XLink simple links and xs:import schemaLocation attributes are not "dead links".
   */
  def checkInputTaxonomyBaseIgnoringEntrypoints(
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      excludedEntrypointFilter: URI => Boolean): Unit = {

    val rootElems: Seq[standardtaxonomy.dom.TaxonomyElem] = inputTaxonomyBase.rootElems
    val schemas: Seq[standardtaxonomy.dom.XsSchema] = inputTaxonomyBase.findAllXsdSchemas
    val linkbases: Seq[standardtaxonomy.dom.Linkbase] = inputTaxonomyBase.findAllLinkbases

    // TODO Check that all documents have no conflicting scopes

    // Document root elements must be xs:schema or link:linkbase.

    val nonRootElemDocElems: Seq[standardtaxonomy.dom.TaxonomyElem] = rootElems.filter(!_.isRootElement)

    if (nonRootElemDocElems.nonEmpty) {
      throw new IllegalStateException(s"Not all documents are schemas or linkbases (e.g. ${nonRootElemDocElems.head.docUri})")
    }

    // All xs:schema and link:linkbase elements must be document roots. Hence embedded linkbases are not allowed.

    val nonRootSchemas: Seq[standardtaxonomy.dom.XsSchema] = schemas.filter(_.underlyingElem.findParentElem.nonEmpty)
    val nonRootLinkbases: Seq[standardtaxonomy.dom.Linkbase] = linkbases.filter(_.underlyingElem.findParentElem.nonEmpty)

    if (nonRootSchemas.nonEmpty) {
      throw new IllegalStateException(s"Not all xs:schema elements are document root elements (e.g. in ${nonRootSchemas.head.docUri})")
    }
    if (nonRootLinkbases.nonEmpty) {
      // So embedded linkbases are also not allowed
      throw new IllegalStateException(
        s"Not all link:linkbase elements are document root elements (e.g. in ${nonRootLinkbases.head.docUri})")
    }

    // All schemas must have a (non-empty) targetNamespace attribute.

    val schemasWithoutTns: Seq[standardtaxonomy.dom.XsSchema] = schemas.filter(e => e.targetNamespaceOption.forall(_.trim.isEmpty))

    if (schemasWithoutTns.nonEmpty) {
      throw new IllegalStateException(s"Not all schemas have a (non-empty) targetNamespace (e.g. ${schemasWithoutTns.head.docUri})")
    }

    // No xs:include elements are allowed.

    val xsIncludes: Seq[standardtaxonomy.dom.TaxonomyElem] = schemas.flatMap(_.filterDescendantElemsOrSelf(_.name == ENames.XsIncludeEName))

    if (xsIncludes.nonEmpty) {
      throw new IllegalStateException(s"Element(s) xs:include not allowed (e.g. in ${xsIncludes.head.docUri})")
    }

    // No 2 different schema documents are allowed to have the same target namespace.

    val schemasByTns: Map[String, Seq[standardtaxonomy.dom.XsSchema]] = schemas.groupBy(_.targetNamespaceOption.ensuring(_.nonEmpty).get)

    if (schemasByTns.exists(_._2.sizeIs >= 2)) {
      val aViolatingTns: String = schemasByTns.find(_._2.sizeIs >= 2).get._1
      throw new IllegalStateException(s"Not all schema documents have a unique targetNamespace (e.g. for TNS '$aViolatingTns')")
    }
  }

  /**
   * Converts all non-entrypoint documents in the input TaxonomyBase to their locator-free counterparts, resulting in
   * a locator-free TaxonomyBase returned by this function. Only "non-core" taxonomy documents are converted.
   *
   * The input TaxonomyBase should be closed under DTS discovery rules, if we want to add entrypoints with method
   * addSingleDocumentEntrypoint later.
   *
   * The conversion starts with calling method checkInputTaxonomyBaseIgnoringEntrypoints, so fails early on errors.
   */
  def convertTaxonomyBaseIgnoringEntrypoints(
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      excludedEntrypointFilter: URI => Boolean): locfreetaxonomy.taxonomy.TaxonomyBase = {

    checkInputTaxonomyBaseIgnoringEntrypoints(inputTaxonomyBase, excludedEntrypointFilter)

    val nonEntrypointSchemaConverter: NonEntrypointSchemaConverter =
      new NonEntrypointSchemaConverter(namespacePrefixMapper, documentENameExtractor)
    val linkbaseConverter: LinkbaseConverter =
      new LinkbaseConverter(xlinkResourceConverter, namespacePrefixMapper, documentENameExtractor)

    val rootElems: Seq[locfreetaxonomy.dom.TaxonomyElem] = inputTaxonomyBase.rootElems
      .filterNot(e => excludedEntrypointFilter(e.docUri))
      .flatMap {
        case e: standardtaxonomy.dom.XsSchema if Taxonomies.isCoreDocumentUri(e.docUri) =>
          println(s"Parsing (not converting) schema '${e.docUri}'") // scalastyle:off
          Some(locfreetaxonomy.dom.TaxonomyElem(e.underlyingElem))
        case e: standardtaxonomy.dom.XsSchema =>
          // TODO Require non-entrypoint schema in terms of content
          println(s"Converting schema '${e.docUri}'") // scalastyle:off
          Some(nonEntrypointSchemaConverter.convertSchema(e, inputTaxonomyBase))
        case e: standardtaxonomy.dom.Linkbase if Taxonomies.isCoreDocumentUri(e.docUri) =>
          println(s"Parsing (not converting) linkbase '${e.docUri}'") // scalastyle:off
          Some(locfreetaxonomy.dom.TaxonomyElem(e.underlyingElem))
        case e: standardtaxonomy.dom.Linkbase =>
          println(s"Converting linkbase '${e.docUri}'") // scalastyle:off
          Some(linkbaseConverter.convertLinkbase(e, inputTaxonomyBase))
        case _ =>
          None
      }

    // Not losing the document URI, but potentially losing top-level comments or processing instructions.
    val documents: Seq[locfreetaxonomy.dom.TaxonomyDocument] = rootElems.map(e => locfreetaxonomy.dom.TaxonomyDocument(e))

    // TODO Add new core schemas for locator-free model

    locfreetaxonomy.taxonomy.TaxonomyBase.build(documents, SubstitutionGroupMap.Empty)
  }

  def addSingleDocumentEntrypoint(
      entrypointDocUri: URI,
      taxonomyBase: locfreetaxonomy.taxonomy.TaxonomyBase,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase
  ): locfreetaxonomy.taxonomy.TaxonomyBase = {
    val entrypointSchemaConverter: EntrypointSchemaConverter =
      new EntrypointSchemaConverter(namespacePrefixMapper, documentENameExtractor)

    val inputEntrypointSchema =
      inputTaxonomyBase.findXsdSchema(_.docUri == entrypointDocUri).getOrElse(sys.error(s"Missing document '$entrypointDocUri'"))

    val entrypointSchema: locfreetaxonomy.dom.XsSchema = entrypointSchemaConverter.convertSchema(inputEntrypointSchema, inputTaxonomyBase)

    locfreetaxonomy.taxonomy.TaxonomyBase.build(
      taxonomyBase.documents.prepended(locfreetaxonomy.dom.TaxonomyDocument(entrypointSchema)),
      taxonomyBase.extraProvidedSubstitutionGroupMap
    )
  }

  def addSingleDocumentEntrypoints(
      entrypointDocUriPattern: Pattern,
      taxonomyBase: locfreetaxonomy.taxonomy.TaxonomyBase,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase
  ): locfreetaxonomy.taxonomy.TaxonomyBase = {
    val entrypointSchemaConverter: EntrypointSchemaConverter =
      new EntrypointSchemaConverter(namespacePrefixMapper, documentENameExtractor)

    val inputEntrypointSchemas =
      inputTaxonomyBase.filterXsdSchemas(e => entrypointDocUriPattern.matcher(e.docUri.toString).matches)

    val entrypointSchemas: Seq[locfreetaxonomy.dom.XsSchema] = inputEntrypointSchemas.map { inputSchema =>
      entrypointSchemaConverter.convertSchema(inputSchema, inputTaxonomyBase)
    }

    locfreetaxonomy.taxonomy.TaxonomyBase.build(
      taxonomyBase.documents.prependedAll(entrypointSchemas.map(e => locfreetaxonomy.dom.TaxonomyDocument(e))),
      taxonomyBase.extraProvidedSubstitutionGroupMap
    )
  }
}

object TaxonomyBaseConverter {

  def apply(
      xlinkResourceConverter: XLinkResourceConverter,
      namespacePrefixMapper: NamespacePrefixMapper,
      documentENameExtractor: DocumentENameExtractor): TaxonomyBaseConverter = {
    new TaxonomyBaseConverter(xlinkResourceConverter, namespacePrefixMapper, documentENameExtractor)
  }
}
