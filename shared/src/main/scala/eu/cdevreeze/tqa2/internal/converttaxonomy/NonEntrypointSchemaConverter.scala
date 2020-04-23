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
import eu.cdevreeze.tqa2.internal.xmlutil.SimpleElemUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.XsSchema
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.core.QName
import eu.cdevreeze.yaidom2.node.indexed
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.node.simple
import eu.cdevreeze.yaidom2.queryapi.ScopedElemApi
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor

import scala.collection.immutable.ListMap

/**
 * Converter from standard taxonomy non-entrypoint schema documents to locator-free taxonomy schema documents.
 *
 * Non-entrypoint schemas contain no XLink locators (obviously) and they contain no XLink simple links. The latter is
 * easy to see given that non-entrypoint schemas in the locator-free model do not contribute to DTS discovery, so there
 * is no need for linkbaseRef elements etc.
 *
 * Moreover, xs:include is not allowed in the locator-free model, and xs:import elements contain no schemaLocation attribute
 * in the locator-free model. Finally, xbrldt:typedDomainRef attributes are replaced by cxbrldt:typedDomainKey attributes.
 *
 * All in all, all URIs to other taxonomy elements are thus removed from non-entrypoint schemas in the locator-free model.
 *
 * @author Chris de Vreeze
 */
