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

import eu.cdevreeze.yaidom2.core.EName
import eu.cdevreeze.yaidom2.queryapi.BackingElemApi
import eu.cdevreeze.yaidom2.utils.namespaces.DocumentENameExtractor
import eu.cdevreeze.yaidom2.utils.namespaces.TextENameExtractor

/**
 * DocumentENameExtractor for XBRL taxonomy documents, including locator-free ones. It does not look at ancestry of
 * elements to find TextENameExtractor instances, but considers only element and attribute names for that.
 *
 * Attribute value text EName extractors are a nested Map where the outer Map key is the element name and the inner Map
 * key is the attribute name.
 */
final class XbrlDocumentENameExtractor(
    val elementTextENameExtractors: Map[EName, TextENameExtractor],
    val attributeValueTextENameExtractors: Map[EName, Map[EName, TextENameExtractor]]
) extends DocumentENameExtractor {

  def findElemTextENameExtractor(elem: BackingElemApi): Option[TextENameExtractor] = {
    elementTextENameExtractors.get(elem.name)
  }

  def findAttributeValueENameExtractor(elem: BackingElemApi, attrName: EName): Option[TextENameExtractor] = {
    attributeValueTextENameExtractors.get(elem.name).flatMap(_.get(attrName))
  }
}

object XbrlDocumentENameExtractor {

  // TODO Locator-free model namespace usage

  val defaultElementTextENameExtractors: Map[EName, TextENameExtractor] = {
    Seq(
      "{http://www.xbrl.org/2003/linkbase}usedOn" -> QNameTextENameExtractor,
      "{http://xbrl.org/2010/filter/aspect-cover}qname" -> QNameTextENameExtractor,
      "{http://xbrl.org/2008/filter/concept}qname" -> QNameTextENameExtractor,
      "{http://xbrl.org/2010/filter/concept-relation}qname" -> QNameTextENameExtractor,
      "{http://xbrl.org/2008/filter/dimension}qname" -> QNameTextENameExtractor,
      "{http://xbrl.org/2008/formula}qname" -> QNameTextENameExtractor,
      "{http://xbrl.org/2008/filter/tuple}qname" -> QNameTextENameExtractor,
      "{http://xbrl.org/2008/filter/unit}qname" -> QNameTextENameExtractor,
      "{http://xbrl.org/2014/table}dimensionAspect" -> QNameTextENameExtractor,
      "{http://xbrl.org/2014/table}dimension" -> QNameTextENameExtractor,
      "{http://xbrl.org/2014/table}relationshipSource" -> QNameTextENameExtractor,
      "{http://xbrl.org/2010/filter/aspect-cover}qnameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2008/filter/concept}qnameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2010/filter/concept-relation}qnameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2010/filter/concept-relation}linkroleExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2010/filter/concept-relation}linknameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2010/filter/concept-relation}arcroleExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2010/filter/concept-relation}arcnameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2010/custom-function}step" -> XPathTextENameExtractor,
      "{http://xbrl.org/2010/custom-function}output" -> XPathTextENameExtractor,
      "{http://xbrl.org/2008/filter/dimension}qnameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2008/formula}qnameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2008/formula}precision" -> XPathTextENameExtractor,
      "{http://xbrl.org/2008/formula}decimals" -> XPathTextENameExtractor,
      "{http://xbrl.org/2008/filter/tuple}qnameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2008/filter/unit}qnameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2014/table}relationshipSourceExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2014/table}linkroleExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2014/table}arcroleExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2014/table}formulaAxisExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2014/table}generationsExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2014/table}linknameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2014/table}arcnameExpression" -> XPathTextENameExtractor,
      "{http://xbrl.org/2010/filter/concept-relation}variable" -> QNameTextENameExtractorIgnoringDefaultNamespace,
      "{http://xbrl.org/2008/filter/dimension}variable" -> QNameTextENameExtractorIgnoringDefaultNamespace,
      "{http://xbrl.org/2010/message}message" -> XPathInMessageTextENameExtractor,
    ).map(kv => EName.parse(kv._1) -> kv._2).toMap
  }

