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
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.indexed
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.node.resolved
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

  // TODO Reorder arcs, keys and non-key resources

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

    val sanitizedLinkbaseElem = nodeBuilderUtil.sanitize(nodebuilder.Elem.from(rawLinkbase))
    makeLinkbase(startLinkbase.docUriOption, sanitizedLinkbaseElem)
  }

  def convertAndAddExtendedLink(
      inputExtendedLink: standardtaxonomy.dom.ExtendedLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    inputExtendedLink match {
      case e: standardtaxonomy.dom.DefinitionLink =>
        convertAndAddDefinitionLink(e, inputTaxonomyBase, currentLinkbase)
      case e: standardtaxonomy.dom.PresentationLink =>
        convertAndAddPresentationLink(e, inputTaxonomyBase, currentLinkbase)
      case e: standardtaxonomy.dom.CalculationLink =>
        convertAndAddCalculationLink(e, inputTaxonomyBase, currentLinkbase)
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

  def convertAndAddDefinitionLink(
      inputExtendedLink: standardtaxonomy.dom.DefinitionLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    convertAndAddInterConceptLink(inputExtendedLink, inputTaxonomyBase, currentLinkbase)
  }

  def convertAndAddPresentationLink(
      inputExtendedLink: standardtaxonomy.dom.PresentationLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    convertAndAddInterConceptLink(inputExtendedLink, inputTaxonomyBase, currentLinkbase)
  }

  def convertAndAddCalculationLink(
      inputExtendedLink: standardtaxonomy.dom.CalculationLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    convertAndAddInterConceptLink(inputExtendedLink, inputTaxonomyBase, currentLinkbase)
  }

  def convertAndAddLabelLink(
      inputExtendedLink: standardtaxonomy.dom.LabelLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    convertAndAddConceptResourceLink(inputExtendedLink, inputTaxonomyBase, currentLinkbase)
  }

  def convertAndAddReferenceLink(
      inputExtendedLink: standardtaxonomy.dom.ReferenceLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    convertAndAddConceptResourceLink(inputExtendedLink, inputTaxonomyBase, currentLinkbase)
  }

  def convertAndAddNonStandardLink(
      inputExtendedLink: standardtaxonomy.dom.NonStandardLink,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      currentLinkbase: Linkbase
  ): Linkbase = {
    val inputExtLinkBaseUri: URI = inputExtendedLink.baseUri

    val locatorHrefs: Set[URI] = inputExtendedLink.labeledXlinkChildren
      .collect { case e: standardtaxonomy.dom.XLinkLocator => e }
      .map(e => getLocatorHref(e, inputExtLinkBaseUri))
      .toSet

    val locatorHrefResolutions: Map[URI, standardtaxonomy.dom.TaxonomyElem] =
      locatorHrefs.toSeq.map(uri => uri -> resolveLocatorHref(uri, inputTaxonomyBase)).toMap

    val labeledXLinkMap: Map[String, Seq[standardtaxonomy.dom.LabeledXLink]] = inputExtendedLink.labeledXlinkMap

    val parentPrefixedScope: PrefixedScope = PrefixedScope.ignoringDefaultNamespace(currentLinkbase.scope)
    val parentBaseUri: URI = currentLinkbase.baseUri

    val xlinkChildren: Seq[nodebuilder.Elem] = inputExtendedLink.arcs
      .collect { case e: standardtaxonomy.dom.NonStandardArc => e }
      .flatMap { inputArc =>
        convertNonStandardArcToArcsAndTaxoKeys(
          inputArc,
          labeledXLinkMap,
          locatorHrefResolutions,
          inputTaxonomyBase,
          parentPrefixedScope,
          parentBaseUri)
      }
      .distinctBy(e => resolved.Elem.from(e))

    val targetLinkName: EName = convertLinkName(inputExtendedLink.name)

    val extLink: nodebuilder.Elem = emptyElem(targetLinkName, inputExtendedLink.attributes, parentPrefixedScope).creationApi
      .plusChildren(xlinkChildren)
      .underlying

    val resultElem: nodebuilder.Elem = nodebuilder.Elem.from(currentLinkbase).creationApi.plusChild(extLink).underlying
    makeLinkbase(currentLinkbase.docUriOption, resultElem)
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

    val inputExtLinkBaseUri: URI = inputExtendedLink.baseUri

    val locatorHrefs: Set[URI] = inputExtendedLink.labeledXlinkChildren
      .collect { case e: standardtaxonomy.dom.XLinkLocator => e }
      .map(e => getLocatorHref(e, inputExtLinkBaseUri))
      .toSet

    val locatorHrefResolutions: Map[URI, standardtaxonomy.dom.TaxonomyElem] =
      locatorHrefs.toSeq.map(uri => uri -> resolveLocatorHref(uri, inputTaxonomyBase)).toMap

    val labeledXLinkMap: Map[String, Seq[standardtaxonomy.dom.LabeledXLink]] = inputExtendedLink.labeledXlinkMap

    val parentPrefixedScope: PrefixedScope = PrefixedScope.ignoringDefaultNamespace(currentLinkbase.scope)
    val parentBaseUri: URI = currentLinkbase.baseUri

    val xlinkChildren: Seq[nodebuilder.Elem] = inputExtendedLink.arcs
      .collect { case e: standardtaxonomy.dom.InterConceptArc => e }
      .flatMap { inputArc =>
        convertInterConceptArcToArcsAndTaxoKeys(inputArc, labeledXLinkMap, locatorHrefResolutions, parentPrefixedScope, parentBaseUri)
      }
      .distinctBy(e => resolved.Elem.from(e))

    val targetLinkName: EName = convertLinkName(inputExtendedLink.name)

    val extLink: nodebuilder.Elem = emptyElem(targetLinkName, inputExtendedLink.attributes, parentPrefixedScope).creationApi
      .plusChildren(xlinkChildren)
      .underlying

    val resultElem: nodebuilder.Elem = nodebuilder.Elem.from(currentLinkbase).creationApi.plusChild(extLink).underlying
    TaxonomyElem(indexed.Elem.ofRoot(currentLinkbase.docUriOption, simple.Elem.from(resultElem))).asInstanceOf[Linkbase]
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

    val inputExtLinkBaseUri: URI = inputExtendedLink.baseUri

    val locatorHrefs: Set[URI] = inputExtendedLink.labeledXlinkChildren
      .collect { case e: standardtaxonomy.dom.XLinkLocator => e }
      .map(e => getLocatorHref(e, inputExtLinkBaseUri))
      .toSet

    val locatorHrefResolutions: Map[URI, standardtaxonomy.dom.TaxonomyElem] =
      locatorHrefs.toSeq.map(uri => uri -> resolveLocatorHref(uri, inputTaxonomyBase)).toMap

    val labeledXLinkMap: Map[String, Seq[standardtaxonomy.dom.LabeledXLink]] = inputExtendedLink.labeledXlinkMap

    val parentPrefixedScope: PrefixedScope = PrefixedScope.ignoringDefaultNamespace(currentLinkbase.scope)
    val parentBaseUri: URI = currentLinkbase.baseUri

    val xlinkChildren: Seq[nodebuilder.Elem] = inputExtendedLink.arcs
      .collect { case e: standardtaxonomy.dom.ConceptResourceArc => e }
      .flatMap { inputArc =>
        convertConceptResourceArcToArcsAndTaxoKeys(
          inputArc,
          labeledXLinkMap,
          locatorHrefResolutions,
          inputTaxonomyBase,
          parentPrefixedScope,
          parentBaseUri)
      }
      .distinctBy(e => resolved.Elem.from(e))

    val targetLinkName: EName = convertLinkName(inputExtendedLink.name)

    val extLink: nodebuilder.Elem = emptyElem(targetLinkName, inputExtendedLink.attributes, parentPrefixedScope).creationApi
      .plusChildren(xlinkChildren)
      .underlying

    val resultElem: nodebuilder.Elem = nodebuilder.Elem.from(currentLinkbase).creationApi.plusChild(extLink).underlying
    TaxonomyElem(indexed.Elem.ofRoot(currentLinkbase.docUriOption, simple.Elem.from(resultElem))).asInstanceOf[Linkbase]
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

  private def convertInterConceptArcToArcsAndTaxoKeys(
      inputArc: standardtaxonomy.dom.InterConceptArc,
      labeledXLinkMap: Map[String, Seq[standardtaxonomy.dom.LabeledXLink]],
      locatorHrefResolutions: Map[URI, standardtaxonomy.dom.TaxonomyElem],
      parentScope: PrefixedScope,
      parentBaseUri: URI
  ): Seq[nodebuilder.Elem] = {
    val targetArcName: EName = convertArcName(inputArc.name)

    val arcGroups: Seq[Seq[nodebuilder.Elem]] =
      for {
        fromLocOrRes <- labeledXLinkMap.getOrElse(inputArc.from, sys.error(s"Missing XLink 'from' (${inputArc.docUri})"))
        fromLoc = fromLocOrRes.asInstanceOf[standardtaxonomy.dom.XLinkLocator]
        fromUri = getLocatorHref(fromLoc, parentBaseUri)
        fromTaxoElem = locatorHrefResolutions.getOrElse(fromUri, sys.error(s"Missing XML element at '$fromUri'"))
        toLocOrRes <- labeledXLinkMap.getOrElse(inputArc.to, sys.error(s"Missing XLink 'to' (${inputArc.docUri})"))
        toLoc = toLocOrRes.asInstanceOf[standardtaxonomy.dom.XLinkLocator]
        toUri = getLocatorHref(toLoc, parentBaseUri)
        toTaxoElem = locatorHrefResolutions.getOrElse(toUri, sys.error(s"Missing XML element at '$toUri'"))
      } yield {
        require(fromTaxoElem.isInstanceOf[standardtaxonomy.dom.GlobalElementDeclaration], s"Not a concept declaration: '$fromUri'")
        require(toTaxoElem.isInstanceOf[standardtaxonomy.dom.GlobalElementDeclaration], s"Not a concept declaration: '$toUri'")

        val fromConceptDecl: standardtaxonomy.dom.GlobalElementDeclaration =
          fromTaxoElem.asInstanceOf[standardtaxonomy.dom.GlobalElementDeclaration]
        val toConceptDecl: standardtaxonomy.dom.GlobalElementDeclaration =
          toTaxoElem.asInstanceOf[standardtaxonomy.dom.GlobalElementDeclaration]

        val arc: nodebuilder.Elem = emptyElem(targetArcName, inputArc.attributes, parentScope) // All attributes? Without filtering?
        val from: nodebuilder.Elem = xlinkLocatorConverter.convertLocToConceptKey(fromLoc, fromConceptDecl, parentScope)
        val to: nodebuilder.Elem = xlinkLocatorConverter.convertLocToConceptKey(toLoc, toConceptDecl, parentScope)

        Seq(arc, from, to)
      }

    arcGroups.flatten.distinctBy(e => resolved.Elem.from(e))
  }

  private def convertConceptResourceArcToArcsAndTaxoKeys(
      inputArc: standardtaxonomy.dom.ConceptResourceArc,
      labeledXLinkMap: Map[String, Seq[standardtaxonomy.dom.LabeledXLink]],
      locatorHrefResolutions: Map[URI, standardtaxonomy.dom.TaxonomyElem],
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      parentScope: PrefixedScope,
      parentBaseUri: URI
  ): Seq[nodebuilder.Elem] = {
    val targetArcName: EName = convertArcName(inputArc.name)

    val arcGroups: Seq[Seq[nodebuilder.Elem]] =
      for {
        fromLocOrRes <- labeledXLinkMap.getOrElse(inputArc.from, sys.error(s"Missing XLink 'from' (${inputArc.docUri})"))
        fromLoc = fromLocOrRes.asInstanceOf[standardtaxonomy.dom.XLinkLocator]
        fromUri = getLocatorHref(fromLoc, parentBaseUri)
        fromTaxoElem = locatorHrefResolutions.getOrElse(fromUri, sys.error(s"Missing XML element at '$fromUri'"))
        toLocOrRes <- labeledXLinkMap.getOrElse(inputArc.to, sys.error(s"Missing XLink 'to' (${inputArc.docUri})"))
      } yield {
        require(fromTaxoElem.isInstanceOf[standardtaxonomy.dom.GlobalElementDeclaration], s"Not a concept declaration: '$fromUri'")

        val fromConceptDecl: standardtaxonomy.dom.GlobalElementDeclaration =
          fromTaxoElem.asInstanceOf[standardtaxonomy.dom.GlobalElementDeclaration]

        val arc: nodebuilder.Elem = emptyElem(targetArcName, inputArc.attributes, parentScope) // All attributes? Without filtering?
        val from: nodebuilder.Elem = xlinkLocatorConverter.convertLocToConceptKey(fromLoc, fromConceptDecl, parentScope)

        val to: nodebuilder.Elem = toLocOrRes match {
          case res: standardtaxonomy.dom.XLinkResource =>
            xlinkResourceConverter.convertResource(res, inputTaxonomyBase, parentScope)
          case loc: standardtaxonomy.dom.XLinkLocator =>
            val locUri = getLocatorHref(loc, parentBaseUri)
            val locatedElem = locatorHrefResolutions.getOrElse(locUri, sys.error(s"Missing XML element at '$locUri'"))
            val res = locatedElem.asInstanceOf[standardtaxonomy.dom.XLinkResource]
            xlinkLocatorConverter.convertLocToTaxonomyElemKey(loc, res, inputTaxonomyBase, parentScope)
        }

        Seq(arc, from, to)
      }

    arcGroups.flatten.distinctBy(e => resolved.Elem.from(e))
  }

  private def convertNonStandardArcToArcsAndTaxoKeys(
      inputArc: standardtaxonomy.dom.NonStandardArc,
      labeledXLinkMap: Map[String, Seq[standardtaxonomy.dom.LabeledXLink]],
      locatorHrefResolutions: Map[URI, standardtaxonomy.dom.TaxonomyElem],
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      parentScope: PrefixedScope,
      parentBaseUri: URI
  ): Seq[nodebuilder.Elem] = {
    val targetArcName: EName = convertArcName(inputArc.name)

    val arcGroups: Seq[Seq[nodebuilder.Elem]] =
      for {
        fromLocOrRes <- labeledXLinkMap.getOrElse(inputArc.from, sys.error(s"Missing XLink 'from' (${inputArc.docUri})"))
        toLocOrRes <- labeledXLinkMap.getOrElse(inputArc.to, sys.error(s"Missing XLink 'to' (${inputArc.docUri})"))
      } yield {
        val arc: nodebuilder.Elem = emptyElem(targetArcName, inputArc.attributes, parentScope) // All attributes? Without filtering?

        val from: nodebuilder.Elem = fromLocOrRes match {
          case res: standardtaxonomy.dom.XLinkResource =>
            xlinkResourceConverter.convertResource(res, inputTaxonomyBase, parentScope)
          case loc: standardtaxonomy.dom.XLinkLocator =>
            val locUri = getLocatorHref(loc, parentBaseUri)
            val locatedElem = locatorHrefResolutions.getOrElse(locUri, sys.error(s"Missing XML element at '$locUri'"))
            xlinkLocatorConverter.convertLocToTaxonomyElemKey(loc, locatedElem, inputTaxonomyBase, parentScope)
        }

        val to: nodebuilder.Elem = toLocOrRes match {
          case res: standardtaxonomy.dom.XLinkResource =>
            xlinkResourceConverter.convertResource(res, inputTaxonomyBase, parentScope)
          case loc: standardtaxonomy.dom.XLinkLocator =>
            val locUri = getLocatorHref(loc, parentBaseUri)
            val locatedElem = locatorHrefResolutions.getOrElse(locUri, sys.error(s"Missing XML element at '$locUri'"))
            xlinkLocatorConverter.convertLocToTaxonomyElemKey(loc, locatedElem, inputTaxonomyBase, parentScope)
        }

        Seq(arc, from, to)
      }

    arcGroups.flatten.distinctBy(e => resolved.Elem.from(e))
  }

  private def makeLinkbase(docUriOption: Option[URI], linkbaseRootElem: nodebuilder.Elem): Linkbase = {
    TaxonomyElem(indexed.Elem.ofRoot(docUriOption, simple.Elem.from(linkbaseRootElem))).asInstanceOf[Linkbase]
  }
}
