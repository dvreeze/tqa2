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

import eu.cdevreeze.yaidom2.queryapi.BackingElemApi

/**
 * Taxonomy root element in a locator-free taxonomy, so either a schema element or a linkbase element.
 * This type is mixed in by taxonomy elements that are indeed either a schema element or a linkbase element.
 *
 * @author Chris de Vreeze
 */
trait TaxonomyRootElem extends BackingElemApi {

  def isSchema: Boolean

  def isLinkbase: Boolean
}
