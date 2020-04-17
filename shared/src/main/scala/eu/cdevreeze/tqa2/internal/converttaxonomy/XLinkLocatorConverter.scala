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
import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.core.PrefixedScopeUtil
import eu.cdevreeze.yaidom2.node.nodebuilder

/**
 * Converter from standard taxonomy XLink content to their locator-free counterparts, as nodebuilder elements.
 * This is a low level class, used internally by LinkbaseConverter etc.
 *
 * @author Chris de Vreeze
 */
final class XLinkLocatorConverter(val namespacePrefixMapper: NamespacePrefixMapper) {

  implicit private val nsPrefixMapper: NamespacePrefixMapper = namespacePrefixMapper
  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(nsPrefixMapper)

  private val prefixedScopeUtil = new PrefixedScopeUtil(nsPrefixMapper)

  import elemCreator._
  import nodebuilder.NodeBuilderCreator._
  import prefixedScopeUtil.extractScope

  def convertLocToTaxonomyElemKey(
      loc: standardtaxonomy.dom.XLinkLocator,
      locatedElem: standardtaxonomy.dom.TaxonomyElem,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    locatedElem match {
      case e: standardtaxonomy.dom.NamedGlobalSchemaComponent =>
        convertLocToSchemaComponentKey(loc, e, inputTaxonomyBase, parentScope)
      case e: standardtaxonomy.dom.RoleType =>
        convertLocToRoleKey(loc, e, parentScope)
      case e: standardtaxonomy.dom.ArcroleType =>
        convertLocToArcroleKey(loc, e, parentScope)
      case e =>
        convertLocToAnyElementKey(loc, e, parentScope)
    }
  }

  def convertLocToSchemaComponentKey(
      loc: standardtaxonomy.dom.XLinkLocator,
      locatedSchemaComponent: standardtaxonomy.dom.NamedGlobalSchemaComponent,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    locatedSchemaComponent match {
      case e: standardtaxonomy.dom.GlobalElementDeclaration
          if inputTaxonomyBase.findConceptDeclaration(locatedSchemaComponent.targetEName).nonEmpty =>
        convertLocToConceptKey(loc, e, parentScope)
      case e: standardtaxonomy.dom.GlobalElementDeclaration =>
        convertLocToElementKey(loc, e, parentScope)
      case e: standardtaxonomy.dom.NamedTypeDefinition =>
        convertLocToTypeKey(loc, e, parentScope)
      case e =>
        convertLocToAnyElementKey(loc, e, parentScope)
    }
  }

  def convertLocToConceptKey(
      loc: standardtaxonomy.dom.XLinkLocator,
      locatedConceptDecl: standardtaxonomy.dom.GlobalElementDeclaration,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    convertLocToSchemaComponentKey(loc, locatedConceptDecl, ENames.CKeyConceptKeyEName, parentScope)
  }

  def convertLocToElementKey(
      loc: standardtaxonomy.dom.XLinkLocator,
      locatedElementDecl: standardtaxonomy.dom.GlobalElementDeclaration,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    convertLocToSchemaComponentKey(loc, locatedElementDecl, ENames.CKeyElementKeyEName, parentScope)
  }

  def convertLocToTypeKey(
      loc: standardtaxonomy.dom.XLinkLocator,
      locatedTypeDef: standardtaxonomy.dom.NamedTypeDefinition,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    convertLocToSchemaComponentKey(loc, locatedTypeDef, ENames.CKeyTypeKeyEName, parentScope)
  }

  def convertLocToRoleKey(
      loc: standardtaxonomy.dom.XLinkLocator,
      locatedRoleType: standardtaxonomy.dom.RoleType,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    val roleUri: String = locatedRoleType.roleUri

    emptyElem(ENames.CKeyRoleKeyEName, parentScope).creationApi
      .plusAttribute(ENames.KeyEName, roleUri)
      .plusAttribute(ENames.XLinkLabelEName, loc.xlinkLabel)
      .plusAttribute(ENames.XLinkTypeEName, "resource")
      .underlying
  }

  def convertLocToArcroleKey(
      loc: standardtaxonomy.dom.XLinkLocator,
      locatedArcroleType: standardtaxonomy.dom.ArcroleType,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    val arcroleUri: String = locatedArcroleType.arcroleUri

    emptyElem(ENames.CKeyArcroleKeyEName, parentScope).creationApi
      .plusAttribute(ENames.KeyEName, arcroleUri)
      .plusAttribute(ENames.XLinkLabelEName, loc.xlinkLabel)
      .plusAttribute(ENames.XLinkTypeEName, "resource")
      .underlying
  }

  def convertLocToAnyElementKey(
      loc: standardtaxonomy.dom.XLinkLocator,
      locatedElement: standardtaxonomy.dom.TaxonomyElem,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    val docUri: URI = locatedElement.docUri
    val id = locatedElement
      .attrOption(ENames.IdEName)
      .getOrElse(sys.error(s"Missing ID for an element ${locatedElement.name} at ${locatedElement.fragmentKey}"))
    val ownUri: URI = new URI(docUri.getScheme, docUri.getSchemeSpecificPart, id)

    emptyElem(ENames.CKeyAnyElemKeyEName, parentScope).creationApi
      .plusAttribute(ENames.KeyEName, ownUri.toString)
      .plusAttribute(ENames.XLinkLabelEName, loc.xlinkLabel)
      .plusAttribute(ENames.XLinkTypeEName, "resource")
      .underlying
  }

  private def convertLocToSchemaComponentKey(
      loc: standardtaxonomy.dom.XLinkLocator,
      locatedSchemaComponent: standardtaxonomy.dom.NamedGlobalSchemaComponent,
      targetElemName: EName,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    val targetEName: EName = locatedSchemaComponent.targetEName
    val effectiveScope: PrefixedScope = extractScope(targetEName, parentScope)

    emptyElem(targetElemName, effectiveScope).creationApi
      .plusAttribute(ENames.KeyEName, effectiveScope.getQName(targetEName).toString)
      .plusAttribute(ENames.XLinkLabelEName, loc.xlinkLabel)
      .plusAttribute(ENames.XLinkTypeEName, "resource")
      .underlying
  }
}
