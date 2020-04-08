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

package eu.cdevreeze.tqa2.internal.converttaxonomy

import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.nodebuilder

/**
 * Converter from standard taxonomy XLink resources to their locator-free counterparts, as nodebuilder elements.
 * This is a low level class, used internally by LinkbaseConverter etc.
 *
 * @author Chris de Vreeze
 */
trait XLinkResourceConverter {

  def convertResource(
      res: standardtaxonomy.dom.XLinkResource,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      parentScope: PrefixedScope): nodebuilder.Elem
}
