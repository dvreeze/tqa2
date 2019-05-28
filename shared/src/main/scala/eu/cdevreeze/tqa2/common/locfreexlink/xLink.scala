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

package eu.cdevreeze.tqa2.common.locfreexlink

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi

/**
 * A locator-free (and simple-link-free) XLink element in a taxonomy, obeying the constraints on XLink imposed by XBRL.
 * For example, an XLink arc or extended link without locator children.
 *
 * The XLink elements are themselves a yaidom `BackingElemApi`. As a consequence, XLink child elements of an extended link know their
 * parent element, and therefore know their ELR (extended link role).
 *
 * XLink (see https://www.w3.org/TR/xlink11/) is a somewhat low level standard on top of XML, but it is
 * very important in an XBRL context. Many taxonomy elements are also XLink elements, especially inside linkbases.
 *
 * It is assumed that the XLink content obeys the (locator-free) XLink XML schemas, or else the query methods below may throw an exception.
 *
 * @author Chris de Vreeze
 */
trait XLinkElem extends BackingElemApi {

  final def xlinkType: String = {
    attrOption(ENames.XLinkTypeEName).getOrElse(sys.error(s"Missing xlink:type attribute. Document: $docUri. Element: $name"))
  }

  final def xlinkAttributes: Map[EName, String] = {
    attributes.filter { case (attrName, _) => Namespaces.XLinkNamespace.contains(attrName.namespaceUriOption.getOrElse("")) }
  }
}

// TODO XLink title and documentation (abstract) elements have not been modeled (yet).

/**
 * XLink child element of an extended link, so an XLink arc or resource.
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
 * XLink resource.
 */
trait XLinkResource extends ChildXLink {

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
 * XLink extended link.
 */
trait ExtendedLink extends XLinkElem {

  /**
   * Returns the extended link role.
   */
  final def role: String = {
    attrOption(ENames.XLinkRoleEName).getOrElse(sys.error(s"Missing xlink:role attribute. Document: $docUri. Element: $name"))
  }

  final def xlinkChildren: Seq[ChildXLink] = {
    findAllChildElems().collect { case e: ChildXLink => e }
  }

  final def xlinkResourceChildren: Seq[XLinkResource] = {
    findAllChildElems().collect { case e: XLinkResource => e }
  }

  final def arcs: Seq[XLinkArc] = {
    findAllChildElems().collect { case e: XLinkArc => e }
  }

  /**
   * Returns the XLink resources grouped by XLink label.
   * This is an expensive method, so when processing an extended link, this method should
   * be called only once per extended link.
   */
  final def labeledXlinkResourceMap: Map[String, Seq[XLinkResource]] = {
    xlinkResourceChildren.groupBy(_.xlinkLabel)
  }
}

/**
 * XLink arc.
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
