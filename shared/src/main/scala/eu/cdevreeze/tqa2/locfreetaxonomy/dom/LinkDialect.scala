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
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi

/**
 * The "legacy" linkbase dialect (in a locator-free taxonomy). All elements in this dialect are in the "link" namespace.
 *
 * The elements in this dialect extend the yaidom `BackingElemApi` API and not the more specific `BackingNodes.Elem` API.
 * It is meant to be mixed in by more concrete XML Schema dialects that do extend the `BackingNodes.Elem` API.
 *
 * @author Chris de Vreeze
 */
object LinkDialect {

  /**
   * Element in the "Link" namespace in a locator-free taxonomy, like roleTypes and arcroleTypes.
   * This type or a sub-type is mixed in by taxonomy elements that are indeed in that namespace.
   */
  trait Elem extends BackingElemApi

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
