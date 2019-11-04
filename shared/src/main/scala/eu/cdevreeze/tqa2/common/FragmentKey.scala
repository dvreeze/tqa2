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

import java.net.URI

/**
 * Unique fragment key of an XML element, consisting of an absolute document URI along with the element's navigation path
 * from the root element, according to function `BackingElemApi.ownNavigationPathRelativeToRootElem`.
 *
 * The own navigation path relative to the root element is defined as follows. For example, if it is Seq(3, 5, 0), this means that
 * this element can be found from the root element as follows: from the root, take the child element with zero-based
 * element index 3, from there take its child element with zero-based element index 5, and finally from there take its
 * child element with zero-based element index 0.
 *
 * @author Chris de Vreeze
 */
final case class FragmentKey(docUri: URI, ownNavigationPathRelativeToRootElem: Seq[Int]) {
  require(docUri.isAbsolute && Option(docUri.getFragment).isEmpty, s"Not an absolute document URI: $docUri")
}
