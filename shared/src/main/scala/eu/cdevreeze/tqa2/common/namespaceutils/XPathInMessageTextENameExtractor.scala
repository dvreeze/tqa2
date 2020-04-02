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

import scala.util.Try

import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.core.Scope
import eu.cdevreeze.yaidom2.utils.namespaces.TextENameExtractor
import fastparse._

/**
 * Yaidom2 TextENameExtractor for text that must be interpreted as a message containing placeholders with XPath in them.
 *
 * @author Chris de Vreeze
 */
object XPathInMessageTextENameExtractor extends TextENameExtractor {

  /**
   * Finds all used QNames resolved as ENames, by parsing the XPath expressions in the message and finding all QNames
   * in the abstract syntax trees. The pattern xs:QName('prefix:localname') is also recognized and it also
   * contributes ENames.
   */
  def extractENames(scope: Scope, text: String): Set[EName] = {
    // Trying not to throw any exceptions.

    val pars: Seq[String] =
      parse(text, MessageParsing.messageParameters(_)) match {
        case Parsed.Success(p, _)    => p
        case Parsed.Failure(_, _, _) => IndexedSeq()
      }

    Try(pars.flatMap(par => XPathTextENameExtractor.extractENames(scope, par.trim)).toSet).getOrElse(Set())
  }

  /**
   * Finding parameters in message strings, minding escaping of braces inside parameters.
   * This parsing uses the FastParse library (instead of regular expressions).
   *
   * This is a rather basic message parser, so maybe it does not cover all corner cases. On the other hand,
   * the template text is skipped (for this use case of extracting ENames from the XPath message parameters),
   * which simplifies things considerably. For example, message elements in XBRL can have a separator attribute,
   * but for extracting ENames that's not relevant.
   *
   * See also the attribute value templates section in the XSLT specification.
   */
  private object MessageParsing {

    import fastparse._
    import MultiLineWhitespace._

    private def nonBraces[_: P]: P[String] =
      P(CharsWhile(c => c != '{' && c != '}').!)

    private def escapedParameterChar[_: P]: P[String] =
      P(StringIn("{{", "}}").!).map(_.take(1))

    // Skipped template text (a "fixed part") can contain non-brace strings and escaped parameter characters.

    private def skippedTemplateText[_: P]: P[Unit] =
      P((escapedParameterChar | nonBraces).rep).map(_ => ())

    // Parameter text (enclosed by braces) can contain anything but braces (escaping of braces does not apply here).
    // It is assumed that XPath does not contain braces.

    // Note that it is possible that XPath does contain braces! For example, in EQNames with namespaces,
    // in enclosed expressions, comments and in (XPath 3) map constructors! The worst that can happen in those cases
    // is that some ENames are not found by the TextENameExtractor.

    private def parameterText[_: P]: P[String] = P(nonBraces)

    private def messageParameter[_: P]: P[String] = P("{" ~ parameterText ~ "}")

    def messageParameters[_: P]: P[Seq[String]] =
      P(skippedTemplateText ~ messageParameter.rep(sep = skippedTemplateText) ~ skippedTemplateText ~ End)
        .map(_.toIndexedSeq)
  }
}
