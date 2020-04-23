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

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.tqa2.internal.xmlutil.NodeBuilderUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.indexed
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.node.simple
import eu.cdevreeze.yaidom2.queryapi.ScopedElemApi
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor

/**
 * Converter from standard taxonomy entrypoint schema documents to locator-free taxonomy entrypoint schema documents.
 *
 * @author Chris de Vreeze
 */
final class EntrypointSchemaConverter(
    val namespacePrefixMapper: NamespacePrefixMapper,
    val documentENameExtractor: DocumentENameExtractor) {

  private val nodeBuilderUtil: NodeBuilderUtil = new NodeBuilderUtil(namespacePrefixMapper, documentENameExtractor)

  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(namespacePrefixMapper)

  import elemCreator._
  import nodebuilder.NodeBuilderCreator._

  private val nonEntrypointSchemaConverter: NonEntrypointSchemaConverter =
    new NonEntrypointSchemaConverter(namespacePrefixMapper, documentENameExtractor)

  /**
   * Converts an entrypoint schema in the given (2nd parameter) TaxonomyBase to its locator-free counterparts, resulting in
   * a locator-free XsSchema returned by this function. Only "non-core" (entrypoint) schemas should be converted by this function.
   *
   * The input TaxonomyBase parameter (2nd parameter) should be closed under DTS discovery rules.
   *
   * The result entrypoint schema must explicitly sum up the entire DTS.
   */
  def convertSchema(inputSchema: standardtaxonomy.dom.XsSchema, inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase): XsSchema = {
    val dtsUriCollector = standardtaxonomy.taxonomy.builder.DefaultDtsUriCollector.instance

    val inputDtsUris: Set[URI] = dtsUriCollector.findAllDtsUris(
      Set(inputSchema.docUri), { uri =>
        inputTaxonomyBase.rootElemMap.getOrElse(uri, sys.error(s"Document '$uri' not found"))
      }
    )

    createEntrypoint(inputDtsUris.diff(Set(inputSchema.docUri)), inputSchema, inputTaxonomyBase)
  }

  // TODO Add new core schemas of locator-free model.

  private def createEntrypoint(
      dtsUrisWithoutEntrypoint: Set[URI],
      inputSchema: standardtaxonomy.dom.XsSchema,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase): XsSchema = {
    val linkbaseUris: Set[URI] =
      inputTaxonomyBase.rootElemMap.view
        .filterKeys(dtsUrisWithoutEntrypoint)
        .collect { case (docUri, _: standardtaxonomy.dom.Linkbase) => docUri }
        .toSet

    val schemas: Map[URI, standardtaxonomy.dom.XsSchema] =
      inputTaxonomyBase.rootElemMap.view
        .filterKeys(dtsUrisWithoutEntrypoint)
        .collect { case (docUri, e: standardtaxonomy.dom.XsSchema) => docUri -> e }
        .toMap

    val schemaUris: Set[URI] = schemas.keySet

    require(ScopedElemApi.containsNoConflictingScopes(inputSchema), s"Conflicting scopes not allowed (document ${inputSchema.docUri})")

    val parentScope: PrefixedScope = PrefixedScope.from(
      namespacePrefixMapper.getPrefix(Namespaces.XsNamespace) -> Namespaces.XsNamespace,
      namespacePrefixMapper.getPrefix(Namespaces.CLinkNamespace) -> Namespaces.CLinkNamespace
    )

    require(
      inputSchema.findAllDescendantElemsOrSelf.forall(_.scope.defaultNamespaceOption.isEmpty),
      s"Currently no default namespace is allowed in any input schema (violation: ${inputSchema.docUri})"
    )

    val tns = inputSchema.targetNamespaceOption.getOrElse(sys.error(s"Missing targetNamespace in schema '${inputSchema.docUri}''"))

    val rawSchemaElem: nodebuilder.Elem = emptyElem(ENames.XsSchemaEName, parentScope).creationApi
      .plusAttribute(ENames.TargetNamespaceEName, tns)
      .plusAttributeOption(ENames.IdEName, inputSchema.attrOption(ENames.IdEName))
      .plusAttributeOption(ENames.AttributeFormDefaultEName, inputSchema.attrOption(ENames.AttributeFormDefaultEName))
      .plusAttributeOption(ENames.ElementFormDefaultEName, inputSchema.attrOption(ENames.ElementFormDefaultEName))
      .plusChild(wrapInAnnotation(
        linkbaseUris.toSeq.sortBy(_.toString).map { linkbaseUri =>
          emptyElem(ENames.CLinkLinkbaseRefEName, parentScope).creationApi
            .plusAttribute(ENames.HrefEName, linkbaseUri.toString)
            .underlying
        },
        parentScope
      ))
      .plusChildren(schemaUris.toSeq.sortBy(_.toString).map { schemaUri =>
        emptyElem(ENames.XsImportEName, parentScope).creationApi
          .plusAttribute(
            ENames.NamespaceEName,
            schemas.getOrElse(schemaUri, sys.error(s"Missing schema '$schemaUri'")).targetNamespaceOption.get)
          .plusAttribute(ENames.SchemaLocationEName, schemaUri.toString)
          .underlying
      })
      .plusChildren(inputSchema.findAllChildElems.flatMap {
        case _: standardtaxonomy.dom.Annotation =>
          Seq.empty
        case _: standardtaxonomy.dom.Import =>
          Seq.empty
        case elemDecl: standardtaxonomy.dom.GlobalElementDeclaration =>
          // For EBA taxonomy
          Seq(nonEntrypointSchemaConverter.convertGlobalElementDeclaration(elemDecl, inputTaxonomyBase, parentScope))
        case che =>
          // TODO Make sure no default namespace is used or that it is "converted away"
          Seq(nodebuilder.Elem.from(che).creationApi.usingNonConflictingParentScope(parentScope).underlyingElem)
      })
      .underlying
      .transformChildElemsToNodeSeq(e => removeIfEmptyAnnotation(e).toSeq)

    val sanitizedSchemaElem = nodeBuilderUtil.sanitizeAndPrettify(nodebuilder.Elem.from(rawSchemaElem))
    makeSchema(inputSchema.docUriOption, sanitizedSchemaElem)
  }

  private def makeSchema(docUriOption: Option[URI], schemaRootElem: nodebuilder.Elem): XsSchema = {
    // Indexed elements not efficient, but great for debugging
    TaxonomyElem(indexed.Elem.ofRoot(docUriOption, simple.Elem.from(schemaRootElem))).asInstanceOf[XsSchema]
  }

  private def removeIfEmptyAnnotation(elem: nodebuilder.Elem): Option[nodebuilder.Elem] = {
    elem.name match {
      case ENames.XsAnnotationEName =>
        val onlyAppinfo = elem.findAllChildElems.forall(_.name == ENames.XsAppinfoEName)

        if (onlyAppinfo && elem.findAllChildElems.forall(_.findAllChildElems.isEmpty)) {
          None
        } else {
          Some(elem)
        }
      case _ =>
        Some(elem)
    }
  }

  private def wrapInAnnotation(elems: Seq[nodebuilder.Elem], parentScope: PrefixedScope): nodebuilder.Elem = {
    emptyElem(ENames.XsAnnotationEName, parentScope).creationApi
      .plusChild(emptyElem(ENames.XsAppinfoEName, parentScope))
      .underlying
      .transformDescendantElems {
        case e: nodebuilder.Elem if e.name == ENames.XsAppinfoEName =>
          e.creationApi.plusChildren(elems).underlying
        case e => e
      }
  }
}
