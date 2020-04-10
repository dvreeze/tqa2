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

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.internal.standardtaxonomy
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.node.nodebuilder

/**
 * Trivial default converter from standard taxonomy XLink resources to their locator-free counterparts, as nodebuilder elements.
 * This is a low level class, used internally by LinkbaseConverter etc. The resource must have no default namespace (anywhere
 * in its element tree), and is basically copied as-is, but maybe using another namespace for the element name.
 *
 * @author Chris de Vreeze
 */
final class DefaultXLinkResourceConverter(val namespacePrefixMapper: NamespacePrefixMapper) extends XLinkResourceConverter {

  implicit private val nsPrefixMapper: NamespacePrefixMapper = namespacePrefixMapper
  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(nsPrefixMapper)

  import nodebuilder.NodeBuilderCreator._
  import elemCreator._

  def convertResource(
      res: standardtaxonomy.dom.XLinkResource,
      inputTaxonomyBase: standardtaxonomy.taxonomy.TaxonomyBase,
      parentScope: PrefixedScope): nodebuilder.Elem = {

    require(
      res.findAllDescendantElemsOrSelf.forall(_.scope.defaultNamespaceOption.isEmpty),
      s"Default namespace not allowed in resource '${res.fragmentKey}'")

    convertName(nodebuilder.Elem.from(res), parentScope).creationApi.usingParentScope(parentScope).underlying
  }

  private def convertName(elem: nodebuilder.Elem, parentScope: PrefixedScope): nodebuilder.Elem = {
    withName(elem, convertName(elem.name), parentScope)
  }

  private def convertName(name: EName): EName = {
    name match {
      case ENames.LinkLabelEName     => ENames.CLinkLabelEName
      case ENames.LinkReferenceEName => ENames.CLinkReferenceEName
      case n                         => n
    }
  }

  // TODO In yaidom2, make this name update easier

  private def withName(elem: nodebuilder.Elem, newName: EName, parentScope: PrefixedScope): nodebuilder.Elem = {
    emptyElem(newName, parentScope).creationApi.plusAttributes(elem.attributes).withChildren(elem.children).underlying
  }
}
