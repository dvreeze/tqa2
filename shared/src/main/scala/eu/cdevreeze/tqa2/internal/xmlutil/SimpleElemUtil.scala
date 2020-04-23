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

import eu.cdevreeze.tqa2.Namespaces
import eu.cdevreeze.tqa2.common.namespaceutils.QNameTextENameExtractor
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.QName
import eu.cdevreeze.yaidom2.node.indexed
import eu.cdevreeze.yaidom2.node.simple
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor

/**
 * Helper functions for editing simple elements. For example, removing default namespaces, etc.
 *
 * @author Chris de Vreeze
 */
final class SimpleElemUtil(val documentENameExtractor: DocumentENameExtractor) {

  import SimpleElemUtil._

  /**
   * If the given element consistently uses the default namespace for the XML Schema namespace, it is replaced by a prefixed
   * namespace throughout the tree, using the given prefix. Otherwise the element is returned unaltered (taking up no costly CPU time).
   *
   * QName-valued element text and attribute values are also edited, if the default namespace is used.
   */
  def tryToRemoveDefaultXsNamespace(elem: indexed.Elem, xsPrefix: String = "xs"): simple.Elem = {
    if (elem.scope.defaultNamespaceOption.contains(Namespaces.XsNamespace) &&
        elem.findAllDescendantElemsOrSelf.forall(_.scope.defaultNamespaceOption.contains(Namespaces.XsNamespace))) {

      removeDefaultXsNamespace(elem, xsPrefix)
    } else {
      elem.underlyingElem
    }
  }

  private def removeDefaultXsNamespace(elem: indexed.Elem, xsPrefix: String): simple.Elem = {
    assert(elem.scope.defaultNamespaceOption.contains(Namespaces.XsNamespace))

    val affectedElemKeys: Seq[ElemKey] = elem
      .filterDescendantElemsOrSelf { e =>
        assert(e.scope.defaultNamespaceOption.contains(Namespaces.XsNamespace))

        documentENameExtractor.findElemTextENameExtractor(e).exists {
          case QNameTextENameExtractor =>
            e.textAsQName.prefixOption.isEmpty // Default namespace used
          case _ =>
            false
        }
      }
      .map(ElemKey.from)

    val affectedElemAttrKeys: Seq[ElemAttributeKey] = elem.findAllDescendantElemsOrSelf
      .flatMap { e =>
        assert(e.scope.defaultNamespaceOption.contains(Namespaces.XsNamespace))

        e.attributes.flatMap {
          case (attrName, attrValue) =>
            documentENameExtractor.findAttributeValueENameExtractor(e, attrName).flatMap {
              case QNameTextENameExtractor =>
                val defaultNamespaceUsed = e.attrAsQNameOption(attrName).filter(_.prefixOption.isEmpty).nonEmpty

                if (defaultNamespaceUsed) {
                  Some(ElemAttributeKey.from(e, attrName))
                } else {
                  None
                }
              case _ =>
                None
            }
        }
      }

    val rawResultElem: simple.Elem = elem.underlyingElem
      .transformDescendantElemsOrSelf { e =>
        assert(e.scope.defaultNamespaceOption.contains(Namespaces.XsNamespace))

        // Enhance scope with prefix, and adapt element QName

        val scope = e.scope.append(xsPrefix, Namespaces.XsNamespace)

        val qname: QName = if (e.qname.prefixOption.isEmpty) {
          assert(e.name.namespaceUriOption.contains(Namespaces.XsNamespace))
          QName(xsPrefix, e.qname.localPart)
        } else {
          e.qname
        }

        // Attributes use no default namespace in their names.
        new simple.Elem(qname, e.attributesByQName, scope, e.children)
      }
      .updateDescendantElemsOrSelf(affectedElemKeys.map(_.elemNavigationPath).toSet) {
        case (e, _) =>
          assert(QName.parse(e.text).prefixOption.isEmpty)

          val enameInText: EName = QNameTextENameExtractor
            .extractENames(e.scope, e.text)
            .head
            .ensuring(_.namespaceUriOption.contains(Namespaces.XsNamespace))

          val qnameInText: QName = QName(xsPrefix, enameInText.localPart)
          val elemText: simple.Text = simple.Text(qnameInText.toString, isCData = false)
          new simple.Elem(e.qname, e.attributesByQName, e.scope, Vector(elemText))
      }
      .updateDescendantElemsOrSelf(affectedElemAttrKeys.map(_.elemNavigationPath).toSet) {
        case (e, p) =>
          // TODO Improve performance

          affectedElemAttrKeys.filter(_.elemNavigationPath == p).foldLeft(e) {
            case (accElem, attrKey) if accElem.attrOption(attrKey.attrName).nonEmpty =>
              assert(QName.parse(accElem.attr(attrKey.attrName)).prefixOption.isEmpty)

              val enameInAttrValue: EName = QNameTextENameExtractor
                .extractENames(accElem.scope, accElem.attr(attrKey.attrName))
                .head
                .ensuring(_.namespaceUriOption.contains(Namespaces.XsNamespace))

              val qnameInAttrValue: QName = QName(xsPrefix, enameInAttrValue.localPart)
              val attrQName: QName = accElem.attributesByQName.keySet
                .filter(qn => accElem.scope.withoutDefaultNamespace.resolveQName(qn) == attrKey.attrName)
                .head

              new simple.Elem(
                accElem.qname,
                accElem.attributesByQName.updated(attrQName, qnameInAttrValue.toString),
                accElem.scope,
                accElem.children)
            case (accElem, _) =>
              accElem
          }
      }

    rawResultElem.transformDescendantElemsOrSelf { e =>
      new simple.Elem(e.qname, e.attributesByQName, e.scope.withoutDefaultNamespace, e.children)
    }
  }
}

object SimpleElemUtil {

  final case class ElemKey(elemName: EName, elemNavigationPath: Seq[Int])

  object ElemKey {

    def from(e: indexed.Elem): ElemKey = ElemKey(e.name, e.ownNavigationPathRelativeToRootElem)
  }

  final case class ElemAttributeKey(elemName: EName, attrName: EName, elemNavigationPath: Seq[Int])

  object ElemAttributeKey {

    def from(e: indexed.Elem, attrName: EName): ElemAttributeKey = {
      ElemAttributeKey(e.name, attrName, e.ownNavigationPathRelativeToRootElem)
    }
  }
}