final class NonEntrypointSchemaConverter(
    val namespacePrefixMapper: NamespacePrefixMapper,
    val documentENameExtractor: DocumentENameExtractor) {

  private val nodeBuilderUtil: NodeBuilderUtil = new NodeBuilderUtil(namespacePrefixMapper, documentENameExtractor)

  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(namespacePrefixMapper)

  import elemCreator._
  import nodebuilder.NodeBuilderCreator._

  // TODO Add xs:import for every namespace used in an XML Schema sense (using another DocumentENameExtractor)

  // TODO Convert values of link:usedOn elements in link:roleType and link:arcroleType

  /**
   * Converts a non-entrypoint schema in the given (2nd parameter) TaxonomyBase to its locator-free counterpart, resulting in
   * a locator-free XsSchema returned by this function.
   *
   * The input TaxonomyBase parameter (2nd parameter) should be closed under DTS discovery rules.
   */
  def convertSchema(inputSchema: standardtaxonomy.dom.XsSchema, inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase): XsSchema = {
    require(ScopedElemApi.containsNoConflictingScopes(inputSchema), s"Conflicting scopes not allowed (document ${inputSchema.docUri})")

    val parentScope: PrefixedScope = PrefixedScope.from(
      namespacePrefixMapper.getPrefix(Namespaces.XsNamespace) -> Namespaces.XsNamespace,
      namespacePrefixMapper.getPrefix(Namespaces.XbrldtNamespace) -> Namespaces.XbrldtNamespace
    )

    val editedInputSchema: standardtaxonomy.dom.XsSchema =
      tryToRemoveDefaultXsNamespace(inputSchema, parentScope.getPrefixForNamespace(Namespaces.XsNamespace))

    convertAdaptedSchema(editedInputSchema, inputTaxonomyBase)
  }

  private def convertAdaptedSchema(
      inputSchema: standardtaxonomy.dom.XsSchema,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase): XsSchema = {
    require(ScopedElemApi.containsNoConflictingScopes(inputSchema), s"Conflicting scopes not allowed (document ${inputSchema.docUri})")

    val parentScope: PrefixedScope = PrefixedScope.from(
      namespacePrefixMapper.getPrefix(Namespaces.XsNamespace) -> Namespaces.XsNamespace,
      namespacePrefixMapper.getPrefix(Namespaces.XbrldtNamespace) -> Namespaces.XbrldtNamespace
    )

    require(
      inputSchema.findAllDescendantElemsOrSelf.forall(_.scope.defaultNamespaceOption.isEmpty),
      s"Currently no default namespace is allowed in any (adapted) input schema (violation: ${inputSchema.docUri})"
    )

    val tns =
      inputSchema.targetNamespaceOption.getOrElse(sys.error(s"Missing targetNamespace in schema '${inputSchema.docUri}''"))

    val rawSchemaElem: nodebuilder.Elem = emptyElem(ENames.XsSchemaEName, parentScope).creationApi
      .plusAttribute(ENames.TargetNamespaceEName, tns)
      .plusAttributeOption(ENames.IdEName, inputSchema.attrOption(ENames.IdEName))
      .plusAttributeOption(ENames.AttributeFormDefaultEName, inputSchema.attrOption(ENames.AttributeFormDefaultEName))
      .plusAttributeOption(ENames.ElementFormDefaultEName, inputSchema.attrOption(ENames.ElementFormDefaultEName))
      .plusChildren(inputSchema.findAllChildElems.map {
        case annotation: standardtaxonomy.dom.Annotation =>
          convertAnnotation(annotation, inputTaxonomyBase, parentScope)
        case xsImport: standardtaxonomy.dom.Import =>
          convertImport(xsImport, inputTaxonomyBase, parentScope)
        case elemDecl: standardtaxonomy.dom.GlobalElementDeclaration =>
          convertGlobalElementDeclaration(elemDecl, inputTaxonomyBase, parentScope)
        case che =>
          // TODO Make sure no default namespace is used or that it is "converted away"
          nodebuilder.Elem.from(che).creationApi.usingNonConflictingParentScope(parentScope).underlyingElem
      })
      .underlying
      .transformChildElemsToNodeSeq(e => removeIfEmptyAnnotation(e).toSeq)

    val sanitizedSchemaElem = nodeBuilderUtil.sanitize(nodebuilder.Elem.from(rawSchemaElem))
    makeSchema(inputSchema.docUriOption, sanitizedSchemaElem)
  }

  /**
   * If the given schema consistently uses the default namespace for the XML Schema namespace, it is replaced by a prefixed
   * namespace throughout the tree, using the given prefix. Otherwise the element is returned unaltered (taking up no costly CPU time).
   *
   * QName-valued element text and attribute values are also edited, if the default namespace is used.
   */
  private def tryToRemoveDefaultXsNamespace(inputSchema: standardtaxonomy.dom.XsSchema, xsPrefix: String): standardtaxonomy.dom.XsSchema = {
    if (inputSchema.scope.defaultNamespaceOption.contains(Namespaces.XsNamespace)) {
      val simpleElemUtil: SimpleElemUtil = new SimpleElemUtil(documentENameExtractor)

      val simpleResultElem = simpleElemUtil.tryToRemoveDefaultXsNamespace(indexed.Elem.from(inputSchema), xsPrefix)
      makeStandardSchema(inputSchema.docUriOption, simpleResultElem)
    } else {
      // Hopefully no default namespace has been used at all, so no editing is needed
      inputSchema
    }
  }

  private def convertGlobalElementDeclaration(
      inputGlobalElemDecl: standardtaxonomy.dom.GlobalElementDeclaration,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    var extraScope: PrefixedScope = PrefixedScope.empty

    val attributes: ListMap[EName, String] = inputGlobalElemDecl.attributes.map {
      case (ENames.XbrldtTypedDomainRefEName, attrValue) =>
        val ref: URI = inputGlobalElemDecl.baseUri.resolve(attrValue)

        val typedDomainElemDecl: standardtaxonomy.dom.GlobalElementDeclaration = inputTaxonomyBase
          .getElemByUri(ref)
          .asInstanceOf[standardtaxonomy.dom.GlobalElementDeclaration]

        val typedDomainEName: EName = typedDomainElemDecl.targetEName
        val typedDomainPrefixOption = typedDomainEName.namespaceUriOption.map(ns => namespacePrefixMapper.getPrefix(ns))

        extraScope = extraScope.append(
          typedDomainPrefixOption
            .map(pref => PrefixedScope.from(pref -> typedDomainEName.namespaceUriOption.get))
            .getOrElse(PrefixedScope.empty))

        val typedDomainQName: QName = QName(typedDomainPrefixOption, typedDomainEName.localPart)

        ENames.CXbrldtTypedDomainKeyEName -> typedDomainQName.toString
      case (attrName, attrValue) =>
        val textENameExtractorOption = documentENameExtractor.findAttributeValueENameExtractor(inputGlobalElemDecl, attrName)

        val usedNamespaces: Set[String] = textENameExtractorOption
          .map(_.extractENames(inputGlobalElemDecl.scope.withoutDefaultNamespace, attrValue).flatMap(_.namespaceUriOption))
          .getOrElse(Set.empty)

        extraScope = extraScope.append(usedNamespaces.foldLeft(PrefixedScope.empty) {
          case (accScope, ns) =>
            accScope.append(PrefixedScope.from(namespacePrefixMapper.getPrefix(ns) -> ns))
        })

        // TODO Edit the attribute value (if QName-valued)?
        attrName -> attrValue
    }

    emptyElem(ENames.XsElementEName, parentScope.append(extraScope)).creationApi
      .plusAttributes(attributes)
      .plusChildren(inputGlobalElemDecl.children.map(n => nodebuilder.Node.from(n)))
      .underlying
  }

  private def convertAnnotation(
      inputAnnotation: standardtaxonomy.dom.Annotation,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    // TODO Make sure no default namespace is used or that it is "converted away"

    nodebuilder.Elem.from(inputAnnotation).transformDescendantElemsToNodeSeq { e =>
      e.name match {
        case ENames.LinkLinkbaseRefEName => Seq.empty
        case ENames.LinkSchemaRefEName   => Seq.empty
        case ENames.LinkUsedOnEName =>
          val targetName: EName = NameConversions.convertLinkOrResourceName(e.textAsResolvedQName)

          val extraScope: PrefixedScope = targetName.namespaceUriOption
            .map { ns =>
              PrefixedScope.from(namespacePrefixMapper.getPrefix(ns) -> ns)
            }
            .getOrElse(PrefixedScope.empty)

          val targetQName: QName = extraScope.getQName(targetName)

          val editedElem = textElem(e.name, e.attributes, targetQName.toString, e.prefixedScope).creationApi
            .usingNonConflictingParentScope(extraScope)
            .underlying

          Seq(editedElem)
        case _ => Seq(e)
      }
    }
  }

  private def convertImport(
      inputImport: standardtaxonomy.dom.Import,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    emptyElem(ENames.XsImportEName, parentScope).creationApi
      .plusAttributes(inputImport.attributes.filterNot(_._1 == ENames.SchemaLocationEName))
      .underlying
  }

  private def makeSchema(docUriOption: Option[URI], schemaRootElem: nodebuilder.Elem): XsSchema = {
    makeSchema(docUriOption, simple.Elem.from(schemaRootElem))
  }

  private def makeSchema(docUriOption: Option[URI], schemaRootElem: simple.Elem): XsSchema = {
    TaxonomyElem(indexed.Elem.ofRoot(docUriOption, schemaRootElem)).asInstanceOf[XsSchema]
  }

  private def makeStandardSchema(docUriOption: Option[URI], schemaRootElem: simple.Elem): standardtaxonomy.dom.XsSchema = {
    standardtaxonomy.dom.TaxonomyElem(indexed.Elem.ofRoot(docUriOption, schemaRootElem)).asInstanceOf[standardtaxonomy.dom.XsSchema]
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
}
