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
 * Partly abstract API of XLink content, where XLink locators and XLink simple links are not allowed.
 *
 * @author Chris de Vreeze
 */
package object locfreexlink {

  type XLinkElem = XLink.XLinkElem

  type ChildXLink = XLink.ChildXLink

  type XLinkResource = XLink.XLinkResource

  type XLinkArc = XLink.XLinkArc

  type ExtendedLink = XLink.ExtendedLink
}
