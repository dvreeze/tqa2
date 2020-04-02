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

package eu.cdevreeze.tqa2.common.namespaceutils

import eu.cdevreeze.xpathparser.ast.EQName
import eu.cdevreeze.xpathparser.parse.XPathParser.xpathExpr
import eu.cdevreeze.xpathparser.util.EQNameUtil
import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.QName
import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.utils.namespaces.TextENameExtractor
import fastparse._

/**
 * Yaidom2 TextENameExtractor for text that must be interpreted as XPath.
 *
 * @author Chris de Vreeze
 */
object XPathTextENameExtractor extends TextENameExtractor {

  /**
   * Finds all used QNames resolved as ENames, by parsing the XPath expression and finding all QNames
   * in the abstract syntax tree. The pattern xs:QName('prefix:localname') is also recognized and it also
   * contributes ENames.
   *
   * Names (of functions) in the "fn" namespace (more precisely: namespace "http://www.w3.org/2005/xpath-functions")
   * are returned as names without any namespace if no prefix has been used. This is not correct, but
   * needed in order to prevent the situation where we conclude from the returned ENames that
   * we need a namespace declaration for the "fn" namespace. In other words, the default namespace is not
   * used here when extracting ENames, so unprefixed QNames are resolved as no-namespace ENames.
   */
  def extractENames(scope: Scope, text: String): Set[EName] = {
    // Trying not to throw any exceptions.

    val xpathExprParseResult = parse(text, xpathExpr(_))

    val eqNames: Set[EQName] =
      xpathExprParseResult match {
        case Parsed.Success(expr, _) => EQNameUtil.findUsedEQNames(expr, EQNameUtil.eqnameProducerFromXsQName)
        case Parsed.Failure(_, _, _) => Set.empty // Exception ignored!
      }

    val qnames: Set[QName] = eqNames.collect {
      case EQName.QName(qn) => QName.parse(qn.toString)
    }

    // Resolve the QNames as ENames, but do not use the default namespace, if any.
    // The default namespace in an XPath expression should be the one normally linked to prefix "fn".
    // As described above, we do not use any default namespace to resolve QNames, however.

    // Note that below any unresolved prefixed QName simply gets lost, without throwing any exception.

    val enames: Set[EName] = qnames.flatMap(qn => scope.withoutDefaultNamespace.resolveQNameOption(qn))
    enames
  }
}