  val defaultAttributeValueTextENameExtractors: Map[EName, Map[EName, TextENameExtractor]] = {
    Map(
      EName.parse("{http://www.w3.org/2001/XMLSchema}element") -> Map(
        EName.parse("ref") -> QNameTextENameExtractor,
        EName.parse("substitutionGroup") -> QNameTextENameExtractor,
        EName.parse("type") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}attribute") -> Map(
        EName.parse("ref") -> QNameTextENameExtractor,
        EName.parse("type") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}group") -> Map(
        EName.parse("ref") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}attributeGroup") -> Map(
        EName.parse("ref") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}restriction") -> Map(
        EName.parse("base") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}extension") -> Map(
        EName.parse("base") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://www.w3.org/2001/XMLSchema}keyref") -> Map(
        EName.parse("refer") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/formula}explicitDimension") -> Map(
        EName.parse("dimension") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/formula}typedDimension") -> Map(
        EName.parse("dimension") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchDimension") -> Map(
        EName.parse("dimension") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/variable}function") -> Map(
        EName.parse("name") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/variable}parameter") -> Map(
        EName.parse("name") -> QNameTextENameExtractor,
        EName.parse("as") -> QNameTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2010/filter/concept-relation}conceptRelation") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/assertion/consistency}consistencyAssertion") -> Map(
        EName.parse("absoluteAcceptanceRadius") -> XPathTextENameExtractor,
        EName.parse("proportionalAcceptanceRadius") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/dimension}typedDimension") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/entity}identifier") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/entity}specificScheme") -> Map(
        EName.parse("scheme") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/entity}specificIdentifier") -> Map(
        EName.parse("scheme") -> XPathTextENameExtractor,
        EName.parse("value") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/assertion/existence}existenceAssertion") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/formula}formula") -> Map(
        EName.parse("value") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/formula}entityIdentifier") -> Map(
        EName.parse("scheme") -> XPathTextENameExtractor,
        EName.parse("value") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/formula}instant") -> Map(
        EName.parse("value") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/formula}duration") -> Map(
        EName.parse("start") -> XPathTextENameExtractor,
        EName.parse("end") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/formula}multiplyBy") -> Map(
        EName.parse("measure") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/formula}divideBy") -> Map(
        EName.parse("measure") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/formula}occXpath") -> Map(
        EName.parse("select") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/general}general") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/period}period") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/period}periodStart") -> Map(
        EName.parse("date") -> XPathTextENameExtractor,
        EName.parse("time") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/period}periodEnd") -> Map(
        EName.parse("date") -> XPathTextENameExtractor,
        EName.parse("time") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/period}periodInstant") -> Map(
        EName.parse("date") -> XPathTextENameExtractor,
        EName.parse("time") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/segment-scenario}segment") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/segment-scenario}scenario") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/tuple}locationFilter") -> Map(
        EName.parse("location") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/unit}generalMeasures") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/assertion/value}valueAssertion") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/filter/value}precision") -> Map(
        EName.parse("minimum") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/variable}parameter") -> Map(
        EName.parse("select") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/variable}equalityDefinition") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/variable}generalVariable") -> Map(
        EName.parse("select") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/variable}factVariable") -> Map(
        EName.parse("fallbackValue") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2008/variable}precondition") -> Map(
        EName.parse("test") -> XPathTextENameExtractor,
      ),
      EName.parse("{http://xbrl.org/2010/custom-function}input") -> Map(
        EName.parse("name") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}formula") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}aspects") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}concept") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}entityIdentifier") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}period") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}unit") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}occEmpty") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}occFragments") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}occXpath") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}explicitDimension") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}typedDimension") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}multiplyBy") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/formula}divideBy") -> Map(
        EName.parse("source") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchConcept") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchLocation") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchUnit") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchEntityIdentifier") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchSegment") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchScenario") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchNonXDTSegment") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchNonXDTScenario") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/match}matchDimension") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/period}instantDuration") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/relative}relativeFilter") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/tuple}siblingFilter") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/filter/tuple}locationFilter") -> Map(
        EName.parse("variable") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2008/variable}variableArc") -> Map(
        EName.parse("name") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
      EName.parse("{http://xbrl.org/2014/table}tableParameterArc") -> Map(
        EName.parse("name") -> QNameTextENameExtractorIgnoringDefaultNamespace,
      ),
    )
  }

  val defaultInstance: XbrlDocumentENameExtractor = {
    new XbrlDocumentENameExtractor(defaultElementTextENameExtractors, defaultAttributeValueTextENameExtractors)
  }
}
