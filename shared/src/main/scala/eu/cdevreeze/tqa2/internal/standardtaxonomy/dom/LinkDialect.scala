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

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.common.xlink
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi

/**
 * The "Link" dialect in a standard XBRL taxonomy. All elements in this dialect are in the "link" namespace.
 *
 * The elements in this dialect extend the yaidom `BackingElemApi` API and not the more specific `BackingNodes.Elem` API.
 * It is meant to be mixed in by more concrete XML Schema dialects that do extend the `BackingNodes.Elem` API.
 *
 * @author Chris de Vreeze
 */
object LinkDialect {

  /**
   * Element in the "Link" namespace in a standard XBRL taxonomy.
   * This type or a sub-type is mixed in by taxonomy elements that are indeed in that namespace.
   */
  trait Elem extends BackingElemApi

  // Linkbase

  trait Linkbase extends Elem

  // Standard extended links

  trait StandardLink extends Elem with xlink.ExtendedLink

  trait DefinitionLink extends StandardLink

  trait PresentationLink extends StandardLink

  trait CalculationLink extends StandardLink

  trait LabelLink extends StandardLink

  trait ReferenceLink extends StandardLink

  // Standard arcs

  trait StandardArc extends Elem with xlink.XLinkArc

  trait InterConceptArc extends StandardArc

  trait ConceptResourceArc extends StandardArc

  trait DefinitionArc extends InterConceptArc

  trait PresentationArc extends InterConceptArc

  trait CalculationArc extends InterConceptArc

  trait LabelArc extends ConceptResourceArc

  trait ReferenceArc extends ConceptResourceArc

  // Standard resources

  trait StandardResource extends Elem with xlink.XLinkResource

  trait ConceptLabelResource extends StandardResource

  trait ConceptReferenceResource extends StandardResource

  // Standard locators

  trait StandardLoc extends Elem with xlink.XLinkLocator

  // Standard simple links

  trait StandardSimpleLink extends Elem with xlink.SimpleLink

  trait RoleRef extends StandardSimpleLink {

    final def roleUri: String = {
      attrOption(ENames.RoleURIEName)
        .getOrElse(sys.error(s"Expected roleURI attribute. Document: $docUri. Element: $name"))
    }
  }

  trait ArcroleRef extends StandardSimpleLink {

    final def arcroleUri: String = {
      attrOption(ENames.ArcroleURIEName)
        .getOrElse(sys.error(s"Expected arcroleURI attribute. Document: $docUri. Element: $name"))
    }
  }

  // Standard simple links in entrypoint schemas

  trait LinkbaseRef extends StandardSimpleLink {

    final def href: URI = {
      attrOption(ENames.XLinkHrefEName).map(URI.create)
        .getOrElse(sys.error(s"Expected xlink:href attribute. Document: $docUri. Element: $name"))
    }
  }

  trait SchemaRef extends StandardSimpleLink {

    final def href: URI = {
      attrOption(ENames.XLinkHrefEName).map(URI.create)
        .getOrElse(sys.error(s"Expected xlink:href attribute. Document: $docUri. Element: $name"))
    }
  }

  // Other elements

  trait RoleType extends Elem {

    /**
     * Returns the roleURI attribute.
     */
    def roleUri: String = {
      attr(ENames.RoleURIEName)
    }
  }

  trait ArcroleType extends Elem {

    /**
     * Returns the arcroleURI attribute.
     */
    def arcroleUri: String = {
      attr(ENames.ArcroleURIEName)
    }
  }

  trait Definition extends Elem

  trait UsedOn extends Elem
}
