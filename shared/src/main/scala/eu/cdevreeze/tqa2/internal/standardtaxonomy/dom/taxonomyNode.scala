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

package eu.cdevreeze.tqa2.internal.standardtaxonomy.dom

import java.net.URI

import scala.collection.immutable.{ArraySeq, SeqMap}

import eu.cdevreeze.tqa2.{ENames, Namespaces}
import eu.cdevreeze.tqa2.common.FragmentKey
import eu.cdevreeze.tqa2.locfreetaxonomy.common.{BaseSetKey, Use}
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.dialect.AbstractDialectBackingElem
import eu.cdevreeze.yaidom2.queryapi.{BackingNodes, ElemStep}

/**
 * Node in a standard XBRL taxonomy.
 *
 * @author Chris de Vreeze
 */
// scalastyle:off number.of.types
// scalastyle:off file.size.limit
sealed trait TaxonomyNode extends BackingNodes.Node

sealed trait CanBeTaxonomyDocumentChild extends TaxonomyNode with BackingNodes.CanBeDocumentChild

final case class TaxonomyTextNode(text: String) extends TaxonomyNode with BackingNodes.Text

final case class TaxonomyCommentNode(text: String) extends CanBeTaxonomyDocumentChild with BackingNodes.Comment

final case class TaxonomyProcessingInstructionNode(target: String, data: String) extends CanBeTaxonomyDocumentChild
  with BackingNodes.ProcessingInstruction

/**
 * Standard XBRL taxonomy dialect element node, offering the `BackingNodes.Elem` element query API and additional
 * type-safe query methods.
 *
 * Note that the underlying element can be of any element implementation that offers the `BackingNodes.Elem` API.
 *
 * Taxonomy elements are instantiated without knowing any context of the containing document, such as substitution groups or
 * sibling extended link child elements. Only an entire taxonomy has enough context to turn global element declarations into
 * concept declarations, for example.
 *
 * Creating taxonomy elements should hardly, if ever, fail. After creation, type-safe query methods can fail if the taxonomy
 * content is not valid against the schema, however.
 */
sealed trait TaxonomyElem extends AbstractDialectBackingElem with CanBeTaxonomyDocumentChild {

  type ThisElem = TaxonomyElem

  type ThisNode = TaxonomyNode

  final def wrapElem(underlyingElem: BackingNodes.Elem): ThisElem = TaxonomyElem(underlyingElem)

  // ClarkNodes.Elem

  final def children: ArraySeq[TaxonomyNode] = {
    underlyingElem.children.flatMap(TaxonomyNode.opt).to(ArraySeq)
  }

  final def select(step: ElemStep[TaxonomyElem]): Seq[TaxonomyElem] = {
    step(this)
  }

  // Overridden methods, to "fix" the method signatures (setting ThisElem to TaxonomyElem)

  final override def filterChildElems(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterChildElems(p)
  }

  final override def findAllChildElems: Seq[ThisElem] = {
    super.findAllChildElems
  }

  final override def findChildElem(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findChildElem(p)
  }

  final override def filterDescendantElems(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterDescendantElems(p)
  }

  final override def findAllDescendantElems: Seq[ThisElem] = {
    super.findAllDescendantElems
  }

  final override def findDescendantElem(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findDescendantElem(p)
  }

  final override def filterDescendantElemsOrSelf(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterDescendantElemsOrSelf(p)
  }

  final override def findAllDescendantElemsOrSelf: Seq[ThisElem] = {
    super.findAllDescendantElemsOrSelf
  }

  final override def findDescendantElemOrSelf(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findDescendantElemOrSelf(p)
  }

  final override def findTopmostElems(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.findTopmostElems(p)
  }

  final override def findTopmostElemsOrSelf(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.findTopmostElemsOrSelf(p)
  }

  final override def findDescendantElemOrSelf(navigationPath: Seq[Int]): Option[ThisElem] = {
    super.findDescendantElemOrSelf(navigationPath)
  }

