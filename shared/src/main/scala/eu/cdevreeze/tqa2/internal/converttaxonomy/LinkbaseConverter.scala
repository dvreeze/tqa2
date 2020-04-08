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
import eu.cdevreeze.tqa2.common.xpointer.XPointer
import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.Linkbase
import eu.cdevreeze.tqa2.locfreetaxonomy.dom.TaxonomyElem
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.indexed
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.node.resolved
import eu.cdevreeze.yaidom2.node.simple

/**
 * Converter from standard taxonomy linkbases to locator-free taxonomy linkbases.
 *
 * @author Chris de Vreeze
 */
final class LinkbaseConverter(val namespacePrefixMapper: NamespacePrefixMapper, val xlinkResourceConverter: XLinkResourceConverter) {

  private val xlinkLocatorConverter = new XLinkLocatorConverter(namespacePrefixMapper)

  implicit private val nsPrefixMapper: NamespacePrefixMapper = namespacePrefixMapper
  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(nsPrefixMapper)

  import elemCreator._
  import nodebuilder.NodeBuilderCreator._

  /**
   * Converts a linkbase in the given (2nd parameter) TaxonomyBase to its locator-free counterparts, resulting in
   * a locator-free Linkbase returned by this function.
   *
   * The input TaxonomyBase parameter (2nd parameter) should be closed under DTS discovery rules.
   */
  def convertLinkbase(inputLinkbase: standardtaxonomy.dom.Linkbase, inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase): Linkbase = {

    ???
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
    TaxonomyElem(indexed.Elem.ofRoot(currentLinkbase.docUriOption, simple.Elem.from(resultElem))).asInstanceOf[Linkbase]
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

    val docUri: URI = withoutFragment(locatorHref)
    val fragment: String = Option(locatorHref.getFragment).getOrElse("")

    val docElem: standardtaxonomy.dom.TaxonomyElem = inputTaxonomyBase.rootElemMap
      .getOrElse(docUri, sys.error(s"Could not resolve URI '$docUri'"))

    val elem: standardtaxonomy.dom.TaxonomyElem =
      if (fragment.trim.isEmpty) {
        docElem
      } else {
        XPointer
          .findElem(docElem, XPointer.parseXPointers(fragment))
          .getOrElse(sys.error(s"Could not resolve URI '$locatorHref'"))
      }
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
        toTaxoElem = locatorHrefResolutions.getOrElse(fromUri, sys.error(s"Missing XML element at '$toUri'"))
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
            val res = locatedElem.asInstanceOf[standardtaxonomy.dom.XLinkResource]
            xlinkLocatorConverter.convertLocToTaxonomyElemKey(loc, res, inputTaxonomyBase, parentScope)
        }

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

  private def convertLinkName(inputLinkName: EName): EName = {
    inputLinkName match {
      case ENames.LinkDefinitionLinkEName   => ENames.CLinkDefinitionLinkEName
      case ENames.LinkPresentationLinkEName => ENames.CLinkPresentationLinkEName
      case ENames.LinkCalculationLinkEName  => ENames.CLinkCalculationLinkEName
      case ENames.LinkLabelLinkEName        => ENames.CLinkLabelLinkEName
      case ENames.LinkReferenceLinkEName    => ENames.CLinkReferenceLinkEName
      case ENames.GenLinkEName              => ENames.CGenLinkEName
      case n                                => n
    }
  }

  private def convertArcName(inputArcName: EName): EName = {
    inputArcName match {
      case ENames.LinkDefinitionArcEName   => ENames.CLinkDefinitionArcEName
      case ENames.LinkPresentationArcEName => ENames.CLinkPresentationArcEName
      case ENames.LinkCalculationArcEName  => ENames.CLinkCalculationArcEName
      case ENames.LinkLabelArcEName        => ENames.CLinkLabelArcEName
      case ENames.LinkReferenceArcEName    => ENames.CLinkReferenceArcEName
      case n                               => n
    }
  }

  private def withoutFragment(uri: URI): URI = {
    new URI(uri.getScheme, uri.getSchemeSpecificPart, null) // scalastyle:off null
  }
}
