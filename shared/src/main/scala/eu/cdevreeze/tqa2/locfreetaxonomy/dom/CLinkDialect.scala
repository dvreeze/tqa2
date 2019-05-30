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

package eu.cdevreeze.tqa2.locfreetaxonomy.dom

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.common.locfreexlink
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi

/**
 * The "CLink" dialect in a locator-free taxonomy. All elements in this dialect are in the "clink" namespace.
 *
 * The elements in this dialect extend the yaidom `BackingElemApi` API and not the more specific `BackingNodes.Elem` API.
 * It is meant to be mixed in by more concrete XML Schema dialects that do extend the `BackingNodes.Elem` API.
 *
 * @author Chris de Vreeze
 */
object CLinkDialect {

  /**
   * Element in the "CLink" namespace in a locator-free taxonomy.
   * This type or a sub-type is mixed in by taxonomy elements that are indeed in that namespace.
   */
  trait Elem extends BackingElemApi

  // Linkbase

  trait Linkbase extends Elem

  // Standard extended links

  trait StandardLink extends Elem with locfreexlink.ExtendedLink

  trait DefinitionLink extends StandardLink

  trait PresentationLink extends StandardLink

  trait CalculationLink extends StandardLink

  trait LabelLink extends StandardLink

  trait ReferenceLink extends StandardLink

  // Standard arcs

  trait StandardArc extends Elem with locfreexlink.XLinkArc

  trait DefinitionArc extends StandardArc

  trait PresentationArc extends StandardArc

  trait CalculationArc extends StandardArc

  trait LabelArc extends StandardArc

  trait ReferenceArc extends StandardArc

  // Standard resources

  trait StandardResource extends Elem with locfreexlink.XLinkResource

  trait ConceptLabelResource extends StandardResource

  trait ConceptReferenceResource extends StandardResource

  // Other elements

  trait RoleRef extends Elem {

    final def roleUri: String = {
      attrOption(ENames.RoleURIEName)
        .getOrElse(sys.error(s"Expected roleURI attribute. Document: $docUri. Element: $name"))
    }
  }

  trait ArcroleRef extends Elem {

    final def arcroleUri: String = {
      attrOption(ENames.ArcroleURIEName)
        .getOrElse(sys.error(s"Expected arcroleURI attribute. Document: $docUri. Element: $name"))
    }
  }
}
