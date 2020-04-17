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

import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.NamespacePrefixMapper
import eu.cdevreeze.yaidom2.core.PrefixedScope
import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.node.indexed
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.node.simple
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor

import scala.collection.mutable
import scala.util.chaining._

/**
 * Helper functions for editing nodebuilder elements. For example, pushing up namespace declarations, prettifying, removing unused
 * namespaces, etc.
 *
 * @author Chris de Vreeze
 */
final class NodeBuilderUtil(val namespacePrefixMapper: NamespacePrefixMapper, val documentENameExtractor: DocumentENameExtractor) {

  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(namespacePrefixMapper)

  import nodebuilder.NodeBuilderCreator._

  def prettify(elem: nodebuilder.Elem, indent: Int = 2, useTab: Boolean = false, newLine: String = "\n"): nodebuilder.Elem = {
    val simpleElem: simple.Elem = prettify(simple.Elem.from(elem), indent, useTab, newLine)
    nodebuilder.Elem.from(simpleElem)
  }

  def pushUpPrefixedNamespaceDeclarations(elem: nodebuilder.Elem): nodebuilder.Elem = {
    val parentScope: PrefixedScope = elem.findAllDescendantElemsOrSelf
      .map(_.prefixedScope)
      .distinct
      .reverse
      .reduceLeft(_.append(_))

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
  def stripUnusedNamespaceDeclarations(elem: nodebuilder.Elem): nodebuilder.Elem = {
    val simpleElem: simple.Elem =
      stripUnusedNamespaceDeclarations(indexed.Elem.ofRoot(None, simple.Elem.from(elem)), documentENameExtractor)
    nodebuilder.Elem.from(simpleElem)
  }

  /**
   * Calls stripUnusedNamespaceDeclarations, then pushUpPrefixedNamespaceDeclarations, and then prettify.
   */
  def sanitize(elem: nodebuilder.Elem, indent: Int = 2, useTab: Boolean = false, newLine: String = "\n"): nodebuilder.Elem = {
    elem
      .pipe(stripUnusedNamespaceDeclarations)
      .pipe(pushUpPrefixedNamespaceDeclarations)
      .pipe(e => prettify(e, indent, useTab, newLine))
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

  /**
   * Returns a copy where inter-element whitespace has been removed, throughout the node tree.
   *
   * That is, for each descendant-or-self element determines if it has at least one child element and no non-whitespace
   * text child nodes, and if so, removes all (whitespace) text children.
   *
   * This method is useful if it is known that whitespace around element nodes is used for formatting purposes, and (in
   * the absence of an XML Schema or DTD) can therefore be treated as "ignorable whitespace". In the case of "mixed content"
   * (if text around element nodes is not all whitespace), this method will not remove any text children of the parent element.
   *
   * XML space attributes (xml:space) are not respected by this method. If such whitespace preservation functionality is needed,
   * it can be written as a transformation where for specific elements this method is not called.
   */
  private def removeAllInterElementWhitespace(elem: simple.Elem): simple.Elem = {
    def isWhitespaceText(n: simple.Node): Boolean = n match {
      case t: simple.Text if t.text.trim.isEmpty => true
      case _                                     => false
    }

    def isNonTextNode(n: simple.Node): Boolean = n match {
      case _: simple.Text => false
      case _              => true
    }

    val doStripWhitespace = elem.findChildElem(_ => true).nonEmpty && elem.children.forall(n => isWhitespaceText(n) || isNonTextNode(n))

    // Recursive, but not tail-recursive

    val newChildren = {
      val remainder = if (doStripWhitespace) elem.children.filter(n => isNonTextNode(n)) else elem.children

      remainder.map {
        case e: simple.Elem =>
          // Recursive call
          removeAllInterElementWhitespace(e)
        case n =>
          n
      }
    }

    elem.withChildren(newChildren)
  }

  /**
   * "Prettifies" this Elem. That is, first calls method `removeAllInterElementWhitespace`, and then transforms the result
   * by inserting text nodes with newlines and whitespace for indentation.
   */
  private def prettify(elm: simple.Elem, indent: Int, useTab: Boolean, newLine: String): simple.Elem = {
    require(indent >= 0, "The indent can not be negative")
    require(newLine.nonEmpty && newLine.length <= 2 && newLine.forall(c => c == '\n' || c == '\r'), "The newline must be a valid newline")

    val tabOrSpace = if (useTab) "\t" else " "

    def indentToIndentString(totalIndent: Int): String = {
      // String concatenation. If this function is called as little as possible, performance will not suffer too much.
      newLine + (tabOrSpace * totalIndent)
    }

    // Not a very efficient implementation, but string concatenation is kept to a minimum.
    // The nested prettify function is recursive, but not tail-recursive.

    val indentStringsByIndent = mutable.Map[Int, String]()

    def containsWhitespaceOnly(elem: simple.Elem): Boolean = {
      elem.children.forall {
        case t: simple.Text if t.text.trim.isEmpty => true
        case _                                     => false
      }
    }

    def fixIfWhitespaceOnly(elem: simple.Elem): simple.Elem =
      if (containsWhitespaceOnly(elem)) elem.withChildren(Vector()) else elem

    prettify(removeAllInterElementWhitespace(elm), 0, indent, indentStringsByIndent, indentToIndentString)
      .transformDescendantElemsOrSelf(fixIfWhitespaceOnly)
  }

  private def prettify(
      elm: simple.Elem,
      currentIndent: Int,
      indent: Int,
      indentStringsByIndent: mutable.Map[Int, String],
      indentToIndentString: Int => String): simple.Elem = {

    def isText(n: simple.Node): Boolean = n match {
      case _: simple.Text => true
      case _              => false
    }

    val childNodes = elm.children
    val hasElemChild = elm.findChildElem(_ => true).isDefined
    val doPrettify = hasElemChild && childNodes.forall(n => !isText(n))

    if (doPrettify) {
      val newIndent = currentIndent + indent

      val indentTextString = indentStringsByIndent.getOrElseUpdate(newIndent, indentToIndentString(newIndent))
      val endIndentTextString = indentStringsByIndent.getOrElseUpdate(currentIndent, indentToIndentString(currentIndent))

      // Recursive calls
      val prettifiedChildNodes = childNodes.map {
        case e: simple.Elem => prettify(e, newIndent, indent, indentStringsByIndent, indentToIndentString)
        case n              => n
      }

      val prefixedPrettifiedChildNodes = prettifiedChildNodes.flatMap { n =>
        List(simple.Text(indentTextString, isCData = false), n)
      }
      val newChildNodes = prefixedPrettifiedChildNodes :+ simple.Text(endIndentTextString, isCData = false)

      elm.withChildren(newChildNodes)
    } else {
      // Once we have encountered text-only content or mixed content, the formatting stops right there for that part of the DOM tree.
      elm
    }
  }
}

object NodeBuilderUtil {

  def apply(namespacePrefixMapper: NamespacePrefixMapper, documentENameExtractor: DocumentENameExtractor): NodeBuilderUtil = {
    new NodeBuilderUtil(namespacePrefixMapper, documentENameExtractor)
  }
}
