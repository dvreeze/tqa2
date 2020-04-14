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

import eu.cdevreeze.tqa2.ENames
import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.tqa2.common.namespaceutils.QNameTextENameExtractor
import eu.cdevreeze.yaidom2.core._
import eu.cdevreeze.yaidom2.node.nodebuilder
import eu.cdevreeze.yaidom2.queryapi.BackingNodes
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor

/**
 * Helper functions for editing xs:schema elements, in order to cleanup the use of namespace prefixes to the "canonical" ones,
 * to the extent possible.
 *
 * TODO Improve!
 *
 * @author Chris de Vreeze
 */
final class SchemaUtil(val namespacePrefixMapper: NamespacePrefixMapper, val documentENameExtractor: DocumentENameExtractor) {
  require(
    namespacePrefixMapper.getPrefix(Namespaces.XsNamespace) == "xs",
    s"Expected prefix 'xs' instead of ${namespacePrefixMapper.getPrefix(Namespaces.XsNamespace)} for namespace ${Namespaces.XsNamespace}"
  )
  require(
    namespacePrefixMapper.getPrefix(Namespaces.LinkNamespace) == "link",
    s"Expected prefix 'link' instead of ${namespacePrefixMapper.getPrefix(Namespaces.LinkNamespace)} for namespace ${Namespaces.LinkNamespace}"
  )
  require(
    namespacePrefixMapper.getPrefix(Namespaces.XLinkNamespace) == "xlink",
    s"Expected prefix 'xlink' instead of ${namespacePrefixMapper.getPrefix(Namespaces.XLinkNamespace)} for namespace ${Namespaces.XLinkNamespace}"
  )

  implicit private val elemCreator: nodebuilder.NodeBuilderCreator = nodebuilder.NodeBuilderCreator(namespacePrefixMapper)

  import elemCreator._
  import nodebuilder.NodeBuilderCreator._

  def cleanupNamespacePrefixesForSchema(schemaElem: BackingNodes.Elem): nodebuilder.Elem = {
    require(schemaElem.name == ENames.XsSchemaEName, s"Not an xs:schema: '${schemaElem.name}'")

    cleanupNamespacePrefixes(schemaElem, startScope)
  }

  def cleanupNamespacePrefixes(elem: BackingNodes.Elem, parentScope: PrefixedScope): nodebuilder.Elem = {
    // Element names and attribute names are converted automatically. It's the attribute values and element text to concentrate on.

    val startElem: nodebuilder.Elem = emptyElem(elem.name, parentScope) // This fixes the prefix in the element name

    val elemWithoutChildren: nodebuilder.Elem = elem.attributes
      .foldLeft(startElem) {
        case (accElem, (attrName, _)) =>
          val (convertedAttrValue, newScope) = convertAttributeValue(elem, attrName, accElem.prefixedScope)
          accElem.creationApi.usingParentScope(newScope).plusAttribute(attrName, convertedAttrValue).underlying
      }

    val elemWithChildren: nodebuilder.Elem = elem.children.foldLeft(elemWithoutChildren) {
      case (accElem, ch) =>
        ch match {
          case che: BackingNodes.Elem =>
            // Recursive call
            accElem.creationApi.plusChild(cleanupNamespacePrefixes(che, accElem.prefixedScope)).underlying
          case t: BackingNodes.Text if t.text.trim.isEmpty =>
            accElem.creationApi.plusChild(nodebuilder.NodeBuilders.Text(t.text)).underlying
          case _: BackingNodes.Text =>
            // Assuming no other non-ignorable text child
            val (convertedText, newScope) = convertElementText(elem, accElem.prefixedScope)
            accElem.creationApi.usingParentScope(newScope).plusChild(nodebuilder.NodeBuilders.Text(convertedText)).underlying
          case _: BackingNodes.Comment =>
            // Comments get lost!
            accElem
          case _: BackingNodes.ProcessingInstruction =>
            // PIs get lost!
            accElem
        }
    }

    elemWithChildren
  }

  private def convertAttributeValue(elem: BackingNodes.Elem, attrName: EName, parentScope: PrefixedScope): (String, PrefixedScope) = {
    val optTextENameExtractor = documentENameExtractor.findAttributeValueENameExtractor(elem, attrName)

    optTextENameExtractor
      .map {
        case QNameTextENameExtractor =>
          val enames: Set[EName] = QNameTextENameExtractor.extractENames(elem.scope.withoutDefaultNamespace, elem.attr(attrName))
          assert(enames.sizeIs == 1)
          val nsOption: Option[String] = enames.head.namespaceUriOption
          val prefixOption: Option[String] = nsOption.map(ns => namespacePrefixMapper.getPrefix(ns))
          val qname: QName = QName(prefixOption, enames.head.localPart)
          val scope: PrefixedScope =
            prefixOption.map(pref => parentScope.append(PrefixedScope.from(pref -> nsOption.get))).getOrElse(parentScope)
          (qname.toString, scope)
        case _ =>
          // No change to the attribute value, but we do need to catch the namespaces and corresponding originally used prefixes
          val namespaces: Set[String] =
            QNameTextENameExtractor.extractENames(elem.scope.withoutDefaultNamespace, elem.attr(attrName)).flatMap(_.namespaceUriOption)
          val scopeToAdd: Scope = elem.scope.filterNamespaces(namespaces)
          require(
            scopeToAdd.defaultNamespaceOption.isEmpty,
            s"Default namespace '${scopeToAdd.defaultNamespaceOption.get}' not allowed here")
          val scope: PrefixedScope =
            parentScope.append(PrefixedScope.from(scopeToAdd))
          (elem.text, scope)
      }
      .getOrElse((elem.text, parentScope))
  }

  private def convertElementText(elem: BackingNodes.Elem, parentScope: PrefixedScope): (String, PrefixedScope) = {
    val optTextENameExtractor = documentENameExtractor.findElemTextENameExtractor(elem)

    optTextENameExtractor
      .map {
        case QNameTextENameExtractor =>
          val enames: Set[EName] = QNameTextENameExtractor.extractENames(elem.scope, elem.text)
          assert(enames.sizeIs == 1)
          val nsOption: Option[String] = enames.head.namespaceUriOption
          val prefixOption: Option[String] = nsOption.map(ns => namespacePrefixMapper.getPrefix(ns))
          val qname: QName = QName(prefixOption, enames.head.localPart)
          val scope: PrefixedScope =
            prefixOption.map(pref => parentScope.append(PrefixedScope.from(pref -> nsOption.get))).getOrElse(parentScope)
          (qname.toString, scope)
        case _ =>
          // No change to the element text, but we do need to catch the namespaces and corresponding originally used prefixes
          val namespaces: Set[String] = QNameTextENameExtractor.extractENames(elem.scope, elem.text).flatMap(_.namespaceUriOption)
          val scopeToAdd: Scope = elem.scope.filterNamespaces(namespaces)
          require(
            scopeToAdd.defaultNamespaceOption.isEmpty,
            s"Default namespace '${scopeToAdd.defaultNamespaceOption.get}' not allowed here")
          val scope: PrefixedScope =
            parentScope.append(PrefixedScope.from(scopeToAdd))
          (elem.text, scope)
      }
      .getOrElse((elem.text, parentScope))
  }

  private def startScope: PrefixedScope = {
    PrefixedScope.from("xs" -> Namespaces.XsNamespace, "link" -> Namespaces.LinkNamespace, "xlink" -> Namespaces.XLinkNamespace)
  }
}
