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

package eu.cdevreeze.tqa2.common.xlink

import java.net.URI

import scala.collection.immutable.SeqMap
import scala.util.Try

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi

/**
 * XLink dialect, free from locators and simple links.
 *
 * The elements in this dialect extend the yaidom `BackingElemApi` API and not the more specific `BackingNodes.Elem` API.
 * It is meant to be mixed in by more concrete locator-free XLink-based dialects that do extend the `BackingNodes.Elem` API.
 *
 * @author Chris de Vreeze
 */
object XLink {

  /**
   * An XLink element in a taxonomy, obeying the constraints on XLink imposed by XBRL. For example, an XLink arc or extended link.
   *
   * The XLink elements are themselves a yaidom `BackingElemApi`. As a consequence, XLink child elements of an extended link know their
   * parent element, and therefore know their ELR (extended link role).
   *
   * XLink (see https://www.w3.org/TR/xlink11/) is a somewhat low level standard on top of XML, but it is
   * very important in an XBRL context. Many taxonomy elements are also XLink elements, especially inside linkbases.
   *
   * It is assumed that the XLink content obeys the XLink XML schemas, or else the query methods below may throw an exception.
   */
  trait XLinkElem extends BackingElemApi {

    type ChildXLinkType <: ChildXLink

    type LabeledXLinkType <: LabeledXLink

    type XLinkResourceType <: XLinkResource

    type XLinkLocatorType <: XLinkLocator

    type XLinkArcType <: XLinkArc

    final def xlinkType: String = {
      attrOption(ENames.XLinkTypeEName).getOrElse(sys.error(s"Missing xlink:type attribute. Document: $docUri. Element: $name"))
    }

    final def xlinkAttributes: SeqMap[EName, String] = {
      attributes.filter { case (attrName, _) => attrName.namespaceUriOption.contains(Namespaces.XLinkNamespace) }
    }
  }

  // TODO XLink title and documentation (abstract) elements have not been modeled (yet).

  /**
   * Simple or extended XLink link.
   */
  trait XLinkLink extends XLinkElem

  /**
   * XLink child element of an extended link, so an XLink arc, locator or resource.
   */
  trait ChildXLink extends XLinkElem {

    /**
     * Returns the extended link role of the surrounding extended link element.
     */
    final def elr: String = {
      findParentElem().flatMap(_.attrOption(ENames.XLinkRoleEName)).getOrElse(
        sys.error(s"Missing parent or its xlink:role attribute. Document: $docUri. Element: $name")
      )
    }
  }

  /**
   * XLink locator or resource.
   */
  trait LabeledXLink extends ChildXLink {

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

  /**
   * XLink extended link. For example (child elements have been left out):
   *
   * {{{
   * <link:presentationLink
   *   xlink:type="extended" xlink:role="http://mycompany.com/myPresentationElr">
   *
   * </link:presentationLink>
   * }}}
   *
   * Or, for example (again leaving out child elements):
   *
   * {{{
   * <link:labelLink
   *   xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
   *
   * </link:labelLink>
   * }}}
   */
  trait ExtendedLink extends XLinkLink {

    /**
     * Returns the extended link role.
     */
    final def role: String = {
      attrOption(ENames.XLinkRoleEName).getOrElse(sys.error(s"Missing xlink:role attribute. Document: $docUri. Element: $name"))
    }

    def xlinkChildren: Seq[ChildXLinkType]

    def labeledXlinkChildren: Seq[LabeledXLinkType]

    def arcs: Seq[XLinkArcType]

    /**
     * Returns the XLink locators and resources grouped by XLink label.
     * This is an expensive method, so when processing an extended link, this method should
     * be called only once per extended link.
     */
    def labeledXlinkMap: Map[String, Seq[LabeledXLinkType]]
  }

  /**
   * XLink arc. For example, showing an XLink arc in a presentation link:
   *
   * {{{
   * <link:presentationArc xlink:type="arc"
   *   xlink:arcrole="http://www.xbrl.org/2003/arcrole/parent-child"
   *   xlink:from="parentConcept" xlink:to="childConcept" />
   * }}}
   *
   * The xlink:from and xlink:to attributes point to XLink locators or resources
   * in the same extended link with the corresponding xlink:label attributes.
   */
  trait XLinkArc extends ChildXLink {

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
  }

  /**
   * XLink resource. For example, showing an XLink resource in a label link:
   *
   * {{{
   * <link:label xlink:type="resource"
   *   xlink:label="regionAxis_lbl" xml:lang="en"
   *   xlink:role="http://www.xbrl.org/2003/role/label">Region [Axis]</link:label>
   * }}}
   */
  trait XLinkResource extends LabeledXLink

  /**
   * XLink locator. For example:
   *
   * {{{
   * <link:loc xlink:type="locator"
   *   xlink:label="entityAxis"
   *   xlink:href="Axes.xsd#entityAxis" />
   * }}}
   */
  trait XLinkLocator extends LabeledXLink {

    /**
     * Returns the XLink href as URI.
     */
    final def rawHref: URI = {
      attrOption(ENames.XLinkHrefEName).flatMap(u => Try(URI.create(u)).toOption).getOrElse(
        sys.error(s"Missing or incorrect xlink:href attribute. Document: $docUri, Element: $name")
      )
    }

    /**
     * Returns the XLink href as URI, resolved using XML Base.
     */
    final def resolvedHref: URI = {
      baseUri.resolve(rawHref)
    }
  }

  /**
   * XLink simple link. For example, showing a roleRef:
   *
   * {{{
   * <link:roleRef xlink:type="simple"
   *   xlink:href="Concepts.xsd#SalesAnalysis"
   *   roleURI="http://mycompany.com/2017/SalesAnalysis" />
   * }}}
   */
  trait SimpleLink extends XLinkLink {

    final def arcroleOption: Option[String] = {
      attrOption(ENames.XLinkArcroleEName)
    }

    final def roleOption: Option[String] = {
      attrOption(ENames.XLinkRoleEName)
    }

    /**
     * Returns the XLink href as URI.
     */
    final def rawHref: URI = {
      attrOption(ENames.XLinkHrefEName).flatMap(u => Try(URI.create(u)).toOption).getOrElse(
        sys.error(s"Missing or incorrect xlink:href attribute. Document: $docUri, Element: $name")
      )
    }

    /**
     * Returns the XLink href as URI, resolved using XML Base.
     */
    final def resolvedHref: URI = {
      baseUri.resolve(rawHref)
    }
  }

}