  final override def getDescendantElemOrSelf(navigationPath: Seq[Int]): ThisElem = {
    super.getDescendantElemOrSelf(navigationPath)
  }

  final override def findParentElem(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findParentElem(p)
  }

  final override def findParentElem: Option[ThisElem] = {
    super.findParentElem
  }

  final override def filterAncestorElems(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterAncestorElems(p)
  }

  final override def findAllAncestorElems: Seq[ThisElem] = {
    super.findAllAncestorElems
  }

  final override def findAncestorElem(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findAncestorElem(p)
  }

  final override def filterAncestorElemsOrSelf(p: ThisElem => Boolean): Seq[ThisElem] = {
    super.filterAncestorElemsOrSelf(p)
  }

  final override def findAllAncestorElemsOrSelf: Seq[ThisElem] = {
    super.findAllAncestorElemsOrSelf
  }

  final override def findAncestorElemOrSelf(p: ThisElem => Boolean): Option[ThisElem] = {
    super.findAncestorElemOrSelf(p)
  }

  final override def findAllPrecedingSiblingElems: Seq[ThisElem] = {
    super.findAllPrecedingSiblingElems
  }

  final override def rootElem: ThisElem = {
    super.rootElem
  }

  // Other methods

  final def fragmentKey: FragmentKey = {
    FragmentKey(underlyingElem.docUri, underlyingElem.ownNavigationPathRelativeToRootElem)
  }

  final def isRootElement: Boolean = this match {
    case _: RootElement => true
    case _ => false
  }

  protected[dom] def requireName(elemName: EName): Unit = {
    require(name == elemName, s"Required name: $elemName. Found name $name instead, in document $docUri")
  }
}

// XLink

sealed trait XLinkElem extends TaxonomyElem {

  final def xlinkType: String = {
    attrOption(ENames.XLinkTypeEName).getOrElse(sys.error(s"Missing xlink:type attribute. Document: $docUri. Element: $name"))
  }

  final def xlinkAttributes: SeqMap[EName, String] = {
    attributes.filter { case (attrName, _) => attrName.namespaceUriOption.contains(Namespaces.XLinkNamespace) }
  }
}

/**
 * Simple or extended link
 */
sealed trait XLinkLink extends XLinkElem

// TODO XLink title and documentation (abstract) elements have not been modeled (yet).

/**
 * XLink child element of an extended link, so an XLink resource, locator or arc
 */
sealed trait ChildXLink extends XLinkElem {

  /**
   * Returns the extended link role of the surrounding extended link element.
   */
  final def elr: String = {
    findParentElem.flatMap(_.attrOption(ENames.XLinkRoleEName)).getOrElse(
      sys.error(s"Missing parent or its xlink:role attribute. Document: $docUri. Element: $name")
    )
  }
}

/**
 * XLink resource or locator
 */
sealed trait LabeledXLink extends ChildXLink {

  /**
   * Returns the XLink label.
   */
  final def xlinkLabel: String = {
    attrOption(ENames.XLinkLabelEName).getOrElse(sys.error(s"Missing xlink:label attribute. Document: $docUri. Element: $name"))
  }

  final def roleOption: Option[String] = {
    attrOption(ENames.XLinkRoleEName)
  }
}

sealed trait XLinkResource extends LabeledXLink

sealed trait XLinkLocator extends LabeledXLink {

  final def rawHref: URI = {
    val uriString = attrOption(ENames.XLinkHrefEName).getOrElse(sys.error(s"Missing xlink:href attribute. Document: $docUri. Element: $name"))
    URI.create(uriString)
  }

  final def resolvedHref: URI = {
    baseUriOption.map(u => u.resolve(rawHref)).getOrElse(rawHref)
  }
}

sealed trait SimpleLink extends XLinkLink {

  final def arcroleOption: Option[String] = {
    attrOption(ENames.XLinkArcroleEName)
  }

  final def roleOption: Option[String] = {
    attrOption(ENames.XLinkRoleEName)
  }

