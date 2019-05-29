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
  trait Elem extends BackingElemApi {
    // TODO
  }

  // Linkbase

  trait Linkbase extends Elem {
    // TODO
  }

  // Standard extended links

  trait StandardLink extends Elem with locfreexlink.ExtendedLink {
    // TODO
  }

  trait DefinitionLink extends StandardLink {
    // TODO
  }

  trait PresentationLink extends StandardLink {
    // TODO
  }

  trait CalculationLink extends StandardLink {
    // TODO
  }

  trait LabelLink extends StandardLink {
    // TODO
  }

  trait ReferenceLink extends StandardLink {
    // TODO
  }

  // Standard arcs

  trait StandardArc extends Elem with locfreexlink.XLinkArc {
    // TODO
  }

  trait DefinitionArc extends StandardArc {
    // TODO
  }

  trait PresentationArc extends StandardArc {
    // TODO
  }

  trait CalculationArc extends StandardArc {
    // TODO
  }

  trait LabelArc extends StandardArc {
    // TODO
  }

  trait ReferenceArc extends StandardArc {
    // TODO
  }

  // Standard resources

  trait StandardResource extends Elem with locfreexlink.XLinkResource {
    // TODO
  }

  trait ConceptLabelResource extends StandardResource {
    // TODO
  }

  trait ConceptReferenceResource extends StandardResource {
    // TODO
  }
}
