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
import eu.cdevreeze.tqa2.internal.xmlutil.NodeBuilderUtil
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Linkbase
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElems
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.indexed
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.node.simple
import eu.cdevreeze.yaidom2.queryapi.ScopedElemApi
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor

/**
 * Converter from standard taxonomy linkbases to locator-free taxonomy linkbases.
 *
 * The locator-free linkbases are not XBRL linkbases (due to the use of another namespace), and they contain neither XLink
 * locators nor XLink simple links. The locators have been replaced by XLink resources representing taxonomy element keys
 * (which are mostly semantic instead of containing some URI to another XML element in the taxonomy). The simple links
 * (like roleRef elements) have been replaced by non-XLink counterparts. Also, locator-free linkbases do not contribute
 * to DTS discovery, because that is limited to entrypoint schemas in the locator-free model.
 *
 * @author Chris de Vreeze
 */
final class LinkbaseConverter(
    val xlinkResourceConverter: XLinkResourceConverter,
    val namespacePrefixMapper: NamespacePrefixMapper,
    val documentENameExtractor: DocumentENameExtractor) {

  private val xlinkLocatorConverter = new XLinkLocatorConverter(namespacePrefixMapper)

  private val nodeBuilderUtil: NodeBuilderUtil = new NodeBuilderUtil(namespacePrefixMapper, documentENameExtractor)

  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(namespacePrefixMapper)

  import NameConversions._
  import elemCreator._
  import nodebuilder.NodeBuilderCreator._

  /**
   * Converts a linkbase in the given (2nd parameter) TaxonomyBase to its locator-free counterparts, resulting in
   * a locator-free Linkbase returned by this function.
   *
   * The returned locator-free linkbase contains XLink resources (namely taxonomy element keys) instead of locators,
   * it contains no simple links (there are non-XLink counterparts for roleRef etc.), and it contains other element names
   * for extended links and for the linkbase itself.
   *
   * The input TaxonomyBase parameter (2nd parameter) should be closed under DTS discovery rules.
   */
  def convertLinkbase(inputLinkbase: standardtaxonomy.dom.Linkbase, inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase): Linkbase = {
    require(ScopedElemApi.containsNoConflictingScopes(inputLinkbase), s"Conflicting scopes not allowed (document ${inputLinkbase.docUri})")

    val parentScope: PrefixedScope = PrefixedScope.empty

    val inputRoleRefsAndArcroleRefs =
      inputLinkbase.filterChildElems(e => e.name == ENames.LinkRoleRefEName || e.name == ENames.LinkArcroleRefEName)

    val startLinkbaseElem: nodebuilder.Elem = emptyElem(ENames.CLinkLinkbaseEName, parentScope).creationApi
      .plusChildren(inputRoleRefsAndArcroleRefs.map { e =>
        e.name match {
          case ENames.LinkRoleRefEName =>
            emptyElem(ENames.CLinkRoleRefEName, parentScope).creationApi
              .plusAttribute(ENames.RoleURIEName, e.attr(ENames.RoleURIEName))
              .underlying
          case _ =>
            assert(e.name == ENames.LinkArcroleRefEName)
            emptyElem(ENames.CLinkArcroleRefEName, parentScope).creationApi
              .plusAttribute(ENames.ArcroleURIEName, e.attr(ENames.ArcroleURIEName))
              .underlying
        }
      })
      .underlying

    val startLinkbase: Linkbase = makeLinkbase(inputLinkbase.docUriOption, startLinkbaseElem)

    val rawLinkbase: Linkbase = inputLinkbase.findAllExtendedLinks.foldLeft(startLinkbase) {
      case (accLinkbase, extLink) =>
        convertAndAddExtendedLink(extLink, inputTaxonomyBase, accLinkbase)
    }

    val sanitizedLinkbaseElem = nodeBuilderUtil.sanitizeAndPrettify(nodebuilder.Elem.from(rawLinkbase))
    makeLinkbase(startLinkbase.docUriOption, sanitizedLinkbaseElem)
  }

  def convertAndAddExtendedLink(
      inputExtendedLink: standardtaxonomy.dom.ExtendedLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    inputExtendedLink match {
      case e: standardtaxonomy.dom.DefinitionLink =>
        convertAndAddInterConceptLink(e, inputTaxonomyBase, currentLinkbase)
      case e: standardtaxonomy.dom.PresentationLink =>
        convertAndAddInterConceptLink(e, inputTaxonomyBase, currentLinkbase)
      case e: standardtaxonomy.dom.CalculationLink =>
        convertAndAddInterConceptLink(e, inputTaxonomyBase, currentLinkbase)
      case e: standardtaxonomy.dom.LabelLink =>
        convertAndAddConceptResourceLink(e, inputTaxonomyBase, currentLinkbase)
      case e: standardtaxonomy.dom.ReferenceLink =>
        convertAndAddConceptResourceLink(e, inputTaxonomyBase, currentLinkbase)
      case e: standardtaxonomy.dom.NonStandardLink =>
        convertAndAddNonStandardLink(e, inputTaxonomyBase, currentLinkbase)
      case _ =>
        sys.error(s"Cannot convert ${inputExtendedLink.name} in '${inputExtendedLink.docUri}'")
    }
  }

  def convertAndAddNonStandardLink(
      inputExtendedLink: standardtaxonomy.dom.NonStandardLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    val parentPrefixedScope: PrefixedScope = PrefixedScope.ignoringDefaultNamespace(currentLinkbase.scope)

    val inputExtLinkBaseUri: URI = inputExtendedLink.baseUri

    val xlinkChildren: Seq[nodebuilder.Elem] = inputExtendedLink.xlinkChildren.map {
      case arc: standardtaxonomy.dom.NonStandardArc =>
        val targetArcName: EName = convertArcName(arc.name)

        emptyElem(targetArcName, arc.attributes, parentPrefixedScope) // All attributes? Without filtering?
          .ensuring(_.attr(ENames.XLinkFromEName) == arc.from)
          .ensuring(_.attr(ENames.XLinkToEName) == arc.to)
      case loc: standardtaxonomy.dom.XLinkLocator =>
        val href: URI = getLocatorHref(loc, inputExtLinkBaseUri)
        val taxoElem: standardtaxonomy.dom.TaxonomyElem = resolveLocatorHref(href, inputTaxonomyBase)
        xlinkLocatorConverter.convertLocToTaxonomyElemKey(loc, taxoElem, inputTaxonomyBase, parentPrefixedScope)
      case res: standardtaxonomy.dom.XLinkResource =>
        xlinkResourceConverter.convertResource(res, inputTaxonomyBase, parentPrefixedScope)
      case e =>
        sys.error(s"Element ${e.name} not allowed in inter-concept extended link (in ${e.docUri})")
    }

    addExtendedLink(inputExtendedLink, xlinkChildren, parentPrefixedScope, currentLinkbase)
  }

  private def convertAndAddInterConceptLink(
      inputExtendedLink: standardtaxonomy.dom.StandardLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    require(
      Set(ENames.LinkDefinitionLinkEName, ENames.LinkPresentationLinkEName, ENames.LinkCalculationLinkEName)
        .contains(inputExtendedLink.name),
      s"Not an inter-concept extended link (in ${inputExtendedLink.docUri})"
    )

    val parentPrefixedScope: PrefixedScope = PrefixedScope.ignoringDefaultNamespace(currentLinkbase.scope)

    val inputExtLinkBaseUri: URI = inputExtendedLink.baseUri

    val xlinkChildren: Seq[nodebuilder.Elem] = inputExtendedLink.xlinkChildren.map {
      case arc: standardtaxonomy.dom.InterConceptArc =>
        convertStandardArc(arc, parentPrefixedScope)
          .ensuring(_.attr(ENames.XLinkFromEName) == arc.from)
          .ensuring(_.attr(ENames.XLinkToEName) == arc.to)
      case loc: standardtaxonomy.dom.StandardLoc =>
        val href: URI = getLocatorHref(loc, inputExtLinkBaseUri)
        val taxoElem: standardtaxonomy.dom.TaxonomyElem = resolveLocatorHref(href, inputTaxonomyBase)
        require(
          taxoElem.isInstanceOf[standardtaxonomy.dom.GlobalElementDeclaration],
          s"Not a global element declaration: ${taxoElem.name} (in ${taxoElem.docUri})")
        val conceptDecl = taxoElem.asInstanceOf[standardtaxonomy.dom.GlobalElementDeclaration]

        xlinkLocatorConverter
          .convertLocToConceptKey(loc, conceptDecl, parentPrefixedScope)
          .ensuring(_.attr(ENames.XLinkLabelEName) == loc.xlinkLabel)
      case res: standardtaxonomy.dom.StandardResource =>
        sys.error(s"XLink resource not allowed in inter-concept extended link (in ${res.docUri})")
      case e =>
        sys.error(s"Element ${e.name} not allowed in inter-concept extended link (in ${e.docUri})")
    }

    addExtendedLink(inputExtendedLink, xlinkChildren, parentPrefixedScope, currentLinkbase)
  }

  private def convertAndAddConceptResourceLink(
      inputExtendedLink: standardtaxonomy.dom.StandardLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    require(
      Set(ENames.LinkLabelLinkEName, ENames.LinkReferenceLinkEName)
        .contains(inputExtendedLink.name),
      s"Not a concept-resource extended link (in ${inputExtendedLink.docUri})"
    )

    val parentPrefixedScope: PrefixedScope = PrefixedScope.ignoringDefaultNamespace(currentLinkbase.scope)

    val inputExtLinkBaseUri: URI = inputExtendedLink.baseUri

    val xlinkChildren: Seq[nodebuilder.Elem] = inputExtendedLink.xlinkChildren.map {
      case arc: standardtaxonomy.dom.ConceptResourceArc =>
        convertStandardArc(arc, parentPrefixedScope)
          .ensuring(_.attr(ENames.XLinkFromEName) == arc.from)
          .ensuring(_.attr(ENames.XLinkToEName) == arc.to)
      case loc: standardtaxonomy.dom.StandardLoc =>
        val href: URI = getLocatorHref(loc, inputExtLinkBaseUri)
        val taxoElem: standardtaxonomy.dom.TaxonomyElem = resolveLocatorHref(href, inputTaxonomyBase)

        taxoElem match {
          case e: standardtaxonomy.dom.GlobalElementDeclaration =>
            xlinkLocatorConverter
              .convertLocToConceptKey(loc, e, parentPrefixedScope)
              .ensuring(_.attr(ENames.XLinkLabelEName) == loc.xlinkLabel)
          case e: standardtaxonomy.dom.StandardResource =>
            xlinkLocatorConverter
              .convertLocToTaxonomyElemKey(loc, e, inputTaxonomyBase, parentPrefixedScope)
              .ensuring(_.attr(ENames.XLinkLabelEName) == loc.xlinkLabel)
          case e =>
            sys.error(s"lement ${e.name} not allowed as locator target in inter-concept extended link (in ${e.docUri})")
        }
      case res: standardtaxonomy.dom.StandardResource =>
        xlinkResourceConverter.convertResource(res, inputTaxonomyBase, parentPrefixedScope)
      case e =>
        sys.error(s"Element ${e.name} not allowed in inter-concept extended link (in ${e.docUri})")
    }

    addExtendedLink(inputExtendedLink, xlinkChildren, parentPrefixedScope, currentLinkbase)
  }

  private def addExtendedLink(
      inputExtendedLink: standardtaxonomy.dom.ExtendedLink,
      xlinkChildren: Seq[nodebuilder.Elem],
      parentPrefixedScope: PrefixedScope,
      currentLinkbase: Linkbase
  ): Linkbase = {
    val targetLinkName: EName = convertLinkName(inputExtendedLink.name)

    val extLink: nodebuilder.Elem = emptyElem(targetLinkName, inputExtendedLink.attributes, parentPrefixedScope).creationApi
      .plusChildren(xlinkChildren)
      .underlying

    val resultElem: nodebuilder.Elem = nodebuilder.Elem.from(currentLinkbase).creationApi.plusChild(extLink).underlying
    TaxonomyElems.of(indexed.Elem.ofRoot(currentLinkbase.docUriOption, simple.Elem.from(resultElem))).asInstanceOf[Linkbase]
  }

  private def getLocatorHref(loc: standardtaxonomy.dom.XLinkLocator, parentBaseUri: URI): URI = {
    val baseUri: URI =
      if (loc.attrOption(ENames.XmlBaseEName).isEmpty) parentBaseUri else parentBaseUri.resolve(loc.attr(ENames.XmlBaseEName))
    baseUri.resolve(loc.rawHref) // Same result as loc.resolvedHref, but more efficient (in terms of prevented base URI computations)
  }

  private def resolveLocatorHref(
      locatorHref: URI,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase): standardtaxonomy.dom.TaxonomyElem = {

    require(locatorHref.isAbsolute, s"Not an absolute locator href: '$locatorHref'")

    val elem: standardtaxonomy.dom.TaxonomyElem = inputTaxonomyBase.getElemByUri(locatorHref)
    elem
  }

  private def convertStandardArc(
      inputArc: standardtaxonomy.dom.StandardArc,
      parentScope: PrefixedScope
  ): nodebuilder.Elem = {
    val targetArcName: EName = convertArcName(inputArc.name)
    val arc: nodebuilder.Elem = emptyElem(targetArcName, inputArc.attributes, parentScope) // All attributes? Without filtering?
    arc
  }

  private def makeLinkbase(docUriOption: Option[URI], linkbaseRootElem: nodebuilder.Elem): Linkbase = {
    // Indexed elements not efficient, but great for debugging
    TaxonomyElems.of(indexed.Elem.ofRoot(docUriOption, simple.Elem.from(linkbaseRootElem))).asInstanceOf[Linkbase]
  }
}