  final def rawHref: URI = {
    val uriString = attrOption(ENames.XLinkHrefEName).getOrElse(sys.error(s"Missing xlink:href attribute. Document: $docUri. Element: $name"))
    URI.create(uriString)
  }

  final def resolvedHref: URI = {
    baseUriOption.map(u => u.resolve(rawHref)).getOrElse(rawHref)
  }
}

sealed trait ExtendedLink extends XLinkLink {

  /**
   * Returns the extended link role.
   */
  final def role: String = {
    attrOption(ENames.XLinkRoleEName).getOrElse(sys.error(s"Missing xlink:role attribute. Document: $docUri. Element: $name"))
  }

  final def xlinkChildren: Seq[ChildXLink] = {
    findAllChildElems.collect { case e: ChildXLink => e }
  }

  final def labeledXLinkChildren: Seq[LabeledXLink] = {
    findAllChildElems.collect { case e: LabeledXLink => e }
  }

  final def arcs: Seq[XLinkArc] = {
    findAllChildElems.collect { case e: XLinkArc => e }
  }

  /**
   * Returns the XLink resources/locators grouped by XLink label.
   * This is an expensive method, so when processing an extended link, this method should
   * be called only once per extended link.
   */
  final def labeledXlinkMap: Map[String, Seq[LabeledXLink]] = {
    labeledXLinkChildren.groupBy(_.xlinkLabel)
  }
}

sealed trait XLinkArc extends ChildXLink {

  /**
   * Returns the arcrole.
   */
  final def arcrole: String = {
    attrOption(ENames.XLinkArcroleEName).getOrElse(sys.error(s"Missing xlink:arcrole attribute. Document: $docUri. Element: $name"))
  }

  /**
   * Returns the XLink "from".
   */
  final def from: String = {
    attrOption(ENames.XLinkFromEName).getOrElse(sys.error(s"Missing xlink:from attribute. Document: $docUri. Element: $name"))
  }

  /**
   * Returns the XLink "to".
   */
  final def to: String = {
    attrOption(ENames.XLinkToEName).getOrElse(sys.error(s"Missing xlink:to attribute. Document: $docUri. Element: $name"))
  }

  /**
   * Returns the Base Set key.
   *
   * If the taxonomy is not known to be schema-valid, it may be impossible to create a Base Set key.
   */
  final def baseSetKey: BaseSetKey = {
    val parentElemOption = underlyingElem.findParentElem
    require(parentElemOption.nonEmpty, s"Missing parent element. Document $docUri. Element name: $name")
    BaseSetKey(name, arcrole, parentElemOption.get.name, parentElemOption.get.attr(ENames.XLinkRoleEName))
  }

  /**
   * Returns the "use" attribute (defaulting to "optional").
   */
  final def use: Use = {
    Use.fromString(attrOption(ENames.UseEName).getOrElse("optional"))
  }

  /**
   * Returns the "priority" integer attribute (defaulting to 0).
   */
  final def priority: Int = {
    attrOption(ENames.PriorityEName).getOrElse("0").toInt
  }

  /**
   * Returns the "order" decimal attribute (defaulting to 1).
   */
  final def order: BigDecimal = {
    BigDecimal(attrOption(ENames.OrderEName).getOrElse("1"))
  }
}

// Root element

/**
 * Taxonomy root element
 */
sealed trait RootElement extends TaxonomyElem with TaxonomyRootElem

object TaxonomyNode {

  def opt(underlyingNode: BackingNodes.Node): Option[TaxonomyNode] = {
    underlyingNode match {
      case e: BackingNodes.Elem => Some(TaxonomyElem(e))
      case t: BackingNodes.Text => Some(TaxonomyTextNode(t.text))
      case c: BackingNodes.Comment => Some(TaxonomyCommentNode(c.text))
      case pi: BackingNodes.ProcessingInstruction => Some(TaxonomyProcessingInstructionNode(pi.target, pi.data))
    }
  }
}

object TaxonomyElem {

  def apply(underlyingElem: BackingNodes.Elem): TaxonomyElem = {
    ???
  }
}
