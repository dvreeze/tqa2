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

package eu.cdevreeze.tqa2.common

/**
 * Partly abstract API of XLink content, as used in XBRL. Different type-safe DOM abstractions share
 * this API for XLink content.
 *
 * See the schema xl-2003-12-31.xsd (and the imported xlink-2003-12-31.xsd). For the relevant part of
 * the Core XBRL Specification, see http://www.xbrl.org/Specification/XBRL-2.1/REC-2003-12-31/XBRL-2.1-REC-2003-12-31+corrected-errata-2013-02-20.html#_3.5.
 *
 * @author Chris de Vreeze
 */
package object xlink {

  type XLinkElem = XLink.XLinkElem

  type XLinkLink = XLink.XLinkLink

  type ChildXLink = XLink.ChildXLink

  type LabeledXLink = XLink.LabeledXLink

  type XLinkResource = XLink.XLinkResource

  type XLinkLocator = XLink.XLinkLocator

  type XLinkArc = XLink.XLinkArc

  type SimpleLink = XLink.SimpleLink

  type ExtendedLink = XLink.ExtendedLink
}
