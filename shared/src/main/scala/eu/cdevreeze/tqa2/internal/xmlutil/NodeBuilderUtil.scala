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

package eu.cdevreeze.tqa2.internal.xmlutil

import eu.cdevreeze.tqa2.internal.xmlutil.ScopeUtil._
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.node.indexed
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.node.simple
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor

import scala.util.chaining._

/**
 * Helper functions for editing nodebuilder elements. For example, pushing up namespace declarations, prettifying, removing unused
 * namespaces, etc.
 *
 * @author Chris de Vreeze
 */
abstract class NodeBuilderUtil(val namespacePrefixMapper: NamespacePrefixMapper, val documentENameExtractor: DocumentENameExtractor) {

  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(namespacePrefixMapper)

  import nodebuilder.NodeBuilderCreator._

  def prettify(elem: nodebuilder.Elem): nodebuilder.Elem

  final def pushUpPrefixedNamespaceDeclarations(elem: nodebuilder.Elem): nodebuilder.Elem = {
    val parentScope: PrefixedScope = elem.findAllDescendantElemsOrSelf
      .map(_.prefixedScope)
      .distinct
      .reverse
      .reduceLeft(_.usingListMap.append(_))

    elem.creationApi.usingParentScope(parentScope).underlying
  }

  /**
   * Returns an adapted copy where all unused namespaces have been removed from the PrefixedScopes (of the
   * element and its descendants).
   *
   * The root element of the given element must be the root element of "the document".
   *
   * This method is very useful when retrieving and serializing a small fragment of an XML tree in which most
   * namespace declarations in the root element are not needed for the retrieved fragment.
   */
  final def stripUnusedNamespaceDeclarations(elem: nodebuilder.Elem): nodebuilder.Elem = {
    val simpleElem: simple.Elem =
      stripUnusedNamespaceDeclarations(indexed.Elem.ofRoot(None, simple.Elem.from(elem)), documentENameExtractor)
    nodebuilder.Elem.from(simpleElem)
  }

  /**
   * Calls `elem.pipe(stripUnusedNamespaceDeclarations).pipe(pushUpPrefixedNamespaceDeclarations).pipe(prettify)`.
   */
  final def sanitize(elem: nodebuilder.Elem): nodebuilder.Elem = {
    elem.pipe(stripUnusedNamespaceDeclarations).pipe(pushUpPrefixedNamespaceDeclarations) // .pipe(prettify) // TODO
  }

  /**
   * Returns an adapted copy (as simple Elem) where all unused namespaces have been removed from the Scopes (of the
   * element and its descendants). To determine the used namespaces, method `findAllNamespaces` is called on the
   * element.
   *
   * The root element of the given indexed element must be the root element of the document.
   *
   * This method is very useful when retrieving and serializing a small fragment of an XML tree in which most
   * namespace declarations in the root element are not needed for the retrieved fragment.
   */
  private def stripUnusedNamespaceDeclarations(elem: indexed.Elem, documentENameExtractor: DocumentENameExtractor): simple.Elem = {
    val usedNamespaces = findAllNamespaces(elem, documentENameExtractor)

    val resultElem = elem.underlyingElem.transformDescendantElemsOrSelf { e =>
      val adaptedScope: Scope = e.scope.filterNamespaces(usedNamespaces.contains)
      new simple.Elem(e.qname, e.attributesByQName, adaptedScope, e.children)
    }
    resultElem
  }

  /**
   * Returns `findAllENames(elem, documentENameExtractor).flatMap(_.namespaceUriOption)`.
   * That is, finds all namespaces used in the element and its descendants.
   *
   * The root element of the given indexed element must be the root element of the document.
   */
  private def findAllNamespaces(elem: BackingElemApi, documentENameExtractor: DocumentENameExtractor): Set[String] = {
    findAllENames(elem, documentENameExtractor).flatMap(_.namespaceUriOption)
  }

  /**
   * Finds all ENames, in element names, attribute names, but also in element content and attribute values,
   * using the given DocumentENameExtractor. The element and all its descendants are taken into account.
   *
   * The root element of the given indexed element must be the root element of the document.
   */
  private def findAllENames(elem: BackingElemApi, documentENameExtractor: DocumentENameExtractor): Set[EName] = {
    elem.findAllDescendantElemsOrSelf.flatMap(e => findENamesInElementItself(e, documentENameExtractor)).toSet
  }

  /**
   * Finds the ENames, in element name, attribute names, but also in element content and attribute values,
   * using the given DocumentENameExtractor. Only the element itself is taken into consideration, not its
   * descendants.
   *
   * The root element of the given indexed element must be the root element of the document.
   */
  private def findENamesInElementItself(elem: BackingElemApi, documentENameExtractor: DocumentENameExtractor): Set[EName] = {
    val scope = elem.scope

    val enamesInElemText: Set[EName] =
      documentENameExtractor.findElemTextENameExtractor(elem).map(_.extractENames(scope, elem.text)).getOrElse(Set())

    val enamesInAttrValues: Set[EName] =
      elem.attributes.flatMap {
        case (attrEName, attrValue) =>
          documentENameExtractor.findAttributeValueENameExtractor(elem, attrEName).map(_.extractENames(scope, attrValue)).getOrElse(Set())
      }.toSet

    Set(elem.name).union(elem.attributes.keySet).union(enamesInElemText).union(enamesInAttrValues)
  }
}
